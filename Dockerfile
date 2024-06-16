FROM openjdk:21-slim as build

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

RUN chmod +x ./gradlew

COPY client-libs client-libs

RUN ./gradlew dependencies

COPY src src

RUN ./gradlew bootJar --info

FROM openjdk:21-slim

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Dvertx.disableDnsResolver=true", "-Djava.net.preferIPv4Stack=true", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]