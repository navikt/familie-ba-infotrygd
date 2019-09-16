FROM navikt/java:11

ENV JAVA_OPTS="${JAVA_OPTS} -Xms270M"

COPY build/libs/*.jar app.jar
EXPOSE 8080

