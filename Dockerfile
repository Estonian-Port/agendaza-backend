# Etapa 1: Build con Gradle
FROM gradle:8.2.1-jdk17-alpine AS builder

WORKDIR /home/app

# Copiar archivos de configuración para aprovechar el caché de capas de Docker
COPY build.gradle.kts settings.gradle.kts gradle.properties* ./
COPY gradle ./gradle

# Descargar dependencias antes de copiar el código fuente
RUN gradle dependencies --no-daemon || true

COPY src ./src

# Generar el JAR excluyendo tests para acelerar el build
RUN gradle bootJar -x test --no-daemon

# Etapa 2: Imagen liviana de ejecución
FROM eclipse-temurin:17-jre-alpine

ENV APP_HOME=/app \
    SPRING_PROFILES_ACTIVE=prod

WORKDIR $APP_HOME

# Crear usuario y grupo sin privilegios de root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copiar el artifact y asignar permisos
COPY --from=builder /home/app/build/libs/*.jar app.jar
RUN chown -R appuser:appgroup $APP_HOME

# Cambiar al usuario no privilegiado
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]