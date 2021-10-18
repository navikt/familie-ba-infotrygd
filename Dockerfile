FROM navikt/java:11

COPY build/libs/*.jar app.jar
EXPOSE 8080

