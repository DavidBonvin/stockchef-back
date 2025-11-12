# 📚 **Guía Completa de Conexión Multi-Base de Datos - StockChef Backend**

Esta guía te permitirá configurar y ejecutar el backend de StockChef con **H2**, **MySQL** o **PostgreSQL** según tus necesidades de desarrollo o producción.

## 🎯 **Índice**

- [1. Resumen Ejecutivo](#1-resumen-ejecutivo)
- [2. H2 Database (Desarrollo Rápido)](#2-h2-database-desarrollo-rápido)
- [3. MySQL (Producción)](#3-mysql-producción)
- [4. PostgreSQL (Producción)](#4-postgresql-producción)
- [5. Configuración de Perfiles Existentes](#5-configuración-de-perfiles-existentes)
- [6. Script de Inicio](#6-script-de-inicio)
- [7. Troubleshooting](#7-troubleshooting)

---

## 1. **Resumen Ejecutivo**

### ✅ **Versiones Compatibles Probadas**

| Base de Datos | Versión | Tipo | Puerto | Uso Recomendado |
|---------------|---------|------|--------|------------------|
| **H2** | 2.3.232+ | En memoria | N/A | Desarrollo rápido |
| **MySQL** | 8.4+ | Contenedor Docker | 3307 | Producción, persistencia |
| **PostgreSQL** | 15+ | Contenedor Docker | 5432 | Producción avanzada |

### 🛠️ **Requisitos del Sistema**

- **Java**: 24.0.1+ (JDK)
- **Maven**: 3.9.11+
- **Docker**: Para MySQL y PostgreSQL
- **Spring Boot**: 3.5.0

---

## 2. **H2 Database (Desarrollo Rápido)**

### 📋 **Características**
- ✅ **Sin configuración adicional**
- ✅ **Inicio inmediato**
- ✅ **Consola web integrada**
- ⚠️ **Datos NO persistentes**

### 🚀 **Inicio Rápido**

```powershell
# Opción 1: Script interactivo
.\start.ps1
# Seleccionar: 1

# Opción 2: Directo
.\start.ps1 -Database h2

# Opción 3: Maven directo
$env:SPRING_PROFILES_ACTIVE = "h2"; mvn spring-boot:run
```

### ⚙️ **Configuración (application-h2.properties)**

```properties
# H2 Database - En Memoria
spring.datasource.url=jdbc:h2:mem:stockchef;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.show-sql=true

# H2 Console Web
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

### 🌐 **URLs de Acceso**

| Servicio | URL | Credenciales |
|----------|-----|--------------|
| API Backend | http://localhost:8090/api | N/A |
| H2 Console | http://localhost:8090/api/h2-console | User: `sa`, Pass: (vacío) |
| JDBC URL | jdbc:h2:mem:stockchef | Para H2 Console |

---

## 3. **MySQL (Producción)**

### 📋 **Características**
- ✅ **Datos persistentes**
- ✅ **Alto rendimiento**
- ✅ **Ampliamente soportado**
- ⚠️ **Requiere Docker**

### 🐳 **Configuración Docker**

#### **Si NO tienes MySQL instalado:**
```bash
# Crear contenedor MySQL
docker run -d \
  --name stockchef-mysql \
  -p 3307:3306 \
  -e MYSQL_ROOT_PASSWORD=UserAdmin \
  -e MYSQL_DATABASE=stockchef_db \
  -e MYSQL_USER=stockchef_user \
  -e MYSQL_PASSWORD=UserAdmin \
  mysql:8.4
```

#### **Si YA tienes MySQL instalado/configurado:**

**📍 Localiza tu configuración en:** `src/main/resources/application-mysql.properties`

```properties
# ACTUALIZA ESTAS LÍNEAS CON TUS DATOS:
spring.datasource.url=jdbc:mysql://localhost:TUPUERTO/TUBASEDEDATOS?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=TUUSUARIO
spring.datasource.password=TUCONTRASEÑA
```

**🔧 Ejemplo con perfil existente:**
```properties
# Si tienes MySQL en puerto 3306 con usuario root
spring.datasource.url=jdbc:mysql://localhost:3306/stockchef_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=TU_PASSWORD_AQUÍ
```

### 🚀 **Inicio con MySQL**

```powershell
# Verificar que MySQL esté corriendo
docker ps | findstr mysql

# Iniciar backend
.\start.ps1 -Database mysql
```

### 📊 **Verificación de Conexión**

```bash
# Conectar manualmente para verificar
docker exec -it stockchef-mysql mysql -u root -pUserAdmin -e "SHOW DATABASES;"

# Debe mostrar: stockchef_db
```

---

## 4. **PostgreSQL (Producción)**

### 📋 **Características**
- ✅ **Muy robusto**
- ✅ **Estándares SQL estrictos**
- ✅ **Tipos de datos avanzados**
- ⚠️ **Requiere Docker**

### 🐳 **Configuración Docker**

#### **Si NO tienes PostgreSQL instalado:**
```bash
# Crear contenedor PostgreSQL
docker run -d \
  --name stockchef-postgres \
  -p 5432:5432 \
  -e POSTGRES_PASSWORD=UserAdmin \
  postgres:15
```

**⚠️ IMPORTANTE**: Con esta configuración, el usuario es `postgres` (no personalizado).

#### **Si YA tienes PostgreSQL instalado/configurado:**

**📍 Localiza tu configuración en:** `src/main/resources/application-postgresql.properties`

```properties
# ACTUALIZA ESTAS LÍNEAS CON TUS DATOS:
spring.datasource.url=jdbc:postgresql://localhost:TUPUERTO/TUBASEDEDATOS?sslmode=disable
spring.datasource.username=TUUSUARIO
spring.datasource.password=TUCONTRASEÑA
```

**🔧 Ejemplo con perfil existente:**
```properties
# Si tienes PostgreSQL local con usuario personalizado
spring.datasource.url=jdbc:postgresql://localhost:5432/mi_base_datos?sslmode=disable
spring.datasource.username=mi_usuario
spring.datasource.password=mi_contraseña
```

### 🚀 **Inicio con PostgreSQL**

```powershell
# Verificar que PostgreSQL esté corriendo
docker ps | findstr postgres

# Crear base de datos (si no existe)
docker exec stockchef-postgres psql -U postgres -c "CREATE DATABASE stockchef_db;"

# Iniciar backend
.\start.ps1 -Database postgresql
```

### 📊 **Verificación de Conexión**

```bash
# Conectar manualmente para verificar
docker exec stockchef-postgres psql -U postgres -c "\l"

# Debe mostrar: stockchef_db
```

---

## 5. **Configuración de Perfiles Existentes**

### 🎯 **Si ya tienes MySQL/PostgreSQL configurado**

#### **Paso 1: Identifica tu configuración**

**Para MySQL:**
```bash
# Encuentra tu puerto y credenciales
mysql -u root -p -e "SELECT @@port;"
```

**Para PostgreSQL:**
```bash
# Encuentra tu puerto y credenciales  
psql -U postgres -c "SHOW port;"
```

#### **Paso 2: Actualiza el archivo correspondiente**

**📁 Ubicación de archivos:**
- H2: `src/main/resources/application-h2.properties`
- MySQL: `src/main/resources/application-mysql.properties`  
- PostgreSQL: `src/main/resources/application-postgresql.properties`

#### **Paso 3: Parámetros clave a modificar**

```properties
# MYSQL - Parámetros principales
spring.datasource.url=jdbc:mysql://TU_HOST:TU_PUERTO/TU_BASE_DATOS?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_CONTRASEÑA

# POSTGRESQL - Parámetros principales
spring.datasource.url=jdbc:postgresql://TU_HOST:TU_PUERTO/TU_BASE_DATOS?sslmode=disable
spring.datasource.username=TU_USUARIO  
spring.datasource.password=TU_CONTRASEÑA
```

#### **Paso 4: Crear la base de datos (si no existe)**

**MySQL:**
```sql
CREATE DATABASE stockchef_db;
```

**PostgreSQL:**
```sql
CREATE DATABASE stockchef_db;
```

---

## 6. **Script de Inicio**

### 🎮 **Uso del Script**

```powershell
# Modo interactivo (recomendado)
.\start.ps1

# Modo directo
.\start.ps1 -Database h2          # H2
.\start.ps1 -Database mysql       # MySQL
.\start.ps1 -Database postgresql  # PostgreSQL
```

### ⚙️ **Lo que hace automáticamente el script:**

1. ✅ **Verifica dependencias** (Docker containers)
2. ✅ **Crea contenedores** si no existen
3. ✅ **Configura JAVA_HOME** automáticamente
4. ✅ **Selecciona perfil** correcto
5. ✅ **Inicia backend** con la BD seleccionada
6. ✅ **Inicializa datos** de prueba

---

## 7. **Troubleshooting**

### 🚨 **Problemas Comunes**

#### **Error: "JAVA_HOME not found"**
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-24"
```

#### **Error: "MySQL container not found"**
```bash
docker run -d --name stockchef-mysql -p 3307:3306 -e MYSQL_ROOT_PASSWORD=UserAdmin -e MYSQL_DATABASE=stockchef_db mysql:8.4
```

#### **Error: "PostgreSQL authentication failed"**
- ✅ Verifica que uses usuario `postgres` (no customizado)
- ✅ Verifica contraseña `UserAdmin`
- ✅ Verifica que la base `stockchef_db` existe

#### **Error: "Port already in use"**
```bash
# Ver qué está usando el puerto
netstat -ano | findstr :3307  # MySQL
netstat -ano | findstr :5432  # PostgreSQL
```

### 🔧 **Comandos Útiles**

```bash
# Ver contenedores
docker ps

# Parar todos los contenedores StockChef
docker stop stockchef-mysql stockchef-postgres

# Eliminar contenedores
docker rm stockchef-mysql stockchef-postgres

# Limpiar sistema Docker
docker system prune -f
```

---

## 🎉 **Testing Final**

### 🧪 **Credenciales de Prueba (todas las BDs)**

```json
{
    "email": "developer@stockchef.com",
    "password": "devpass123"
}
```

### 🌐 **Endpoint de Login**

```http
POST http://localhost:8090/api/auth/login
Content-Type: application/json

{
    "email": "developer@stockchef.com",
    "password": "devpass123"
}
```

### ✅ **Respuesta Esperada**

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

## 📋 **Resumen de Archivos**

| Archivo | Propósito |
|---------|-----------|
| `start.ps1` | Script de inicio interactivo |
| `application.properties` | Configuración base |
| `application-h2.properties` | Perfil H2 |
| `application-mysql.properties` | Perfil MySQL | 
| `application-postgresql.properties` | Perfil PostgreSQL |

---

**🎯 ¡Con esta configuración tienes un sistema completamente funcional para desarrollo y producción!** 

El sistema detecta automáticamente las dependencias y te guía paso a paso para una conexión exitosa. ✨