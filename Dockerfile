FROM openjdk:17-jdk-alpine

RUN addgroup -S mini-bank && adduser -S telegram-bot -G mini-bank

USER telegram-bot:mini-bank

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-Dspring.cloud.openfeign.client.config.middle-clients-service.url=${MIDDLE_SERVICE_URL}", "-jar", "/app.jar"]