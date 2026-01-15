# ============================================
# Dockerfile para SA2Gemini
# Multi-stage build para optimizar tamaño
# ============================================

# Etapa 1: Build
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copiar archivos de Maven
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Dar permisos de ejecución a mvnw
RUN chmod +x ./mvnw

# Descargar dependencias (cacheadas si pom.xml no cambia)
RUN ./mvnw dependency:go-offline -B

# Copiar código fuente
COPY src src

# Construir la aplicación (sin tests para acelerar)
RUN ./mvnw package -DskipTests -Pprod

# Etapa 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Crear usuario no-root para seguridad
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Copiar JAR desde la etapa de build
COPY --from=builder /app/target/*.jar app.jar

# Cambiar ownership del archivo
RUN chown appuser:appgroup app.jar

# Usar usuario no-root
USER appuser

# Exponer puerto (será sobrescrito por la variable PORT)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT:-8080}/login || exit 1

# Ejecutar la aplicación con perfil de producción
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
