# 🐳 StockChef Backend - Configuration Docker Détaillée

Ce document contient la configuration avancée de Docker pour StockChef Backend. 

> 📚 **Note**: Pour un usage général, consultez le [README principal](README.md) qui inclut toute l'information nécessaire.

Ce projet inclut une configuration complète de Docker qui permet d'exécuter l'application StockChef avec support pour MySQL et PostgreSQL de manière flexible et professionnelle.

## 📋 Prérequis

- Docker Desktop installé et en cours d'exécution
- Docker Compose v2.0 ou supérieur
- Ports disponibles: 8090, 3306, 5432, 8080, 8081

## 🚀 Démarrage Rapide

### 1. Configuration initiale
```bash
# Copier le fichier de configuration
cp .env.example .env

# Éditer les configurations si nécessaire
# DATABASE_TYPE=mysql  (ou postgres)
```

### 2. Exécuter avec MySQL (recommandé)
```bash
# Utilisant le script PowerShell (Windows)
.\docker-manager.ps1 up mysql

# Utilisant le script bash (Linux/Mac)
./docker-manager.sh up mysql

# Ou directement avec docker-compose
docker-compose up -d mysql stockchef-backend
```

### 3. Exécuter avec PostgreSQL
```bash
# Utilisant le script PowerShell (Windows)
.\docker-manager.ps1 up postgres

# Utilisant le script bash (Linux/Mac)
./docker-manager.sh up postgres

# Ou directement avec docker-compose
docker-compose up -d postgres stockchef-backend
```

## 🛠️ Gestion des Services

### Scripts d'Administration

Les scripts `docker-manager.ps1` (Windows) et `docker-manager.sh` (Linux/Mac) fournissent une interface facile pour gérer l'environnement:

```bash
# Commandes principales
.\docker-manager.ps1 up mysql        # Démarrer avec MySQL
.\docker-manager.ps1 up postgres     # Démarrer avec PostgreSQL
.\docker-manager.ps1 down            # Arrêter les services
.\docker-manager.ps1 restart mysql   # Redémarrer avec MySQL
.\docker-manager.ps1 status          # Voir l'état des services
.\docker-manager.ps1 logs backend    # Voir les logs du backend
.\docker-manager.ps1 tools           # Démarrer les outils d'administration
.\docker-manager.ps1 clean           # Nettoyer les données (attention!)
.\docker-manager.ps1 build           # Reconstruire l'image
```

### Commandes Docker Compose Directes

```bash
# Démarrer les services
docker-compose up -d

# Voir les logs
docker-compose logs -f stockchef-backend
docker-compose logs -f mysql
docker-compose logs -f postgres

# Arrêter les services
docker-compose down

# Reconstruire l'image
docker-compose build --no-cache stockchef-backend
```

## 🌐 Ports et Services

| Service | Port | URL | Description |
|----------|--------|-----|-------------|
| Backend | 8090 | http://localhost:8090/api | API StockChef |
| MySQL | 3306 | localhost:3306 | Base de données MySQL |
| PostgreSQL | 5432 | localhost:5432 | Base de données PostgreSQL |
| phpMyAdmin | 8080 | http://localhost:8080 | Administration MySQL |
| pgAdmin | 8081 | http://localhost:8081 | Administration PostgreSQL |

## 🔍 Endpoints Importants

### Health Check
```bash
```bash
curl http://localhost:8090/api/health
```

### Status avec informations de base de données
```bash
curl http://localhost:8090/api/health/status
```

## 🗄️ Bases de Données

### Configuration MySQL
- **Host**: mysql (interne) / localhost:3306 (externe)
- **Base de données**: stockchef_db
- **Utilisateur**: stockchef_user
- **Mot de passe**: UserAdmin
- **Mot de passe Root**: UserAdmin

### Configuration PostgreSQL
- **Host**: postgres (interne) / localhost:5432 (externe)
- **Base de données**: stockchef_db
- **Utilisateur**: postgres
- **Mot de passe**: UserAdmin

## 🔧 Outils d'Administration

### phpMyAdmin (MySQL)
- **URL**: http://localhost:8080
- **Utilisateur**: root
- **Mot de passe**: UserAdmin

### pgAdmin (PostgreSQL)
- **URL**: http://localhost:8081
- **Email**: admin@stockchef.com
- **Mot de passe**: UserAdmin

Pour démarrer les outils:
```bash
.\docker-manager.ps1 tools
```

## 📁 Structure des Fichiers

```
├── docker-compose.yml          # Configuration principale de Docker Compose
├── Dockerfile                  # Image de l'application Spring Boot
├── .env.example               # Fichier de configuration d'exemple
├── .dockerignore              # Fichiers à ignorer dans le build
├── docker-manager.ps1         # Script de gestion pour Windows
├── docker-manager.sh          # Script de gestion pour Linux/Mac
├── docker/                    # Configurations spécifiques de Docker
│   ├── mysql/
│   │   └── init.sql          # Script d'initialisation MySQL
│   └── postgres/
│       └── init.sql          # Script d'initialisation PostgreSQL
└── src/main/resources/
    ├── application-docker-mysql.properties
    └── application-docker-postgres.properties
```

## 🔄 Changement entre Bases de Données

Pour passer de MySQL à PostgreSQL ou vice versa:

1. **Arrêter les services actuels**:
   ```bash
   .\docker-manager.ps1 down
   ```

2. **Démarrer avec la nouvelle base de données**:
   ```bash
   .\docker-manager.ps1 up postgres  # ou mysql
   ```

3. **Ou éditer .env et redémarrer**:
   ```bash
   # Changer DATABASE_TYPE dans .env
   .\docker-manager.ps1 restart
   ```

## 💾 Persistance des Données

Les données sont stockées dans des volumes Docker:
- `mysql_data`: Données MySQL
- `postgres_data`: Données PostgreSQL
- `pgadmin_data`: Configuration pgAdmin

Pour nettoyer toutes les données:
```bash
.\docker-manager.ps1 clean
```

## 🐛 Résolution de Problèmes

### Vérifier l'état des services
```bash
.\docker-manager.ps1 status
docker-compose ps
```

### Voir les logs détaillés
```bash
.\docker-manager.ps1 logs backend
.\docker-manager.ps1 logs mysql
.\docker-manager.ps1 logs postgres
```

### Reconstruire l'image si il y a des changements dans le code
```bash
.\docker-manager.ps1 build
```

### Problèmes de ports occupés
Si les ports sont occupés, vous pouvez modifier le fichier `.env`:
```bash
BACKEND_PORT=8091
MYSQL_PORT=3307
POSTGRES_PORT=5433
```

### Nettoyer le système Docker
```bash
docker system prune -a
docker volume prune
```

## 🔒 Sécurité

⚠️ **Important**: Les mots de passe par défaut sont pour le développement. Pour la production:

1. Changez tous les mots de passe dans `.env`
2. Utilisez des variables d'environnement sécurisées
3. Configurez des pare-feux appropriés
4. Utilisez HTTPS avec des certificats SSL

## 🚀 Déploiement en Production

Pour la production, considérez:

1. **Variables d'environnement sécurisées**
2. **Secrets Docker/Kubernetes**
3. **Réseaux privés**
4. **Sauvegarde automatique des bases de données**
5. **Monitoring et alertes**
6. **Load balancers**
7. **Certificats SSL/TLS**

## 📝 Notes

- Le backend se configure automatiquement selon la base de données sélectionnée
- Les health checks assurent que les bases de données soient prêtes avant de démarrer le backend
- Les outils d'administration sont optionnels et se démarrent avec le profil `tools`
- Les volumes persistent les données entre les redémarrages de conteneurs