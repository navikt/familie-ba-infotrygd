FROM busybox:1.36.1-uclibc AS busybox

FROM gcr.io/distroless/java21-debian12:nonroot
COPY --from=busybox /bin/cat /bin/cat
COPY --from=busybox /bin/ash /bin/ash
COPY --from=busybox /bin/printenv /bin/printenv
#COPY init.sh /init-scripts/init.sh
COPY --chown=nonroot:nonroot ./build/libs/familie-ba-infotrygd-0.0.1-SNAPSHOT.jar /app/app.jar
WORKDIR /app

ENV APP_NAME=familie-ba-infotrygd
ENV TZ="Europe/Oslo"

CMD [ "ash", "-c", "export APP_DATASOURCE_USERNAME=$(cat \"$APP_DATASOURCE_USERNAME_PATH\" 2> /dev/null || echo $APP_DATASOURCE_USERNAME)" ]
CMD [ "ash", "-c", "export APP_DATASOURCE_PASSWORD=$(cat \"$APP_DATASOURCE_PASSWORD\" 2> /dev/null || echo $APP_DATASOURCE_PASSWORD)" ]

# TLS Config works around an issue in OpenJDK... See: https://github.com/kubernetes-client/java/issues/854
#ENTRYPOINT [ "ash", "-c", "export APP_DATASOURCE_USERNAME=$(cat \"$APP_DATASOURCE_USERNAME_PATH\" 2> /dev/null || echo $APP_DATASOURCE_USERNAME) && printenv " ]
ENTRYPOINT [ "java", "-Djdk.tls.client.protocols=TLSv1.2", "-jar", "/app/app.jar" ]