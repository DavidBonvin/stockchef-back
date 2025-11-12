# 🚀 **Quick Start - StockChef Multi-Database**

## ⚡ **Inicio Inmediato**

```powershell
# 1. Ejecuta el script
.\start.ps1

# 2. Selecciona base de datos:
#    1 = H2 (rápido, sin Docker)
#    2 = MySQL (persistente, requiere Docker)  
#    3 = PostgreSQL (robusto, requiere Docker)

# 3. ¡Listo! Backend en http://localhost:8090/api
```

## 🔧 **Si ya tienes MySQL/PostgreSQL**

### **MySQL existente:**
1. Edita: `src/main/resources/application-mysql.properties`
2. Cambia estas líneas:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:TU_PUERTO/TU_BD?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   spring.datasource.username=TU_USUARIO
   spring.datasource.password=TU_CONTRASEÑA
   ```

### **PostgreSQL existente:**
1. Edita: `src/main/resources/application-postgresql.properties`
2. Cambia estas líneas:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:TU_PUERTO/TU_BD?sslmode=disable
   spring.datasource.username=TU_USUARIO
   spring.datasource.password=TU_CONTRASEÑA
   ```

## 🧪 **Testing**

```bash
# Endpoint de prueba
POST http://localhost:8090/api/auth/login

# Credenciales
{
  "email": "developer@stockchef.com",
  "password": "devpass123"
}
```

## 📋 **Versiones Compatibles**

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