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

# Variables d'environnement par défaut
ENV SPRING_PROFILES_ACTIVE=dev
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Comando de health check (Railway compatible)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8090/api/actuator/health || exit 1

# Exécuter l'application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar target/stockchef-back-*.jar"]