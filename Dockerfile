FROM sbtscala/scala-sbt:eclipse-temurin-jammy-11.0.17_8_1.9.0_3.2.2 AS build
WORKDIR /app
COPY . /app
RUN sbt assembly

FROM eclipse-temurin:11-jre-alpine
WORKDIR /app
USER 1001
COPY --from=build --chown=1001 /app/target/scala-3.2.2/app.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
CMD []
