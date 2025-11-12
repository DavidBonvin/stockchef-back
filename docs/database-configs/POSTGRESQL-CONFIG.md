# PostgreSQL Database Configuration Guide

## 📖 Descripción
PostgreSQL es un sistema de gestión de bases de datos objeto-relacional avanzado, conocido por su robustez, extensibilidad y cumplimiento de estándares SQL.

## ⚡ Ventajas
- ✅ **Muy robusto**: Excelente integridad de datos
- ✅ **Estándares SQL**: Cumple estrictamente con SQL
- ✅ **Extensible**: Soporte para tipos de datos complejos
- ✅ **Open Source**: Completamente gratuito

## ❌ Desventajas
- ⚠️ **Requiere Docker**: Necesita contenedor corriendo
- ⚠️ **Menos familiar**: Menor adopción que MySQL

## 🚀 Cómo usar

### Opción 1: Script automático
```powershell
.\start-backend.ps1
# Seleccionar opción 3
```

### Opción 2: Comando directo
```powershell
.\start-backend.ps1 -Database postgresql
```

### Opción 3: Maven directo
```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=postgresql
```

## 🐳 Configuración Docker

### 1. Crear contenedor PostgreSQL
```bash
docker run -d \
  --name stockchef-postgres \
  -p 5432:5432 \
  -e POSTGRES_PASSWORD=stockchef123 \
  -e POSTGRES_DB=stockchef_db \
  -e POSTGRES_USER=stockchef_user \
  postgres:15
```

### 2. Verificar que esté corriendo
```bash
docker ps | findstr postgres
```

### 3. Conectar manualmente (opcional)
```bash
docker exec -it stockchef-postgres psql -U stockchef_user -d stockchef_db
```

## 🔧 Configuración (application-postgresql.properties)

```properties
# PostgreSQL Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/stockchef_db
spring.datasource.username=stockchef_user
spring.datasource.password=stockchef123
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

# Connection Pool
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
```

## 🌐 URLs importantes

| Servicio | URL | Descripción |
|----------|-----|-------------|
| Backend API | http://localhost:8090/api | API principal |
| PostgreSQL Port | localhost:5432 | Puerto estándar |
| Auth Login | POST http://localhost:8090/api/auth/login | Endpoint de autenticación |

## 🔐 Credenciales

### API Authentication
```json
{
    "email": "developer@stockchef.com",
    "password": "devpass123"
}
```

### PostgreSQL Database
- **Host**: `localhost:5432`
- **Database**: `stockchef_db`
- **Usuario**: `stockchef_user`
- **Password**: `stockchef123`

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
El perfil PostgreSQL incluye identificación en los logs: `[POSTGRESQL-PROFILE]`

## 🔧 Comandos útiles de Docker

```bash
# Iniciar contenedor
docker start stockchef-postgres

# Parar contenedor
docker stop stockchef-postgres

# Ver logs del contenedor
docker logs stockchef-postgres

# Conectar a PostgreSQL
docker exec -it stockchef-postgres psql -U stockchef_user -d stockchef_db

# Eliminar contenedor (¡CUIDADO! Perderás datos)
docker rm -f stockchef-postgres
```

## 🗄️ SQL útiles

```sql
-- Ver bases de datos
\l

-- Conectar a base de datos
\c stockchef_db

-- Ver tablas
\dt

-- Ver usuarios en la tabla
SELECT * FROM users;

-- Describir tabla
\d users

-- Salir
\q
```

## ⚠️ Troubleshooting

### Error: "No PostgreSQL container found"
```bash
docker run -d --name stockchef-postgres -p 5432:5432 -e POSTGRES_PASSWORD=stockchef123 -e POSTGRES_DB=stockchef_db -e POSTGRES_USER=stockchef_user postgres:15
```

### Error: "Port already in use"
```bash
# Ver qué está usando el puerto 5432
netstat -ano | findstr :5432
# O cambiar puerto en application-postgresql.properties
```

### Error: "Connection refused"
Verificar que el contenedor esté corriendo:
```bash
docker ps | findstr postgres
```

## 🔄 Backup y Restore

### Backup
```bash
docker exec stockchef-postgres pg_dump -U stockchef_user stockchef_db > backup.sql
```

### Restore
```bash
docker exec -i stockchef-postgres psql -U stockchef_user -d stockchef_db < backup.sql
```

## 🚀 Comandos PostgreSQL avanzados

```sql
-- Ver conexiones activas
SELECT * FROM pg_stat_activity;

-- Ver tamaño de base de datos
SELECT pg_size_pretty(pg_database_size('stockchef_db'));

-- Ver información de tablas
SELECT schemaname,tablename,attname,typename,char_maximum_length 
FROM pg_catalog.pg_attribute a 
JOIN pg_catalog.pg_class c ON a.attrelid = c.oid 
JOIN pg_catalog.pg_namespace n ON c.relnamespace = n.oid 
JOIN information_schema.columns col ON col.column_name = a.attname 
JOIN pg_catalog.pg_type t ON a.atttypid = t.oid 
WHERE n.nspname = 'public' AND c.relkind = 'r' AND a.attnum > 0;
```

## 📋 Notas importantes
- PostgreSQL es case-sensitive en nombres
- Excelente para aplicaciones que requieren integridad de datos
- Soporte nativo para JSON y tipos complejos
- Ideal para aplicaciones empresariales