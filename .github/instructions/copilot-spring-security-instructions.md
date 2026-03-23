---
applyTo: "**/*.kt,**/application*.yml,**/pom.xml"
---

# Spring Security Setup

This project uses **Spring Security** with **Azure AD / OAuth2 JWT** as the authentication mechanism.
All API endpoints require a valid Azure AD Bearer token. Role-based authorization is enforced via
`@PreAuthorize` annotations backed by a custom JWT converter that maps Azure AD groups and app roles
to Spring Security `GrantedAuthority` objects.

---

## pom.xml

Two Spring Boot starters are required — both are version-managed by `spring-boot-starter-parent`:

```xml
<!-- Core Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- OAuth2 resource server support (JWT validation) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

---

## application.yml

### JWT resource server

Spring validates the incoming Bearer token against the Azure AD OIDC discovery document:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${AZURE_OPENID_CONFIG_ISSUER}   # injected by NAIS at runtime
          audiences:
            - ${AZURE_APP_CLIENT_ID}                  # e.g. "myapp-client-id"
            - api://${AZURE_APP_CLIENT_ID}            # required for on-behalf-of flows
```

`AZURE_OPENID_CONFIG_ISSUER` and `AZURE_APP_CLIENT_ID` are injected automatically by the
[NAIS platform](https://doc.nais.io/security/auth/azure-ad/) when the application is deployed.

### Group ID env vars

Each Azure AD group that maps to a role has its own env var.
The values differ between dev (default in `application.yml`) and prod (`application-prod.yml`):

| Env var | Dev UUID | Prod UUID |
|---|---|---|
| `TEAMFAMILIE_FORVALTNING_GROUP_ID` | `c62e908a-cf20-4ad0-b7b3-3ff6ca4bf38b` | `024bfb25-ead3-4c04-bbe1-1db3d3362188` |

```yaml
# application.yml  (dev / default)
TEAMFAMILIE_FORVALTNING_GROUP_ID: "c62e908a-cf20-4ad0-b7b3-3ff6ca4bf38b"
```

```yaml
# application-prod.yml
TEAMFAMILIE_FORVALTNING_GROUP_ID: "024bfb25-ead3-4c04-bbe1-1db3d3362188"
```

---

## SecurityConfiguration.kt

Located at `src/main/kotlin/no/nav/infotrygd/kontantstotte/config/SecurityConfiguration.kt`.

### Key annotations

```kotlin
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)   // enables @PreAuthorize on controllers
```

`@EnableMethodSecurity(prePostEnabled = true)` is **required** for `@PreAuthorize` to work.

### Filter chain — public vs protected paths

```kotlin
authorizeHttpRequests {
    // Publicly accessible (no token required)
    authorize("/internal/**", permitAll)       // liveness/readiness probes
    authorize("/actuator/**", permitAll)       // Spring Boot Actuator
    authorize("/swagger-ui/**", permitAll)     // Swagger UI
    authorize("/v3/api-docs/**", permitAll)    // OpenAPI docs
    authorize("/swagger-ui.html", permitAll)
    authorize("/testtoken/**", permitAll)

    // Everything else requires a valid Azure AD JWT
    authorize(anyRequest, authenticated)
}
```

### JWT converter wiring

The custom `AzureJwtAuthenticationConverter` is injected and wired as the JWT authentication converter:

```kotlin
oauth2ResourceServer {
    jwt {
        jwtAuthenticationConverter = azureJwtAuthenticationConverter
    }
}
```

CSRF is disabled because the API is stateless (tokens, not cookies):

```kotlin
csrf { disable() }
```

---

## How endpoints are protected with `hasRole`

Role enforcement is done with `@PreAuthorize` from Spring Security's method security.
It can be applied at **class level** (all methods in the controller) or **method level**.

### Class-level example — `InnsynController.kt`

```kotlin
@PreAuthorize("hasRole('FORVALTER') or hasRole('APPLICATION')")
@RestController
@RequestMapping("/api")
class InnsynController(...) {
    // All endpoints in this controller require FORVALTER or APPLICATION role
}
```

### Important: the `ROLE_` prefix

Spring Security's `hasRole('X')` automatically prepends `ROLE_` when comparing authorities.
The `Rolle` enum's `authority()` method ensures the stored authority always matches:

```kotlin
fun authority(): String = "ROLE_$name"
// FORVALTER  → "ROLE_FORVALTER"
// APPLICATION → "ROLE_APPLICATION"
```

So `hasRole('FORVALTER')` matches the authority `"ROLE_FORVALTER"`. ✓

---

## How roles are created

### `Rolle.kt` — the role enum

Located at `src/main/kotlin/no/nav/infotrygd/kontantstotte/security/Rolle.kt`.

```kotlin
enum class Rolle {
    FORVALTER,    // Human user — token contains the forvalter Azure AD group
    APPLICATION,  // Machine-to-machine — token carries the "access_as_application" app role
    ;

    fun authority(): String = "ROLE_$name"
}
```

### `AzureJwtAuthenticationConverter.kt` — how claims become roles

Located at `src/main/kotlin/no/nav/infotrygd/kontantstotte/security/AzureJwtAuthenticationConverter.kt`.

The converter reads two JWT claims:

| JWT claim | Source | Grants role |
|---|---|---|
| `groups` | Azure AD group membership | `FORVALTER` (if group UUID matches) |
| `roles` | Azure AD app role assignment | `APPLICATION` (if `access_as_application` is present) |

```kotlin
override fun convert(jwt: Jwt): AbstractAuthenticationToken {
    val grupper = jwt.getClaimAsStringList("groups") ?: emptyList()
    val roles   = jwt.getClaimAsStringList("roles")  ?: emptyList()

    val roller = buildSet {
        if (grupper.contains(forvalterGroupId)) add(Rolle.FORVALTER)
        if (roles.contains("access_as_application")) add(Rolle.APPLICATION)
    }

    val authorities = roller.map { SimpleGrantedAuthority(it.authority()) }
    return JwtAuthenticationToken(jwt, authorities)
}
```

---

## How to add a new group-based role (step by step)

Use this pattern whenever a new Azure AD group should grant access to specific endpoints.

### Step 1 — Add the group UUID to `application.yml` (dev)

```yaml
# application.yml
MY_NEW_GROUP_ID: "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"   # dev Azure AD group UUID
```

### Step 2 — Add the prod UUID to `application-prod.yml`

```yaml
# application-prod.yml
MY_NEW_GROUP_ID: "yyyyyyyy-yyyy-yyyy-yyyy-yyyyyyyyyyyy"   # prod Azure AD group UUID
```

### Step 3 — Add the new entry to the `Rolle` enum

```kotlin
// Rolle.kt
enum class Rolle {
    FORVALTER,
    APPLICATION,
    MY_NEW_ROLE,    // ← add here, with a descriptive comment
    ;

    fun authority(): String = "ROLE_$name"
}
```

### Step 4 — Inject the group ID and add the mapping in `AzureJwtAuthenticationConverter.kt`

```kotlin
@Component
class AzureJwtAuthenticationConverter(
    @param:Value("\${TEAMFAMILIE_FORVALTNING_GROUP_ID}") private val forvalterGroupId: String,
    @param:Value("\${MY_NEW_GROUP_ID}") private val myNewGroupId: String,   // ← inject
) : Converter<Jwt, AbstractAuthenticationToken> {

    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        val grupper = jwt.getClaimAsStringList("groups") ?: emptyList()
        val roles   = jwt.getClaimAsStringList("roles")  ?: emptyList()

        val roller = buildSet {
            if (grupper.contains(forvalterGroupId)) add(Rolle.FORVALTER)
            if (grupper.contains(myNewGroupId))     add(Rolle.MY_NEW_ROLE)   // ← add check
            if (roles.contains("access_as_application")) add(Rolle.APPLICATION)
        }

        val authorities = roller.map { SimpleGrantedAuthority(it.authority()) }
        return JwtAuthenticationToken(jwt, authorities)
    }
}
```

### Step 5 — Protect the relevant controller or method with `@PreAuthorize`

```kotlin
// Class level — all endpoints in the controller require the role
@PreAuthorize("hasRole('MY_NEW_ROLE')")
@RestController
class MyNewController(...) { ... }

// Method level — only this specific endpoint requires the role
@PreAuthorize("hasRole('MY_NEW_ROLE')")
@GetMapping("/some-endpoint")
fun someEndpoint(): SomeResponse = ...

// Combine with existing roles if needed
@PreAuthorize("hasRole('MY_NEW_ROLE') or hasRole('APPLICATION')")
```

### Checklist summary

- [ ] Group UUID added to `application.yml` (dev)
- [ ] Group UUID added to `application-prod.yml` (prod)
- [ ] New entry in `Rolle` enum
- [ ] `@Value` constructor param added to `AzureJwtAuthenticationConverter`
- [ ] `grupper.contains(...)` check added in the converter's `buildSet`
- [ ] `@PreAuthorize("hasRole('...')")` added to the target controller/method

