# Utiliser une image de base OpenJDK 21 (compatible avec 24)
FROM eclipse-temurin:21-jdk-alpine

# Installer curl pour les vérifications de santé
RUN apk add --no-cache curl

# Établir répertoire de travail
WORKDIR /app

# Copier fichiers de configuration Maven
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Copier code source
COPY src src

# Donner permissions d'exécution au wrapper Maven
RUN chmod +x mvnw

# Construire l'application (en utilisant le wrapper Maven inclus)
RUN ./mvnw clean package -DskipTests

# Exposer le port de l'application
EXPOSE 8090

# Variables d'environnement par défaut (Render compatible)
ENV SPRING_PROFILES_ACTIVE=production
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SERVER_PORT=8090

# Health check pour Render (endpoint /api/health)
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
    CMD curl -f http://localhost:8090/api/health || exit 1

# Exécuter l'application (Render optimized)
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar target/stockchef-back-*.jar --server.port=${PORT:-8090}"]