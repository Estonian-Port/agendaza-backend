# Etapa 1: Build con Gradle
FROM gradle:8.2.1-jdk17-alpine AS builder

WORKDIR /home/app

COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
COPY src ./src

# Genera el .jar optimizado
RUN gradle bootJar --no-daemon

# Etapa 2: Imagen liviana para correr la app
FROM eclipse-temurin:17-jre-alpine

ENV APP_HOME=/app
WORKDIR $APP_HOME

COPY --from=builder /home/app/build/libs/*.jar app.jar

EXPOSE 8081

CMD ["java", "-jar", "app.jar"]
