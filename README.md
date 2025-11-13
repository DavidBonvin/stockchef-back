# 🍽️ StockChef Backend

![Java](https://img.shields.io/badge/Java-21-orange?style=flat&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen?style=flat&logo=spring)
![Docker](https://img.shields.io/badge/Docker-Ready-blue?style=flat&logo=docker)
![MySQL](https://img.shields.io/badge/MySQL-8.4-orange?style=flat&logo=mysql)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-blue?style=flat&logo=postgresql)

API REST Backend pour StockChef - Système de gestion d'inventaire de cuisine professionnel avec support multi-base de données et conteneurisation Docker complète.

## 📋 Table des Matières

- [🎯 Fonctionnalités](#-fonctionnalités)
- [🛠️ Prérequis](#️-prérequis)
- [🚀 Installation et Configuration](#-installation-et-configuration)
- [🐳 Utilisation avec Docker](#-utilisation-avec-docker)
- [🌐 Services et Ports](#-services-et-ports)
- [📁 Structure du Projet](#-structure-du-projet)
- [⚙️ Configuration](#️-configuration)
- [🧪 Tests](#-tests)
- [📚 API Endpoints](#-api-endpoints)
- [🔧 Développement](#-développement)
- [🚀 Déploiement en Production](#-déploiement-en-production)
- [❓ Résolution de Problèmes](#-résolution-de-problèmes)

## 🎯 Fonctionnalités

- ✅ **Multi-Base de Données**: Support pour MySQL et PostgreSQL avec changement dynamique
- ✅ **Conteneurisation Complète**: Docker Compose avec tous les services
- ✅ **Scripts de Gestion**: Outils automatisés pour Windows et Linux
- ✅ **Outils d'Administration**: phpMyAdmin et pgAdmin inclus
- ✅ **Health Checks**: Surveillance automatique des services
- ✅ **Configuration par Profils**: Développement, test et production
- ✅ **Sécurité**: Spring Security avec JWT préparé
- ✅ **Architecture Professionnelle**: Structure de packages d'entreprise

## 🛠️ Prérequis

### Prérequis Minimums
- **Docker Desktop** 4.0+ avec Docker Compose v2.0+
- **Git** pour cloner le dépôt
- **4GB RAM** minimum disponible pour Docker
- **Ports disponibles**: 8090, 3307, 5433, 8080, 8081

### Prérequis pour Développement Local (Optionnel)
- **Java JDK** 21 ou 24
- **Maven** 3.9+
- **MySQL** 8.4+ (si vous n'utilisez pas Docker)
- **PostgreSQL** 18+ (si vous n'utilisez pas Docker) - [Voir guide d'installation](#-installation-postgresql-pour-développement-local)

## 🚀 Installation et Configuration

### 1. Cloner le Dépôt
```bash
git clone https://github.com/DavidBonvin/stockchef-back.git
cd stockchef-back
```

### 2. Vérifier Docker
```bash
# Vérifier que Docker est en cours d'exécution
docker --version
docker-compose --version

# Doit afficher des versions similaires à:
# Docker version 27.4.0
# Docker Compose version v2.30.3
```

### 3. Configurer les Variables d'Environnement
```bash
# Copier le fichier de configuration
cp .env.example .env

# Éditer la configuration (optionnel)
# DATABASE_TYPE=mysql  # ou postgres
```

### 📥 Installation PostgreSQL pour Développement Local

Si vous prévoyez de développer sans Docker et utiliser PostgreSQL local, suivez ce guide d'installation:

#### Composants Recommandés à Installer

##### ✅ **OBLIGATOIRES - Toujours Cocher**
- ✅ **PostgreSQL (64 bit) v18.0-2** - Le serveur de base de données principal
- ✅ **pgAgent (64 bit) for PostgreSQL 18 v4.2.3-1** - Pour les tâches programmées et la maintenance

##### ✅ **RECOMMANDÉS - Très Utiles**
- ✅ **pgJDBC v42.7.2-1** - Driver JDBC pour Java/Spring Boot (sauvegarde locale)
- ✅ **psqlODBC (64 bit) v13.02.0000-1** - Pour la connectivité avec les outils externes

##### 🎯 **OPTIONNELS - Selon les Besoins**
- 🎯 **PostGIS 3.6 Bundle for PostgreSQL 18** - Seulement si vous gérez des données géographiques/localisations

##### ❌ **NON NÉCESSAIRES pour StockChef**
- ❌ **Npgsql** - Driver pour .NET (pas Java)
- ❌ **psqlODBC (32 bit)** - Version 32-bit inutile
- ❌ **PostgreSQL versions antérieures** (v13-v17) - Vous avez déjà v18
- ❌ **Migration Toolkit** - Pour les migrations d'entreprise complexes
- ❌ **Enterprise Manager** - Outils d'entreprise payants
- ❌ **pgBouncer** - Pool de connexions (Docker le gère déjà)
- ❌ **PEM-HTTPD** - Serveur web pour Enterprise Manager

#### Installation Minimale pour Docker
```
✅ Database Server
   └── PostgreSQL (64 bit) v18.0-2
```

> 💡 **Note**: Si vous utilisez principalement Docker, vous n'avez besoin que de PostgreSQL 18. Les drivers et outils sont gérés automatiquement par Docker.

#### Vérifier l'Installation
```bash
# Vérifier la version
psql --version

# Créer une base de données pour le développement
createdb -U postgres stockchef_db
```

## 🐳 Utilisation avec Docker

### Démarrage Rapide

#### Avec MySQL (Recommandé)
```powershell
# Windows PowerShell
.\docker-manager.ps1 up mysql
```

```bash
# Linux/Mac
./docker-manager.sh up mysql
```

#### Avec PostgreSQL
```powershell
# Windows PowerShell
.\docker-manager.ps1 up postgres
```

```bash
# Linux/Mac
./docker-manager.sh up postgres
```

### Commandes de Gestion

| Commande | Description |
|---------|-------------|
| `.\docker-manager.ps1 up mysql` | Démarrer avec MySQL |
| `.\docker-manager.ps1 up postgres` | Démarrer avec PostgreSQL |
| `.\docker-manager.ps1 down` | Arrêter tous les services |
| `.\docker-manager.ps1 status` | Voir l'état des services |
| `.\docker-manager.ps1 logs backend` | Voir les logs du backend |
| `.\docker-manager.ps1 tools` | Démarrer les outils d'administration |
| `.\docker-manager.ps1 clean` | Nettoyer les données (⚠️ destructif) |
| `.\docker-manager.ps1 build` | Reconstruire l'image |
| `.\docker-manager.ps1 help` | Afficher l'aide complète |

### Vérifier le Fonctionnement
```bash
# Tester l'endpoint de santé
curl http://localhost:8090/api/health

# Réponse attendue:
# {"service":"stockchef-back","message":"StockChef Backend fonctionne correctement","version":"0.0.1-SNAPSHOT","status":"UP","timestamp":"..."}
```

## 🌐 Services et Ports

| Service | URL/Host | Port | Identifiants | Description |
|----------|----------|--------|--------------|-------------|
| **Backend API** | http://localhost:8090 | 8090 | - | API REST principale |
| **MySQL** | localhost:3307 | 3307 | `root` / `UserAdmin` | Base de données MySQL |
| **PostgreSQL** | localhost:5433 | 5433 | `postgres` / `UserAdmin` | Base de données PostgreSQL |
| **phpMyAdmin** | http://localhost:8080 | 8080 | `root` / `UserAdmin` | Administration web pour MySQL |
| **pgAdmin** | http://localhost:8081 | 8081 | `admin@stockchef.com` / `UserAdmin` | Administration web pour PostgreSQL |

> 💡 **Outils PostgreSQL**: Si vous avez installé PostgreSQL local avec pgAgent, vous avez aussi accès à `psql` depuis la ligne de commande et pouvez connecter des outils comme pgAdmin desktop.

### Endpoints API Principaux

| Endpoint | Méthode | Description |
|----------|--------|-------------|
| `/api/health` | GET | Health check du service |
| `/api/health/status` | GET | État détaillé avec info de BD |

## 📁 Structure du Projet

```
📦 stockchef-back/
├── 📁 .mvn/                          # Maven wrapper
├── 📁 docker/                        # Configuration Docker
│   ├── 📁 mysql/
│   │   └── init.sql                  # Script d'initialisation MySQL
│   └── 📁 postgres/
│       └── init.sql                  # Script d'initialisation PostgreSQL
├── 📁 src/
│   ├── 📁 main/
│   │   ├── 📁 java/com/stockchef/stockchefback/
│   │   │   ├── 📁 config/            # Configurations Spring
│   │   │   ├── 📁 controller/        # Contrôleurs REST
│   │   │   ├── 📁 dto/              # Data Transfer Objects
│   │   │   ├── 📁 exception/        # Gestion des exceptions
│   │   │   ├── 📁 model/            # Entités JPA
│   │   │   ├── 📁 repository/       # Dépôts de données
│   │   │   ├── 📁 security/         # Configuration sécurité
│   │   │   ├── 📁 service/          # Logique métier
│   │   │   └── StockchefBackApplication.java
│   │   └── 📁 resources/
│   │       ├── application.properties              # Configuration principale
│   │       ├── application-dev.properties          # Développement sans BD
│   │       ├── application-mysql.properties        # Configuration MySQL local
│   │       ├── application-postgres.properties     # Configuration PostgreSQL local
│   │       ├── application-docker-mysql.properties # Configuration MySQL Docker
│   │       └── application-docker-postgres.properties # Configuration PostgreSQL Docker
│   └── 📁 test/                     # Tests unitaires
├── 🐳 Dockerfile                     # Image Docker du backend
├── 🐳 docker-compose.yml             # Orchestration des services
├── 🔧 docker-manager.ps1             # Script de gestion Windows
├── 🔧 docker-manager.sh              # Script de gestion Linux/Mac
├── ⚙️ .env                           # Variables d'environnement
├── ⚙️ .env.example                   # Exemple de configuration
├── 📄 pom.xml                        # Configuration Maven
├── 📚 README-Docker.md               # Documentation Docker détaillée
└── 📚 PROJET-TERMINE.md              # Résumé du projet
```

## ⚙️ Configuration

### Variables d'Environnement (.env)

```bash
# Type de base de données (mysql ou postgres)
DATABASE_TYPE=mysql

# Profil Spring actif
SPRING_PROFILES_ACTIVE=mysql

# Configuration JVM
JAVA_OPTS=-Xmx1024m -Xms512m

# Configuration MySQL
MYSQL_ROOT_PASSWORD=UserAdmin
MYSQL_DATABASE=stockchef_db
MYSQL_USER=stockchef_user
MYSQL_PASSWORD=UserAdmin

# Configuración PostgreSQL
POSTGRES_DB=stockchef_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=UserAdmin

# Configuración herramientas admin
PGADMIN_DEFAULT_EMAIL=admin@stockchef.com
PGADMIN_DEFAULT_PASSWORD=UserAdmin
```

### Perfiles Spring Boot

| Perfil | Descripción | Base de Datos |
|--------|-------------|---------------|
| `dev` | Desarrollo sin BD | H2 en memoria |
| `mysql` | MySQL local | localhost:3306 |
| `postgres` | PostgreSQL local | localhost:5432 |
| `docker-mysql` | MySQL Docker | mysql:3306 |
| `docker-postgres` | PostgreSQL Docker | postgres:5432 |

## 🧪 Testing

### Ejecutar Tests
```bash
# Tests unitarios
mvn test

# Tests con perfil específico
mvn test -Dspring.profiles.active=dev

# Con Docker
docker-compose exec stockchef-backend mvn test
```

### Test de Conectividad
```bash
# Health check
curl http://localhost:8090/api/health

# Con PowerShell
Invoke-RestMethod -Uri "http://localhost:8090/api/health" -Method Get
```

## 📚 API Endpoints

### Health & Status

#### GET /api/health
```bash
curl http://localhost:8090/api/health
```

**Respuesta:**
```json
{
  "service": "stockchef-back",
  "message": "StockChef Backend está funcionando correctamente",
  "version": "0.0.1-SNAPSHOT",
  "status": "UP",
  "timestamp": "2025-11-11T10:30:45.123456789"
}
```

#### GET /api/health/status
```bash
curl http://localhost:8090/api/health/status
```

**Respuesta:**
```json
{
  "status": "UP",
  "database": "connected",
  "database_type": "mysql",
  "version": "0.0.1-SNAPSHOT"
}
```

## 🔧 Desarrollo

### Desarrollo Local sin Docker

1. **Instalar dependencias**:
   - Java JDK 21+
   - Maven 3.9+
   - MySQL 8.4+ o PostgreSQL 18+

2. **Configurar base de datos**:
   ```sql
   -- MySQL
   CREATE DATABASE stockchef_db;
   CREATE USER 'stockchef_user'@'localhost' IDENTIFIED BY 'UserAdmin';
   GRANT ALL PRIVILEGES ON stockchef_db.* TO 'stockchef_user'@'localhost';
   
   -- PostgreSQL (con las herramientas instaladas)
   createdb -U postgres stockchef_db
   # O usando psql:
   # psql -U postgres -c "CREATE DATABASE stockchef_db;"
   ```

3. **Ejecutar aplicación**:
   ```bash
   # Con MySQL
   mvn spring-boot:run -Dspring-boot.run.profiles=mysql
   
   # Con PostgreSQL
   mvn spring-boot:run -Dspring-boot.run.profiles=postgres
   
   # Sin BD (H2)
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

### Desarrollo con Docker

```bash
# Modo desarrollo con recarga automática
.\docker-manager.ps1 up mysql
.\docker-manager.ps1 logs backend -f

# Reconstruir imagen tras cambios
.\docker-manager.ps1 build
.\docker-manager.ps1 restart mysql
```

### Cambiar entre Bases de Datos

```bash
# Cambiar a PostgreSQL
.\docker-manager.ps1 down
.\docker-manager.ps1 up postgres

# Cambiar a MySQL
.\docker-manager.ps1 down
.\docker-manager.ps1 up mysql
```

## 🚀 Despliegue

### Despliegue en Producción

1. **Preparar variables de entorno**:
   ```bash
   # Crear .env de producción
   DATABASE_TYPE=mysql
   SPRING_PROFILES_ACTIVE=docker-mysql
   JAVA_OPTS=-Xmx2048m -Xms1024m
   
   # Cambiar contraseñas por seguridad
   MYSQL_ROOT_PASSWORD=tu_password_seguro
   MYSQL_PASSWORD=tu_password_seguro
   ```

2. **Desplegar servicios**:
   ```bash
   docker-compose up -d mysql stockchef-backend
   ```

3. **Configurar proxy reverso** (Nginx ejemplo):
   ```nginx
   server {
       listen 80;
       server_name tu-dominio.com;
       
       location /api/ {
           proxy_pass http://localhost:8090;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
       }
   }
   ```

### Docker Swarm / Kubernetes

El proyecto está preparado para orquestadores:
- Variables de entorno externalizadas
- Health checks configurados
- Volúmenes persistentes definidos
- Redes privadas implementadas

## ❓ Solución de Problemas

### Problemas Comunes

#### 🔴 Puerto ocupado
```
Error: bind: address already in use
```
**Solución**: Cambiar puertos en `.env` o parar servicios conflictivos:
```bash
# Verificar qué usa el puerto
netstat -tulpn | grep :8090

# Cambiar puerto en .env
BACKEND_PORT=8091
```

#### 🔴 Docker no responde
```
Cannot connect to the Docker daemon
```
**Solución**: 
1. Verificar que Docker Desktop esté ejecutándose
2. Reiniciar Docker Desktop
3. Verificar permisos de usuario

#### 🔴 Base de datos no conecta
```
Unable to connect to database
```
**Solución**:
1. Verificar que el contenedor de BD esté healthy:
   ```bash
   .\docker-manager.ps1 status
   ```
2. Revisar logs:
   ```bash
   .\docker-manager.ps1 logs mysql  # o postgres
   ```
3. Para PostgreSQL local, verificar servicio:
   ```bash
   # Windows
   Get-Service postgresql*
   
   # Verificar conexión manual
   psql -U postgres -d stockchef_db -c "SELECT 1;"
   ```
4. Reiniciar servicios:
   ```bash
   .\docker-manager.ps1 restart mysql
   ```

#### 🔴 Imagen no se construye
```
Build failed
```
**Solución**:
1. Limpiar caché de Docker:
   ```bash
   docker system prune -a
   ```
2. Reconstruir sin caché:
   ```bash
   .\docker-manager.ps1 build --no-cache
   ```

### Logs y Diagnóstico

```bash
# Ver todos los logs
.\docker-manager.ps1 logs all

# Ver logs específicos
.\docker-manager.ps1 logs backend
.\docker-manager.ps1 logs mysql
.\docker-manager.ps1 logs postgres

# Modo seguimiento en tiempo real
docker-compose logs -f stockchef-backend
```

### Limpiar el Entorno

```bash
# Parar servicios
.\docker-manager.ps1 down

# Limpiar datos (⚠️ Destructivo)
.\docker-manager.ps1 clean

# Limpiar sistema Docker completo
docker system prune -a
docker volume prune
```

## � Déploiement en Production

### 🌐 Production sur Railway

**StockChef Backend est actuellement déployé en production sur Railway !**

- **🔗 URL Production** : `https://stockchef-back-production.up.railway.app`
- **📊 Status** : ✅ Opérationnel
- **🔐 Authentification** : JWT activé
- **💾 Base de données** : H2 (prêt pour PostgreSQL)

#### 📖 Documentation complète

**👉 [Voir la documentation détaillée de déploiement Railway](docs/PRODUCTION-RAILWAY.md)**

Cette documentation contient :
- ✅ Guide complet de déploiement Railway
- ✅ Configuration des variables d'environnement
- ✅ Tests d'authentification en production
- ✅ Gestion sécurisée des branches
- ✅ Monitoring et surveillance
- ✅ Fonctionnalités avancées

#### ⚠️ **ATTENTION - Gestion des Branches**

> 🚨 **IMPORTANT** 🚨
> 
> **La branche `main` est connectée à la PRODUCTION !**
> 
> - ✅ `main` → Déploiement automatique en production
> - ✅ Utilisez des branches séparées pour le développement
> - ✅ Testez localement avant de merger vers `main`

```bash
# ❌ Ne pas développer directement sur main
git checkout main

# ✅ Créer une branche pour développer
git checkout -b feature/ma-nouvelle-fonctionnalite
# ... développer et tester ...
git commit -m "feat: nouvelle fonctionnalité"

# ✅ Seulement après validation complète
git checkout main
git merge feature/ma-nouvelle-fonctionnalite
git push origin main  # 🚀 Déploiement automatique !
```

#### 🧪 Test rapide en production

```bash
# Vérification santé
curl https://stockchef-back-production.up.railway.app/api/health

# Test authentification
curl -X POST https://stockchef-back-production.up.railway.app/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"developer@stockchef.com","password":"devpass123"}'
```

---

## �📧 Soporte

- **Repositorio**: [https://github.com/DavidBonvin/stockchef-back](https://github.com/DavidBonvin/stockchef-back)
- **Issues**: [Reportar problemas](https://github.com/DavidBonvin/stockchef-back/issues)
- **Wiki**: [Documentación extendida](https://github.com/DavidBonvin/stockchef-back/wiki)

## 📄 Licencia

Este proyecto está bajo la licencia MIT. Ver archivo `LICENSE` para más detalles.

---

**🍽️ StockChef Backend** - Desarrollado con ❤️ para gestión profesional de inventario de cocina.