# 🚀 StockChef Backend - Inicio Rápido

## ⚡ Configuración en 3 Minutos

### 1. ✅ Verificar Requisitos
```bash
# Verificar Docker
docker --version
docker-compose --version

# Verificar puertos disponibles
netstat -tulpn | grep -E "(8090|3307|5433|8080|8081)"

# Para desarrollo local (opcional)
java --version  # Java 21+
mvn --version   # Maven 3.9+
```

> 💡 **PostgreSQL Local**: Si planeas usar PostgreSQL sin Docker, instala solo PostgreSQL 18 + pgAgent. [Ver guía completa](README.md#-instalación-postgresql-para-desarrollo-local)

### 2. 📥 Clonar y Configurar
```bash
# Clonar repositorio
git clone https://github.com/DavidBonvin/stockchef-back.git
cd stockchef-back

# Configurar entorno
cp .env.example .env
```

### 3. 🚀 Ejecutar
```powershell
# Windows - Iniciar con MySQL
.\docker-manager.ps1 up mysql

# Linux/Mac - Iniciar con MySQL  
./docker-manager.sh up mysql
```

### 4. ✅ Verificar
```bash
# Probar API
curl http://localhost:8090/api/health

# Abrir herramientas
# phpMyAdmin: http://localhost:8080
# Backend: http://localhost:8090/api/health
```

## 📱 Interfaces de Usuario

### 🔗 Enlaces Rápidos
| Servicio | URL | Usuario | Contraseña |
|----------|-----|---------|-----------|
| **API Backend** | http://localhost:8090/api/health | - | - |
| **phpMyAdmin** | http://localhost:8080 | `root` | `UserAdmin` |
| **pgAdmin** | http://localhost:8081 | `admin@stockchef.com` | `UserAdmin` |

### 🎯 Comandos Esenciales
```powershell
# Estado del sistema
.\docker-manager.ps1 status

# Cambiar a PostgreSQL
.\docker-manager.ps1 down
.\docker-manager.ps1 up postgres

# Ver logs en vivo
.\docker-manager.ps1 logs backend

# Parar todo
.\docker-manager.ps1 down
```

## 🛠️ Desarrollo Inmediato

### Agregar tu primera entidad:

1. **Crear modelo** en `src/main/java/com/stockchef/stockchefback/model/`
2. **Crear repositorio** en `src/main/java/com/stockchef/stockchefback/repository/`
3. **Crear servicio** en `src/main/java/com/stockchef/stockchefback/service/`
4. **Crear controller** en `src/main/java/com/stockchef/stockchefback/controller/`

### Reconstruir tras cambios:
```bash
.\docker-manager.ps1 build
.\docker-manager.ps1 restart mysql
```

---

📚 **Documentación completa**: Ver [README.md](README.md) para información detallada.