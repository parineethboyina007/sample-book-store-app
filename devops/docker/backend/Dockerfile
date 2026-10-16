FROM eclipse-temurin:8-jre-jammy

WORKDIR /app

ARG JAR_FILE

COPY ${JAR_FILE} /app/app.jar

RUN mkdir -p /app/Logs \
    && chown -R 10001:10001 /app

USER 10001:10001

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
