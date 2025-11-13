# 🚀 **Démarrage Rapide - StockChef Multi-Database**

## ⚡ **Démarrage Immédiat**

```powershell
# 1. Exécutez le script
.\start.ps1

# 2. Sélectionnez la base de données:
#    1 = H2 (rapide, sans Docker)
#    2 = MySQL (persistant, nécessite Docker)  
#    3 = PostgreSQL (robuste, nécessite Docker)

# 3. Prêt! Backend sur http://localhost:8090/api
```

## 🔧 **Si vous avez déjà MySQL/PostgreSQL**

### **MySQL existant:**
1. Éditez: `src/main/resources/application-mysql.properties`
2. Changez ces lignes:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:VOTRE_PORT/VOTRE_BD?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   spring.datasource.username=VOTRE_UTILISATEUR
   spring.datasource.password=VOTRE_MOT_DE_PASSE
   ```

### **PostgreSQL existant:**
1. Éditez: `src/main/resources/application-postgresql.properties`
2. Changez ces lignes:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:VOTRE_PORT/VOTRE_BD?sslmode=disable
   spring.datasource.username=VOTRE_UTILISATEUR
   spring.datasource.password=VOTRE_MOT_DE_PASSE
   ```

## 🧪 **Tests**

```bash
# Endpoint de test
POST http://localhost:8090/api/auth/login

# Identifiants
{
  "email": "developer@stockchef.com",
  "password": "devpass123"
}
```

## 📋 **Versions Compatibles**

| BD | Versión | Puerto |
|----|---------|--------|
| H2 | 2.3.232+ | N/A |
| MySQL | 8.4+ | 3307 |
| PostgreSQL | 15+ | 5432 |

## 🆘 **Problemas Comunes**

```powershell
# Java no encontrado
$env:JAVA_HOME = "C:\Program Files\Java\jdk-24"

# Puerto ocupado
netstat -ano | findstr :3307

# Limpiar Docker
docker system prune -f
```

**📖 Documentación completa:** `docs/MULTI-DATABASE-SETUP.md`