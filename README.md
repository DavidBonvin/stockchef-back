# 🍽️ StockChef Backend

![Java](https://img.shields.io/badge/Java-21-orange?style=flat&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen?style=flat&logo=spring)
![Docker](https://img.shields.io/badge/Docker-Ready-blue?style=flat&logo=docker)
![MySQL](https://img.shields.io/badge/MySQL-8.4-orange?style=flat&logo=mysql)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-blue?style=flat&logo=postgresql)

Backend REST API para StockChef - Sistema de gestión de inventario de cocina profesional con soporte multi-base de datos y containerización Docker completa.

## 📋 Tabla de Contenidos

- [🎯 Características](#-características)
- [🛠️ Requisitos](#️-requisitos)
- [🚀 Instalación y Configuración](#-instalación-y-configuración)
- [🐳 Uso con Docker](#-uso-con-docker)
- [🌐 Servicios y Puertos](#-servicios-y-puertos)
- [📁 Estructura del Proyecto](#-estructura-del-proyecto)
- [⚙️ Configuración](#️-configuración)
- [🧪 Testing](#-testing)
- [📚 API Endpoints](#-api-endpoints)
- [🔧 Desarrollo](#-desarrollo)
- [🚀 Despliegue](#-despliegue)
- [❓ Solución de Problemas](#-solución-de-problemas)

## 🎯 Características

- ✅ **Multi-Base de Datos**: Soporte para MySQL y PostgreSQL con cambio dinámico
- ✅ **Containerización Completa**: Docker Compose con todos los servicios
- ✅ **Scripts de Gestión**: Herramientas automatizadas para Windows y Linux
- ✅ **Herramientas de Admin**: phpMyAdmin y pgAdmin incluidos
- ✅ **Health Checks**: Monitoreo automático de servicios
- ✅ **Configuración por Perfiles**: Desarrollo, testing y producción
- ✅ **Seguridad**: Spring Security con JWT preparado
- ✅ **Arquitectura Profesional**: Estructura de packages empresarial

## 🛠️ Requisitos

### Requisitos Mínimos
- **Docker Desktop** 4.0+ con Docker Compose v2.0+
- **Git** para clonar el repositorio
- **4GB RAM** mínimo disponible para Docker
- **Puertos disponibles**: 8090, 3307, 5433, 8080, 8081

### Requisitos para Desarrollo Local (Opcional)
- **Java JDK** 21 o 24
- **Maven** 3.9+
- **MySQL** 8.4+ (si no usas Docker)
- **PostgreSQL** 18+ (si no usas Docker) - [Ver guía de instalación](#-instalación-postgresql-para-desarrollo-local)

## 🚀 Instalación y Configuración

### 1. Clonar el Repositorio
```bash
git clone https://github.com/DavidBonvin/stockchef-back.git
cd stockchef-back
```

### 2. Verificar Docker
```bash
# Verificar que Docker está ejecutándose
docker --version
docker-compose --version

# Debe mostrar versiones similares a:
# Docker version 27.4.0
# Docker Compose version v2.30.3
```

### 3. Configurar Variables de Entorno
```bash
# Copiar archivo de configuración
cp .env.example .env

# Editar configuración (opcional)
# DATABASE_TYPE=mysql  # o postgres
```

### 📥 Instalación PostgreSQL para Desarrollo Local

Si planeas desarrollar sin Docker y usar PostgreSQL local, sigue esta guía de instalación:

#### Componentes Recomendados para Instalar

##### ✅ **OBLIGATORIOS - Marcar Siempre**
- ✅ **PostgreSQL (64 bit) v18.0-2** - El servidor principal de base de datos
- ✅ **pgAgent (64 bit) for PostgreSQL 18 v4.2.3-1** - Para tareas programadas y mantenimiento

##### ✅ **RECOMENDADOS - Muy Útiles**
- ✅ **pgJDBC v42.7.2-1** - Driver JDBC para Java/Spring Boot (backup local)
- ✅ **psqlODBC (64 bit) v13.02.0000-1** - Para conectividad con herramientas externas

##### 🎯 **OPCIONALES - Según Necesidad**
- 🎯 **PostGIS 3.6 Bundle for PostgreSQL 18** - Solo si manejarás datos geográficos/ubicaciones

##### ❌ **NO NECESARIOS para StockChef**
- ❌ **Npgsql** - Driver para .NET (no Java)
- ❌ **psqlODBC (32 bit)** - Versión 32-bit innecesaria
- ❌ **PostgreSQL versiones anteriores** (v13-v17) - Ya tienes v18
- ❌ **Migration Toolkit** - Para migraciones empresariales complejas
- ❌ **Enterprise Manager** - Herramientas enterprise de pago
- ❌ **pgBouncer** - Pool de conexiones (Docker ya lo maneja)
- ❌ **PEM-HTTPD** - Servidor web para Enterprise Manager

#### Instalación Mínima para Docker
```
✅ Database Server
   └── PostgreSQL (64 bit) v18.0-2
```

> 💡 **Nota**: Si usas Docker principalmente, solo necesitas PostgreSQL 18. Los drivers y herramientas los maneja Docker automáticamente.

#### Verificar Instalación
```bash
# Verificar versión
psql --version

# Crear base de datos para desarrollo
createdb -U postgres stockchef_db
```

## 🐳 Uso con Docker

### Inicio Rápido

#### Con MySQL (Recomendado)
```powershell
# Windows PowerShell
.\docker-manager.ps1 up mysql
```

```bash
# Linux/Mac
./docker-manager.sh up mysql
```

#### Con PostgreSQL
```powershell
# Windows PowerShell
.\docker-manager.ps1 up postgres
```

```bash
# Linux/Mac
./docker-manager.sh up postgres
```

### Comandos de Gestión

| Comando | Descripción |
|---------|-------------|
| `.\docker-manager.ps1 up mysql` | Iniciar con MySQL |
| `.\docker-manager.ps1 up postgres` | Iniciar con PostgreSQL |
| `.\docker-manager.ps1 down` | Parar todos los servicios |
| `.\docker-manager.ps1 status` | Ver estado de servicios |
| `.\docker-manager.ps1 logs backend` | Ver logs del backend |
| `.\docker-manager.ps1 tools` | Iniciar herramientas de admin |
| `.\docker-manager.ps1 clean` | Limpiar datos (⚠️ destructivo) |
| `.\docker-manager.ps1 build` | Reconstruir imagen |
| `.\docker-manager.ps1 help` | Mostrar ayuda completa |

### Verificar Funcionamiento
```bash
# Probar endpoint de salud
curl http://localhost:8090/api/health

# Respuesta esperada:
# {"service":"stockchef-back","message":"StockChef Backend está funcionando correctamente","version":"0.0.1-SNAPSHOT","status":"UP","timestamp":"..."}
```

## 🌐 Servicios y Puertos

| Servicio | URL/Host | Puerto | Credenciales | Descripción |
|----------|----------|--------|--------------|-------------|
| **Backend API** | http://localhost:8090 | 8090 | - | API REST principal |
| **MySQL** | localhost:3307 | 3307 | `root` / `UserAdmin` | Base de datos MySQL |
| **PostgreSQL** | localhost:5433 | 5433 | `postgres` / `UserAdmin` | Base de datos PostgreSQL |
| **phpMyAdmin** | http://localhost:8080 | 8080 | `root` / `UserAdmin` | Admin web para MySQL |
| **pgAdmin** | http://localhost:8081 | 8081 | `admin@stockchef.com` / `UserAdmin` | Admin web para PostgreSQL |

> 💡 **Herramientas PostgreSQL**: Si instalaste PostgreSQL local con pgAgent, también tienes acceso a `psql` desde línea de comandos y pueden conectarse herramientas como pgAdmin desktop.

### Endpoints API Principales

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/api/health` | GET | Health check del servicio |
| `/api/health/status` | GET | Estado detallado con info de BD |

## 📁 Estructura del Proyecto

```
📦 stockchef-back/
├── 📁 .mvn/                          # Maven wrapper
├── 📁 docker/                        # Configuración Docker
│   ├── 📁 mysql/
│   │   └── init.sql                  # Script inicialización MySQL
│   └── 📁 postgres/
│       └── init.sql                  # Script inicialización PostgreSQL
├── 📁 src/
│   ├── 📁 main/
│   │   ├── 📁 java/com/stockchef/stockchefback/
│   │   │   ├── 📁 config/            # Configuraciones Spring
│   │   │   ├── 📁 controller/        # Controladores REST
│   │   │   ├── 📁 dto/              # Data Transfer Objects
│   │   │   ├── 📁 exception/        # Manejo de excepciones
│   │   │   ├── 📁 model/            # Entidades JPA
│   │   │   ├── 📁 repository/       # Repositorios de datos
│   │   │   ├── 📁 security/         # Configuración seguridad
│   │   │   ├── 📁 service/          # Lógica de negocio
│   │   │   └── StockchefBackApplication.java
│   │   └── 📁 resources/
│   │       ├── application.properties              # Config principal
│   │       ├── application-dev.properties          # Desarrollo sin BD
│   │       ├── application-mysql.properties        # Config MySQL local
│   │       ├── application-postgres.properties     # Config PostgreSQL local
│   │       ├── application-docker-mysql.properties # Config MySQL Docker
│   │       └── application-docker-postgres.properties # Config PostgreSQL Docker
│   └── 📁 test/                     # Tests unitarios
├── 🐳 Dockerfile                     # Imagen Docker del backend
├── 🐳 docker-compose.yml             # Orquestación de servicios
├── 🔧 docker-manager.ps1             # Script gestión Windows
├── 🔧 docker-manager.sh              # Script gestión Linux/Mac
├── ⚙️ .env                           # Variables de entorno
├── ⚙️ .env.example                   # Ejemplo de configuración
├── 📄 pom.xml                        # Configuración Maven
├── 📚 README-Docker.md               # Documentación Docker detallada
└── 📚 PROYECTO-COMPLETADO.md         # Resumen del proyecto
```

## ⚙️ Configuración

### Variables de Entorno (.env)

```bash
# Tipo de base de datos (mysql o postgres)
DATABASE_TYPE=mysql

# Perfil Spring activo
SPRING_PROFILES_ACTIVE=mysql

# Configuración JVM
JAVA_OPTS=-Xmx1024m -Xms512m

# Configuración MySQL
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

## 📧 Soporte

- **Repositorio**: [https://github.com/DavidBonvin/stockchef-back](https://github.com/DavidBonvin/stockchef-back)
- **Issues**: [Reportar problemas](https://github.com/DavidBonvin/stockchef-back/issues)
- **Wiki**: [Documentación extendida](https://github.com/DavidBonvin/stockchef-back/wiki)

## 📄 Licencia

Este proyecto está bajo la licencia MIT. Ver archivo `LICENSE` para más detalles.

---

**🍽️ StockChef Backend** - Desarrollado con ❤️ para gestión profesional de inventario de cocina.