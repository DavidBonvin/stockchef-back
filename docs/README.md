# 📚 **Documentación StockChef Backend**

## 📄 **Guías Disponibles**

### 🚀 **[QUICK-START.md](QUICK-START.md)**
- Inicio rápido en 3 pasos
- Configuración express para desarrollo
- Comandos básicos de testing

### 🔧 **[MULTI-DATABASE-SETUP.md](MULTI-DATABASE-SETUP.md)**
- Guía completa de instalación
- Configuración detallada para H2, MySQL y PostgreSQL
- Troubleshooting avanzado
- Ejemplos de configuración personalizados

## 🏗️ **Arquitectura del Proyecto**

```
stockchef-back/
├── start.ps1              # Script de inicio interactivo
├── src/main/java/com/stockchef/stockchefback/
│   ├── controller/        # Controladores REST
│   │   └── AuthController.java
│   ├── service/          # Servicios de negocio
│   │   └── JwtService.java
│   ├── repository/       # Acceso a datos
│   │   └── UserRepository.java
│   ├── entity/           # Entidades JPA
│   │   └── User.java
│   └── config/           # Configuración
│       └── DataInitConfig.java
└── src/main/resources/
    ├── application.properties           # Configuración base
    ├── application-h2.properties       # Perfil H2
    ├── application-mysql.properties    # Perfil MySQL
    └── application-postgresql.properties # Perfil PostgreSQL
```

## 🌟 **Características Principales**

- ✅ **Autenticación JWT** - Sistema completo de tokens
- ✅ **Multi-base de datos** - H2, MySQL, PostgreSQL
- ✅ **Gestión automática** - Contenedores Docker automáticos
- ✅ **Perfiles Spring** - Configuraciones independientes
- ✅ **Inicialización de datos** - Usuario de desarrollo automático
- ✅ **Documentación completa** - Guías paso a paso

## 🔐 **Credenciales de Desarrollo**

```json
{
  "email": "developer@stockchef.com",
  "password": "devpass123"
}
```

## 📊 **Endpoints Principales**

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/auth/login` | Autenticación JWT |
| GET | `/h2-console` | Consola H2 (solo perfil H2) |

## 🐳 **Docker Containers**

| Base de Datos | Container | Puerto | Comando |
|---------------|-----------|---------|---------|
| MySQL | `stockchef-mysql` | 3307 | `docker run -d --name stockchef-mysql...` |
| PostgreSQL | `stockchef-postgres` | 5432 | `docker run -d --name stockchef-postgres...` |

## 🔄 **Flujo de Desarrollo**

1. **Desarrollo rápido:** Usa H2 para pruebas inmediatas
2. **Testing completo:** Usa MySQL para persistencia
3. **Producción:** Usa PostgreSQL para robustez

## 🛠️ **Herramientas de Debugging**

- **H2 Console:** http://localhost:8090/h2-console
- **Logs de aplicación:** Salida estándar del terminal
- **Health checks:** Verificación automática de contenedores

## 📞 **Soporte y Mantenimiento**

Para problemas específicos:
1. Consulta la sección de troubleshooting en `MULTI-DATABASE-SETUP.md`
2. Verifica los logs de la aplicación
3. Revisa el estado de los contenedores Docker

---
*Última actualización: $(Get-Date -Format "yyyy-MM-dd")*