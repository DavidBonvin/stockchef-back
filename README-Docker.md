# 🐳 StockChef Backend - Configuración Docker Detallada

Este documento contiene la configuración avanzada de Docker para StockChef Backend. 

> 📚 **Nota**: Para uso general, consulta el [README principal](README.md) que incluye toda la información necesaria.

Este proyecto incluye una configuración completa de Docker que permite ejecutar la aplicación StockChef con soporte para MySQL y PostgreSQL de manera flexible y profesional.

## 📋 Prerequisitos

- Docker Desktop instalado y ejecutándose
- Docker Compose v2.0 o superior
- Puertos disponibles: 8090, 3306, 5432, 8080, 8081

## 🚀 Inicio Rápido

### 1. Configuración inicial
```bash
# Copiar archivo de configuración
cp .env.example .env

# Editar configuraciones si es necesario
# DATABASE_TYPE=mysql  (o postgres)
```

### 2. Ejecutar con MySQL (recomendado)
```bash
# Usando script de PowerShell (Windows)
.\docker-manager.ps1 up mysql

# Usando script bash (Linux/Mac)
./docker-manager.sh up mysql

# O directamente con docker-compose
docker-compose up -d mysql stockchef-backend
```

### 3. Ejecutar con PostgreSQL
```bash
# Usando script de PowerShell (Windows)
.\docker-manager.ps1 up postgres

# Usando script bash (Linux/Mac)
./docker-manager.sh up postgres

# O directamente con docker-compose
docker-compose up -d postgres stockchef-backend
```

## 🛠️ Gestión de Servicios

### Scripts de Administración

Los scripts `docker-manager.ps1` (Windows) y `docker-manager.sh` (Linux/Mac) proporcionan una interfaz fácil para gestionar el entorno:

```bash
# Comandos principales
.\docker-manager.ps1 up mysql        # Iniciar con MySQL
.\docker-manager.ps1 up postgres     # Iniciar con PostgreSQL
.\docker-manager.ps1 down            # Parar servicios
.\docker-manager.ps1 restart mysql   # Reiniciar con MySQL
.\docker-manager.ps1 status          # Ver estado de servicios
.\docker-manager.ps1 logs backend    # Ver logs del backend
.\docker-manager.ps1 tools           # Iniciar herramientas de administración
.\docker-manager.ps1 clean           # Limpiar datos (¡cuidado!)
.\docker-manager.ps1 build           # Reconstruir imagen
```

### Comandos Docker Compose Directos

```bash
# Iniciar servicios
docker-compose up -d

# Ver logs
docker-compose logs -f stockchef-backend
docker-compose logs -f mysql
docker-compose logs -f postgres

# Parar servicios
docker-compose down

# Reconstruir imagen
docker-compose build --no-cache stockchef-backend
```

## 🌐 Puertos y Servicios

| Servicio | Puerto | URL | Descripción |
|----------|--------|-----|-------------|
| Backend | 8090 | http://localhost:8090/api | API StockChef |
| MySQL | 3306 | localhost:3306 | Base de datos MySQL |
| PostgreSQL | 5432 | localhost:5432 | Base de datos PostgreSQL |
| phpMyAdmin | 8080 | http://localhost:8080 | Administración MySQL |
| pgAdmin | 8081 | http://localhost:8081 | Administración PostgreSQL |

## 🔍 Endpoints Importantes

### Health Check
```bash
curl http://localhost:8090/api/health
```

### Status con información de base de datos
```bash
curl http://localhost:8090/api/health/status
```

## 🗄️ Bases de Datos

### Configuración MySQL
- **Host**: mysql (interno) / localhost:3306 (externo)
- **Base de datos**: stockchef_db
- **Usuario**: stockchef_user
- **Password**: UserAdmin
- **Root Password**: UserAdmin

### Configuración PostgreSQL
- **Host**: postgres (interno) / localhost:5432 (externo)
- **Base de datos**: stockchef_db
- **Usuario**: postgres
- **Password**: UserAdmin

## 🔧 Herramientas de Administración

### phpMyAdmin (MySQL)
- **URL**: http://localhost:8080
- **Usuario**: root
- **Password**: UserAdmin

### pgAdmin (PostgreSQL)
- **URL**: http://localhost:8081
- **Email**: admin@stockchef.com
- **Password**: UserAdmin

Para iniciar las herramientas:
```bash
.\docker-manager.ps1 tools
```

## 📁 Estructura de Archivos

```
├── docker-compose.yml          # Configuración principal de Docker Compose
├── Dockerfile                  # Imagen de la aplicación Spring Boot
├── .env.example               # Archivo de configuración de ejemplo
├── .dockerignore              # Archivos a ignorar en el build
├── docker-manager.ps1         # Script de gestión para Windows
├── docker-manager.sh          # Script de gestión para Linux/Mac
├── docker/                    # Configuraciones específicas de Docker
│   ├── mysql/
│   │   └── init.sql          # Script de inicialización MySQL
│   └── postgres/
│       └── init.sql          # Script de inicialización PostgreSQL
└── src/main/resources/
    ├── application-docker-mysql.properties
    └── application-docker-postgres.properties
```

## 🔄 Cambio entre Bases de Datos

Para cambiar de MySQL a PostgreSQL o viceversa:

1. **Parar servicios actuales**:
   ```bash
   .\docker-manager.ps1 down
   ```

2. **Iniciar con la nueva base de datos**:
   ```bash
   .\docker-manager.ps1 up postgres  # o mysql
   ```

3. **O editar .env y reiniciar**:
   ```bash
   # Cambiar DATABASE_TYPE en .env
   .\docker-manager.ps1 restart
   ```

## 💾 Persistencia de Datos

Los datos se almacenan en volúmenes de Docker:
- `mysql_data`: Datos de MySQL
- `postgres_data`: Datos de PostgreSQL
- `pgadmin_data`: Configuración de pgAdmin

Para limpiar todos los datos:
```bash
.\docker-manager.ps1 clean
```

## 🐛 Resolución de Problemas

### Verificar estado de servicios
```bash
.\docker-manager.ps1 status
docker-compose ps
```

### Ver logs detallados
```bash
.\docker-manager.ps1 logs backend
.\docker-manager.ps1 logs mysql
.\docker-manager.ps1 logs postgres
```

### Reconstruir imagen si hay cambios en el código
```bash
.\docker-manager.ps1 build
```

### Problemas de puertos ocupados
Si los puertos están ocupados, puedes modificar el archivo `.env`:
```bash
BACKEND_PORT=8091
MYSQL_PORT=3307
POSTGRES_PORT=5433
```

### Limpiar sistema Docker
```bash
docker system prune -a
docker volume prune
```

## 🔒 Seguridad

⚠️ **Importante**: Las contraseñas por defecto son para desarrollo. Para producción:

1. Cambia todas las contraseñas en `.env`
2. Usa variables de entorno seguras
3. Configura firewalls apropiados
4. Usa HTTPS con certificados SSL

## 🚀 Despliegue en Producción

Para producción, considera:

1. **Variables de entorno seguras**
2. **Secretos de Docker/Kubernetes**
3. **Redes privadas**
4. **Backup automático de bases de datos**
5. **Monitoreo y alertas**
6. **Load balancers**
7. **Certificados SSL/TLS**

## 📝 Notas

- El backend se configura automáticamente según la base de datos seleccionada
- Los health checks aseguran que las bases de datos estén listas antes de iniciar el backend
- Las herramientas de administración son opcionales y se inician con el perfil `tools`
- Los volúmenes persisten los datos entre reinicios de contenedores