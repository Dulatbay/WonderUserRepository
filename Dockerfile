FROM openjdk:21-slim as build

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

RUN chmod +x ./gradlew

COPY client-libs client-libs

RUN ./gradlew dependencies
RUN ./gradlew dependencyInsight --dependency springdoc-openapi

COPY src src

RUN ./gradlew bootJar

FROM openjdk:21-slim

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]
