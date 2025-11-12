# Usar una imagen base de OpenJDK 21 (compatible con 24)
FROM eclipse-temurin:21-jdk-alpine

# Instalar curl para health checks
RUN apk add --no-cache curl

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Copiar código fuente
COPY src src

# Dar permisos de ejecución al wrapper de Maven
RUN chmod +x mvnw

# Construir la aplicación (usando el wrapper de Maven incluido)
RUN ./mvnw clean package -DskipTests

# Exponer el puerto de la aplicación
EXPOSE 8090

# Variables de entorno por defecto
ENV SPRING_PROFILES_ACTIVE=dev
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Comando de health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8090/api/health || exit 1

# Ejecutar la aplicación
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar target/stockchef-back-*.jar"]