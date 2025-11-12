# MySQL Database Configuration Guide

## 📖 Descripción
MySQL es un sistema de gestión de bases de datos relacional robusto y popular. Perfecto para desarrollo y producción con persistencia de datos.

## ⚡ Ventajas
- ✅ **Persistente**: Los datos sobreviven a reinicios
- ✅ **Robusto**: Base de datos de calidad empresarial
- ✅ **Escalable**: Maneja grandes volúmenes de datos
- ✅ **Familiar**: Ampliamente conocido

## ❌ Desventajas
- ⚠️ **Requiere Docker**: Necesita contenedor corriendo
- ⚠️ **Configuración**: Más complejo que H2

## 🚀 Cómo usar

### Opción 1: Script automático
```powershell
.\start-backend.ps1
# Seleccionar opción 2
```

### Opción 2: Comando directo
```powershell
.\start-backend.ps1 -Database mysql
```

### Opción 3: Maven directo
```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

## 🐳 Configuración Docker

### 1. Crear contenedor MySQL
```bash
docker run -d \
  --name stockchef-mysql \
  -p 3307:3306 \
  -e MYSQL_ROOT_PASSWORD=UserAdmin \
  -e MYSQL_DATABASE=stockchef_db \
  -e MYSQL_USER=stockchef_user \
  -e MYSQL_PASSWORD=UserAdmin \
  mysql:8.4
```

### 2. Verificar que esté corriendo
```bash
docker ps | findstr mysql
```

### 3. Conectar manualmente (opcional)
```bash
docker exec -it stockchef-mysql mysql -u root -pUserAdmin
```

## 🔧 Configuración (application-mysql.properties)

```properties
# MySQL Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3307/stockchef_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=UserAdmin
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect

# Connection Pool
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
```

## 🌐 URLs importantes

| Servicio | URL | Descripción |
|----------|-----|-------------|
| Backend API | http://localhost:8090/api | API principal |
| MySQL Port | localhost:3307 | Puerto del contenedor |
| Auth Login | POST http://localhost:8090/api/auth/login | Endpoint de autenticación |

## 🔐 Credenciales

### API Authentication
```json
{
    "email": "developer@stockchef.com",
    "password": "devpass123"
}
```

### MySQL Database
- **Host**: `localhost:3307`
- **Database**: `stockchef_db`
- **Usuario**: `root`
- **Password**: `UserAdmin`

## 🧪 Testing con Thunder Client

```http
POST http://localhost:8090/api/auth/login
Content-Type: application/json

{
    "email": "developer@stockchef.com",
    "password": "devpass123"
}
```

## 📊 Logs importantes
El perfil MySQL incluye identificación en los logs: `[MYSQL-PROFILE]`

## 🔧 Comandos útiles de Docker

```bash
# Iniciar contenedor
docker start stockchef-mysql

# Parar contenedor
docker stop stockchef-mysql

# Ver logs del contenedor
docker logs stockchef-mysql

# Conectar a MySQL
docker exec -it stockchef-mysql mysql -u root -pUserAdmin

# Eliminar contenedor (¡CUIDADO! Perderás datos)
docker rm -f stockchef-mysql
```

## 🗄️ SQL útiles

```sql
-- Ver bases de datos
SHOW DATABASES;

-- Usar la base de datos
USE stockchef_db;

-- Ver tablas
SHOW TABLES;

-- Ver usuarios
SELECT * FROM users;

-- Ver estructura de tabla
DESCRIBE users;
```

## ⚠️ Troubleshooting

### Error: "No MySQL container found"
```bash
docker run -d --name stockchef-mysql -p 3307:3306 -e MYSQL_ROOT_PASSWORD=UserAdmin -e MYSQL_DATABASE=stockchef_db mysql:8.4
```

### Error: "Port already in use"
```bash
# Cambiar puerto en application-mysql.properties o parar el servicio que usa 3307
netstat -ano | findstr :3307
```

### Error: "Access denied for user"
Verificar credenciales en `application-mysql.properties`

## 🔄 Backup y Restore

### Backup
```bash
docker exec stockchef-mysql mysqldump -u root -pUserAdmin stockchef_db > backup.sql
```

### Restore
```bash
docker exec -i stockchef-mysql mysql -u root -pUserAdmin stockchef_db < backup.sql
```