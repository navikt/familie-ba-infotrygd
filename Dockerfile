FROM navikt/java:11

COPY init.sh /init-scripts/init.sh

COPY build/libs/*.jar app.jar
EXPOSE 8080

