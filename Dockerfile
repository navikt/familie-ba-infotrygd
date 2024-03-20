FROM ghcr.io/navikt/baseimages/temurin:21-appdynamics

ENV APPD_ENABLED=true
ENV APP_NAME=familie-ba-infotrygd

COPY init.sh /init-scripts/init.sh

COPY build/libs/familie-ba-infotrygd-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080

