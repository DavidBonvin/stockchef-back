# ⚙️ Configuration Avancée - StockChef Backend

## 🗄️ Configuration Spécifique des Bases de Données

### PostgreSQL Optimisé pour StockChef

#### Composants d'Installation Recommandés
```
✅ Installation Minimale (Docker)
   └── PostgreSQL (64 bit) v18.0-2

✅ Installation Développement Local
   ├── PostgreSQL (64 bit) v18.0-2
   ├── pgAgent (64 bit) v4.2.3-1
   ├── pgJDBC v42.7.2-1 (optionnel)
   └── psqlODBC (64 bit) v13.02.0000-1 (optionnel)

🎯 Installation avec Données Géographiques
   └── + PostGIS 3.6 Bundle pour localisations d'inventaire
```

#### Configuration Optimale postgresql.conf
```ini
# Configuration pour StockChef (ajuster selon RAM disponible)
shared_buffers = 256MB
effective_cache_size = 1GB
maintenance_work_mem = 64MB
wal_buffers = 16MB
checkpoint_completion_target = 0.9
random_page_cost = 1.1

# Pour développement
log_statement = 'all'
log_duration = on
```

#### Configuration de Pool de Connexions pour PostgreSQL
```properties
# application-postgres.properties optimisé
spring.datasource.hikari.maximum-pool-size=25
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.validation-timeout=5000
```

### MySQL Optimisé pour StockChef

#### Configuration my.cnf Recommandée
```ini
[mysqld]
# Configuración básica para StockChef
innodb_buffer_pool_size = 256M
innodb_log_file_size = 64M
innodb_flush_log_at_trx_commit = 2
max_connections = 50
query_cache_size = 32M
tmp_table_size = 64M
max_heap_table_size = 64M
```

## 🎛️ Configuración de Perfiles Avanzada

### Configuración por Entorno

#### Desarrollo Local
```properties
# application-dev.properties
spring.profiles.active=dev
spring.h2.console.enabled=true
spring.datasource.url=jdbc:h2:mem:testdb
logging.level.com.stockchef=DEBUG
```

#### Staging
```properties
# application-staging.properties
spring.profiles.active=staging
spring.datasource.url=jdbc:mysql://staging-mysql:3306/stockchef_staging
spring.jpa.hibernate.ddl-auto=validate
logging.level.com.stockchef=INFO
```

#### Producción
```properties
# application-prod.properties
spring.profiles.active=prod
spring.datasource.url=${DATABASE_URL}
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
logging.level.com.stockchef=WARN
server.error.include-stacktrace=never
```

## 🐳 Docker Compose Avanzado

### Configuración de Red Personalizada
```yaml
# docker-compose.override.yml
version: '3.8'
networks:
  stockchef-network:
    driver: bridge
    ipam:
      config:
        - subnet: 172.20.0.0/16
          gateway: 172.20.0.1

services:
  stockchef-backend:
    networks:
      stockchef-network:
        ipv4_address: 172.20.0.10
```

### Configuración de Recursos
```yaml
services:
  stockchef-backend:
    deploy:
      resources:
        limits:
          cpus: '2.0'
          memory: 2G
        reservations:
          cpus: '0.5'
          memory: 512M
```

### Variables de Entorno Seguras
```bash
# .env.prod
DATABASE_TYPE=mysql
MYSQL_ROOT_PASSWORD_FILE=/run/secrets/mysql_root_password
MYSQL_PASSWORD_FILE=/run/secrets/mysql_password

# Usar secrets en lugar de variables directas
```

## 🔧 Optimizaciones JVM

### Configuración JVM Optimizada
```bash
# Para entornos de producción
JAVA_OPTS="-Xmx2048m -Xms1024m -XX:+UseG1GC -XX:G1HeapRegionSize=16m -XX:+UseStringDeduplication -XX:+OptimizeStringConcat"

# Para desarrollo
JAVA_OPTS="-Xmx1024m -Xms512m -XX:+UseZGC -XX:+UnlockExperimentalVMOptions"

# Para debugging
JAVA_OPTS="-Xmx1024m -Xms512m -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
```

## 📊 Monitoreo y Observabilidad

### Health Check Personalizado
```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8090/api/health"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 60s
```

### Logging Avanzado
```yaml
# docker-compose.logging.yml
version: '3.8'
services:
  stockchef-backend:
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
        tag: "stockchef-backend"
```

### Métricas con Prometheus
```yaml
# Agregar a docker-compose.yml
  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
```

## 🔐 Configuración de Seguridad

### SSL/TLS con Traefik
```yaml
# docker-compose.ssl.yml
  traefik:
    image: traefik:v2.8
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./traefik:/etc/traefik
      - /var/run/docker.sock:/var/run/docker.sock

  stockchef-backend:
    labels:
      - "traefik.enable=true"
      - "traefik.http.routers.stockchef.rule=Host(\`api.stockchef.com\`)"
      - "traefik.http.routers.stockchef.tls=true"
      - "traefik.http.routers.stockchef.tls.certresolver=letsencrypt"
```

### Variables de Entorno Seguras
```bash
# Usar Docker Secrets
echo "super_secret_password" | docker secret create mysql_root_password -
echo "jwt_secret_key_256_bits_long" | docker secret create jwt_secret -
```

## 🚀 Despliegue en Cloud

### AWS ECS Task Definition
```json
{
  "family": "stockchef-backend",
  "taskRoleArn": "arn:aws:iam::account:role/ecsTaskRole",
  "networkMode": "awsvpc",
  "containerDefinitions": [
    {
      "name": "stockchef-backend",
      "image": "stockchef/backend:latest",
      "memory": 2048,
      "cpu": 1024,
      "essential": true,
      "portMappings": [
        {
          "containerPort": 8090,
          "protocol": "tcp"
        }
      ]
    }
  ]
}
```

### Kubernetes Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: stockchef-backend
spec:
  replicas: 3
  selector:
    matchLabels:
      app: stockchef-backend
  template:
    metadata:
      labels:
        app: stockchef-backend
    spec:
      containers:
      - name: stockchef-backend
        image: stockchef/backend:latest
        ports:
        - containerPort: 8090
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: database-secret
              key: url
```

## 📈 Performance Tuning

### Configuración de Pool de Conexiones
```properties
# Configuración optimizada para alta carga
spring.datasource.hikari.maximum-pool-size=50
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.leak-detection-threshold=60000
```

### Cache Configuration
```properties
# Configuración de cache para producción
spring.cache.type=redis
spring.redis.host=redis-cluster
spring.redis.port=6379
spring.redis.timeout=2000ms
spring.redis.jedis.pool.max-active=20
spring.redis.jedis.pool.max-idle=10
```

## 🔄 CI/CD Pipeline

### GitHub Actions
```yaml
# .github/workflows/docker.yml
name: Docker Build and Deploy
on:
  push:
    branches: [ main ]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - name: Build and push Docker image
      uses: docker/build-push-action@v3
      with:
        context: .
        push: true
        tags: stockchef/backend:${{ github.sha }}
```

### Docker Multi-stage Build Optimizado
```dockerfile
# Dockerfile.prod
FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache curl
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

Para configuración básica, consulta el [README principal](README.md).