FROM eclipse-temurin:8-jre-jammy

WORKDIR /app

COPY --chown=10001:10001 target/bookstore-catalog-service-0.0.1-SNAPSHOT.jar /app/app.jar
COPY --chown=10001:10001 images/ /app/images/

RUN mkdir -p /app/Logs \
    && chown -R 10001:10001 /app

USER 10001:10001

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
