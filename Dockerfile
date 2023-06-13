FROM sbtscala/scala-sbt:eclipse-temurin-jammy-11.0.17_8_1.9.0_3.2.2 AS build
LABEL name=pawel
WORKDIR /app
COPY . /app
RUN sbt assembly

FROM eclipse-temurin:11-jre-alpine
WORKDIR /app
COPY --from=build /app/target/scala-3.2.2/app.jar app.jar
USER 1001
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
CMD []
