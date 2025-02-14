# Final image
FROM gcr.io/distroless/java21-debian12:nonroot
COPY init.sh /init-scripts/init.sh
COPY --chown=nonroot:nonroot ./build/libs/familie-ba-infotrygd-0.0.1-SNAPSHOT.jar /app/app.jar
WORKDIR /app

ENV APP_NAME=familie-ba-infotrygd
ENV TZ="Europe/Oslo"
# TLS Config works around an issue in OpenJDK... See: https://github.com/kubernetes-client/java/issues/854
ENTRYPOINT [ "java", "-Djdk.tls.client.protocols=TLSv1.2", "-XX:MinRAMPercentage=25.0", "-XX:MaxRAMPercentage=75.0", "-XX:+HeapDumpOnOutOfMemoryError", "-XX:HeapDumpPath=/tmp", "-jar", "/app/app.jar" ]

