# H2 Database Configuration Guide

## 📖 Descripción
H2 es una base de datos en memoria ideal para desarrollo rápido y testing. Los datos se pierden cuando se reinicia la aplicación.

## ⚡ Ventajas
- ✅ **Sin configuración**: Funciona inmediatamente
- ✅ **Rápido**: Base de datos en memoria
- ✅ **Consola web**: Interface gráfica para debug
- ✅ **Testing**: Perfecto para pruebas unitarias

## ❌ Desventajas
- ❌ **No persistente**: Datos se pierden al reiniciar
- ❌ **Limitado**: No para producción

## 🚀 Cómo usar

### Opción 1: Script automático
```powershell
.\start-backend.ps1
# Seleccionar opción 1
```

### Opción 2: Comando directo
```powershell
.\start-backend.ps1 -Database h2
```

### Opción 3: Maven directo
```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

## 🔧 Configuración (application-h2.properties)

```properties
# H2 Database Configuration
spring.datasource.url=jdbc:h2:mem:stockchef;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

## 🌐 URLs importantes

| Servicio | URL | Descripción |
|----------|-----|-------------|
| Backend API | http://localhost:8090/api | API principal |
| H2 Console | http://localhost:8090/api/h2-console | Consola web H2 |
| Auth Login | POST http://localhost:8090/api/auth/login | Endpoint de autenticación |

## 🔐 Credenciales por defecto

### API Authentication
```json
{
    "email": "developer@stockchef.com",
    "password": "devpass123"
}
```

### H2 Console
- **JDBC URL**: `jdbc:h2:mem:stockchef`
- **Usuario**: `sa`
- **Password**: (vacío)

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
El perfil H2 incluye identificación en los logs: `[H2-PROFILE]`

## 🔄 Reiniciar datos
Para limpiar todos los datos, simplemente reinicia la aplicación.

## ⚠️ Notas importantes
- Los datos se inicializan automáticamente con `DataInitConfig`
- Perfecto para desarrollo y testing
- No usar en producción
- Los datos no sobreviven a reinicios