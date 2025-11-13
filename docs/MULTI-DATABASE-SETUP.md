# 📚 **Guide Complet de Connexion Multi-Base de Données - StockChef Backend**

Cette guide vous permettra de configurer et d'exécuter le backend de StockChef avec **H2**, **MySQL** ou **PostgreSQL** selon vos besoins de développement ou de production.

## 🎯 **Sommaire**

- [1. Résumé Exécutif](#1-résumé-exécutif)
- [2. H2 Database (Développement Rapide)](#2-h2-database-développement-rapide)
- [3. MySQL (Production)](#3-mysql-production)
- [4. PostgreSQL (Production)](#4-postgresql-production)
- [5. Configuration de Profils Existants](#5-configuration-de-profils-existants)
- [6. Script de Démarrage](#6-script-de-démarrage)
- [7. Dépannage](#7-dépannage)

---

## 1. **Résumé Exécutif**

### ✅ **Versions Compatibles Testées**

| Base de Données | Version | Type | Port | Utilisation Recommandée |
|-----------------|---------|------|------|--------------------------|
| **H2** | 2.3.232+ | En mémoire | N/A | Développement rapide |
| **MySQL** | 8.4+ | Conteneur Docker | 3307 | Production, persistance |
| **PostgreSQL** | 15+ | Conteneur Docker | 5432 | Production avancée |

### 🛠️ **Prérequis du Système**

- **Java**: 24.0.1+ (JDK)
- **Maven**: 3.9.11+
- **Docker**: Para MySQL y PostgreSQL
- **Spring Boot**: 3.5.0

---

## 2. **H2 Database (Développement Rapide)**

### 📋 **Caractéristiques**
- ✅ **Aucune configuration supplémentaire**
- ✅ **Démarrage immédiat**
- ✅ **Console web intégrée**
- ⚠️ **Données NON persistantes**

### 🚀 **Démarrage Rapide**

```powershell
# Option 1: Script interactif
.\start.ps1
# Sélectionner: 1

# Option 2: Direct
.\start.ps1 -Database h2

# Option 3: Maven direct
$env:SPRING_PROFILES_ACTIVE = "h2"; mvn spring-boot:run
```

### ⚙️ **Configuración (application-h2.properties)**

```properties
# H2 Database - En Mémoire
spring.datasource.url=jdbc:h2:mem:stockchef;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver

# Configuration JPA
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.show-sql=true

# Console Web H2
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

### 🌐 **URLs d'Accès**

| Service | URL | Identifiants |
|---------|-----|---------------|
| API Backend | http://localhost:8090/api | N/A |
| H2 Console | http://localhost:8090/api/h2-console | Utilisateur: `sa`, Mot de passe: (vide) |
| JDBC URL | jdbc:h2:mem:stockchef | Pour H2 Console |

---

## 3. **MySQL (Production)**

### 📋 **Caractéristiques**
- ✅ **Données persistantes**
- ✅ **Haute performance**
- ✅ **Largement supporté**
- ⚠️ **Nécessite Docker**

### 🐳 **Configuration Docker**

#### **Si vous N'avez PAS MySQL installé:**
```bash
# Créer conteneur MySQL
docker run -d \
  --name stockchef-mysql \
  -p 3307:3306 \
  -e MYSQL_ROOT_PASSWORD=UserAdmin \
  -e MYSQL_DATABASE=stockchef_db \
  -e MYSQL_USER=stockchef_user \
  -e MYSQL_PASSWORD=UserAdmin \
  mysql:8.4
```

#### **Si vous avez DÉJÀ MySQL installé/configuré:**

**📍 Localisez votre configuration dans:** `src/main/resources/application-mysql.properties`

```properties
# METTEZ À JOUR CES LIGNES AVEC VOS DONNÉES:
spring.datasource.url=jdbc:mysql://localhost:VOTREPORT/VOTREBASEDEDONNEES?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=VOTREUTILISATEUR
spring.datasource.password=VOTREMOTDEPASSE
```

**🔧 Exemple avec profil existant:**
```properties
# Si vous avez MySQL sur le port 3306 avec l'utilisateur root
spring.datasource.url=jdbc:mysql://localhost:3306/stockchef_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=VOTRE_MOT_DE_PASSE_ICI
```

### 🚀 **Démarrage avec MySQL**

```powershell
# Vérifier que MySQL fonctionne
docker ps | findstr mysql

# Démarrer backend
.\start.ps1 -Database mysql
```

### 📊 **Vérification de Connexion**

```bash
# Se connecter manuellement pour vérifier
docker exec -it stockchef-mysql mysql -u root -pUserAdmin -e "SHOW DATABASES;"

# Doit afficher: stockchef_db
```

---

## 4. **PostgreSQL (Production)**

### 📋 **Caractéristiques**
- ✅ **Très robuste**
- ✅ **Standards SQL stricts**
- ✅ **Types de données avancés**
- ⚠️ **Nécessite Docker**

### 🐳 **Configuration Docker**

#### **Si vous N'avez PAS PostgreSQL installé:**
```bash
# Créer conteneur PostgreSQL
docker run -d \
  --name stockchef-postgres \
  -p 5432:5432 \
  -e POSTGRES_PASSWORD=UserAdmin \
  postgres:15
```

**⚠️ IMPORTANT**: Avec cette configuration, l'utilisateur est `postgres` (non personnalisé).

#### **Si vous avez DÉJÀ PostgreSQL installé/configuré:**

**📍 Localisez votre configuration dans:** `src/main/resources/application-postgresql.properties`

```properties
# METTEZ À JOUR CES LIGNES AVEC VOS DONNÉES:
spring.datasource.url=jdbc:postgresql://localhost:VOTREPORT/VOTREBASEDEDONNEES?sslmode=disable
spring.datasource.username=VOTREUTILISATEUR
spring.datasource.password=VOTREMOTDEPASSE
```

**🔧 Exemple avec profil existant:**
```properties
# Si vous avez PostgreSQL local avec utilisateur personnalisé
spring.datasource.url=jdbc:postgresql://localhost:5432/ma_base_de_donnees?sslmode=disable
spring.datasource.username=mon_utilisateur
spring.datasource.password=mon_mot_de_passe
```

### 🚀 **Démarrage avec PostgreSQL**

```powershell
# Vérifier que PostgreSQL fonctionne
docker ps | findstr postgres

# Créer base de données (si elle n'existe pas)
docker exec stockchef-postgres psql -U postgres -c "CREATE DATABASE stockchef_db;"

# Démarrer backend
.\start.ps1 -Database postgresql
```

### 📊 **Vérification de Connexion**

```bash
# Se connecter manuellement pour vérifier
docker exec stockchef-postgres psql -U postgres -c "\l"

# Doit afficher: stockchef_db
```

---

## 5. **Configuration de Profils Existants**

### 🎯 **Si vous avez déjà MySQL/PostgreSQL configuré**

#### **Étape 1: Identifiez votre configuration**

**Pour MySQL:**
```bash
# Trouvez votre port et identifiants
mysql -u root -p -e "SELECT @@port;"
```

**Pour PostgreSQL:**
```bash
# Trouvez votre port et identifiants
psql -U postgres -c "SHOW port;"
```

#### **Étape 2: Mettez à jour le fichier correspondant**

**📁 Emplacement des fichiers:**
- H2: `src/main/resources/application-h2.properties`
- MySQL: `src/main/resources/application-mysql.properties`  
- PostgreSQL: `src/main/resources/application-postgresql.properties`

#### **Étape 3: Paramètres clés à modifier**

```properties
# MYSQL - Paramètres principaux
spring.datasource.url=jdbc:mysql://VOTRE_HOTE:VOTRE_PORT/VOTRE_BASE_DONNEES?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=VOTRE_UTILISATEUR
spring.datasource.password=VOTRE_MOT_DE_PASSE

# POSTGRESQL - Paramètres principaux
spring.datasource.url=jdbc:postgresql://VOTRE_HOTE:VOTRE_PORT/VOTRE_BASE_DONNEES?sslmode=disable
spring.datasource.username=VOTRE_UTILISATEUR  
spring.datasource.password=VOTRE_MOT_DE_PASSE
```

#### **Étape 4: Créer la base de données (si elle n'existe pas)**

**MySQL:**
```sql
CREATE DATABASE stockchef_db;
```

**PostgreSQL:**
```sql
CREATE DATABASE stockchef_db;
```

---

## 6. **Script de Démarrage**

### 🎮 **Utilisation du Script**

```powershell
# Mode interactif (recommandé)
.\start.ps1

# Mode direct
.\start.ps1 -Database h2          # H2
.\start.ps1 -Database mysql       # MySQL
.\start.ps1 -Database postgresql  # PostgreSQL
```

### ⚙️ **Ce que fait automatiquement le script:**

1. ✅ **Vérifie les dépendances** (conteneurs Docker)
2. ✅ **Crée les conteneurs** s'ils n'existent pas
3. ✅ **Configure JAVA_HOME** automatiquement
4. ✅ **Sélectionne le profil** correct
5. ✅ **Démarre le backend** avec la BD sélectionnée
6. ✅ **Initialise les données** de test

---

## 7. **Dépannage**

### 🚨 **Problèmes Courants**

#### **Erreur: "JAVA_HOME not found"**
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-24"
```

#### **Erreur: "MySQL container not found"**
```bash
docker run -d --name stockchef-mysql -p 3307:3306 -e MYSQL_ROOT_PASSWORD=UserAdmin -e MYSQL_DATABASE=stockchef_db mysql:8.4
```

#### **Erreur: "PostgreSQL authentication failed"**
- ✅ Vérifiez que vous utilisez l'utilisateur `postgres` (non personnalisé)
- ✅ Vérifiez le mot de passe `UserAdmin`
- ✅ Vérifiez que la base `stockchef_db` existe

#### **Erreur: "Port already in use"**
```bash
# Voir ce qui utilise le port
netstat -ano | findstr :3307  # MySQL
netstat -ano | findstr :5432  # PostgreSQL
```

### 🔧 **Commandes Utiles**

```bash
# Voir les conteneurs
docker ps

# Arrêter tous les conteneurs StockChef
docker stop stockchef-mysql stockchef-postgres

# Supprimer les conteneurs
docker rm stockchef-mysql stockchef-postgres

# Nettoyer le système Docker
docker system prune -f
```

---

## 🎉 **Test Final**

### 🧪 **Identifiants de Test (toutes les BDs)**

```json
{
    "email": "developer@stockchef.com",
    "password": "devpass123"
}
```

### 🌐 **Endpoint de Connexion**

```http
POST http://localhost:8090/api/auth/login
Content-Type: application/json

{
    "email": "developer@stockchef.com",
    "password": "devpass123"
}
```

### ✅ **Réponse Attendue**

```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
        "email": "developer@stockchef.com", 
        "role": "ROLE_DEVELOPER"
    }
}
```

---

## 📋 **Résumé des Fichiers**

| Fichier | Objectif |
|---------|----------|
| `start.ps1` | Script de démarrage interactif |
| `application.properties` | Configuration de base |
| `application-h2.properties` | Profil H2 |
| `application-mysql.properties` | Profil MySQL | 
| `application-postgresql.properties` | Profil PostgreSQL |

---

**🎯 Avec cette configuration vous avez un système complètement fonctionnel pour le développement et la production !** 

Le système détecte automatiquement les dépendances et vous guide étape par étape pour une connexion réussie. ✨