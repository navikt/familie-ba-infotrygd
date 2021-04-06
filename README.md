# Infotrygd Barnetrygd

Dette er en REST-tjeneste som gir tilgang til historiske data fra Infotrygd
via replikert database.

Swagger: http://localhost:8080/swagger-ui.html

Tabeller og kolonner som er i bruk: http://localhost:8080/tables 

Confluence:
- https://confluence.adeo.no/display/MODNAV/Databaser

#### Tilgang til repoer fra Github

For at gradle skal kunne hente repositories fra github, må gpr.key=\<token\> settes i ~/.gradle/gradle.properties. Token må lages i Github, med tilgangen read:packages påskrudd. 

