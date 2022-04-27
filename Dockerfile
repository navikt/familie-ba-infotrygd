FROM navikt/java:17

COPY init.sh /init-scripts/init.sh

COPY build/libs/familie-ba-infotrygd-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080

