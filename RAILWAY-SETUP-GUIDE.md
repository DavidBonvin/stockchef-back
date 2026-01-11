# 🚀 Guía de Configuración Railway - Plan A (Dual Deployment)

## 📋 Objetivo
Tener tu backend funcionando en **Railway** y **Render** simultáneamente, permitiéndote cambiar fácilmente entre ellos desde el frontend.

---

## ✅ PASO 1: Crear Cuenta y Proyecto en Railway

### 1.1 Registro
1. Ve a **https://railway.app**
2. Haz clic en **"Start a New Project"** o **"Login"**
3. Regístrate con tu cuenta de **GitHub**
4. Railway te dará **$5 USD gratis** para empezar

### 1.2 Plan de Suscripción
- **Plan Hobby**: €5/mes ($5/mes)
- Incluye:
  - Despliegues ilimitados
  - PostgreSQL incluido
  - 500 horas de ejecución/mes
  - Métricas y logs

---

## 🗄️ PASO 2: Crear Base de Datos PostgreSQL en Railway

### 2.1 Crear el Servicio de Base de Datos
1. En tu proyecto Railway, haz clic en **"+ New"**
2. Selecciona **"Database"**
3. Elige **"PostgreSQL"**
4. Railway creará automáticamente:
   - ✅ Una base de datos PostgreSQL
   - ✅ Variables de entorno automáticas
   - ✅ URL de conexión

### 2.2 Obtener las Credenciales
1. Haz clic en el servicio **PostgreSQL** creado
2. Ve a la pestaña **"Variables"**
3. Verás algo como:
   ```
   DATABASE_URL=postgresql://postgres:...@...railway.app:5432/railway
   POSTGRES_USER=postgres
   POSTGRES_PASSWORD=...
   POSTGRES_DB=railway
   ```

### 2.3 Copiar el DATABASE_URL
- Copia el valor completo de `DATABASE_URL`
- Lo necesitarás en el siguiente paso

---

## 🚂 PASO 3: Desplegar el Backend en Railway

### 3.1 Crear Servicio para el Backend
1. En tu proyecto Railway, haz clic nuevamente en **"+ New"**
2. Selecciona **"GitHub Repo"**
3. Autoriza Railway a acceder a tu GitHub (si es la primera vez)
4. Selecciona el repositorio: **`stockchef-back`**
5. Selecciona la rama: **`main`** (o la que uses para producción)

### 3.2 Railway Detectará Automáticamente
Railway detectará:
- ✅ Proyecto Java
- ✅ Maven como build tool
- ✅ Java 21/24
- ✅ Archivo `railway.json` con configuración personalizada

---

## ⚙️ PASO 4: Configurar Variables de Entorno

### 4.1 Acceder a Variables
1. Haz clic en el servicio **stockchef-back** (tu backend)
2. Ve a la pestaña **"Variables"**
3. Haz clic en **"+ New Variable"**

### 4.2 Agregar las Siguientes Variables

**Variables OBLIGATORIAS:**

```bash
# Perfil de Spring Boot
SPRING_PROFILES_ACTIVE=production

# Puerto (Railway lo asigna automáticamente, pero podemos forzarlo)
PORT=8090

# JWT Secret (usa una clave fuerte de 32+ caracteres)
JWT_SECRET=StockChefSuperSecureSecretKeyForJWTTokenGenerationAndValidation2024!

# JWT Expiration (24 horas en milisegundos)
JWT_EXPIRATION=86400000
```

**Variables de Base de Datos:**

```bash
# Opción 1: Usar DATABASE_URL completo (RECOMENDADO)
DATABASE_URL=postgresql://postgres:tu-password@containers-us-west-xx.railway.app:5432/railway

# Opción 2: O usar variables separadas
DB_USERNAME=postgres
DB_PASSWORD=tu-password-postgresql
```

### 4.3 Conectar la Base de Datos (Método Automático)
1. En la pestaña de tu servicio backend, ve a **"Settings"**
2. Busca la sección **"Service Variables"**
3. Haz clic en **"+ Reference"**
4. Selecciona el servicio **PostgreSQL**
5. Railway automáticamente enlazará `DATABASE_URL`

---

## 🔧 PASO 5: Configurar el Despliegue

### 5.1 Verificar railway.json
Tu proyecto ya tiene el archivo `railway.json` configurado con:
```json
{
  "build": {
    "builder": "NIXPACKS",
    "buildCommand": "mvn clean package -DskipTests"
  },
  "deploy": {
    "healthcheckPath": "/api/actuator/health",
    "startCommand": "java -Dserver.port=$PORT -jar target/stockchef-back-0.0.1-SNAPSHOT.jar"
  }
}
```

### 5.2 Iniciar el Despliegue
1. Railway comenzará a construir automáticamente
2. Verás los logs en tiempo real
3. El proceso tomará **3-5 minutos**

### 5.3 Monitorear el Despliegue
En la pestaña **"Deployments"**:
- ✅ **Building**: Compilando con Maven
- ✅ **Deploying**: Desplegando la aplicación
- ✅ **Active**: ¡Aplicación funcionando!

---

## 🌐 PASO 6: Obtener la URL Pública

### 6.1 Generar Dominio
1. Ve al servicio **stockchef-back**
2. Pestaña **"Settings"**
3. Sección **"Networking"** → **"Public Networking"**
4. Haz clic en **"Generate Domain"**
5. Railway te dará una URL como:
   ```
   https://stockchef-back-production.up.railway.app
   ```

### 6.2 ¡URL Completa de tu API!
```
https://stockchef-back-production.up.railway.app/api
```

---

## 🧪 PASO 7: Probar el Despliegue

### 7.1 Health Check
Abre tu navegador o usa cURL:

```bash
# Health check personalizado
curl https://stockchef-back-production.up.railway.app/api/health

# Actuator health (Spring Boot)
curl https://stockchef-back-production.up.railway.app/api/actuator/health
```

**Respuesta esperada:**
```json
{
  "status": "UP",
  "service": "stockchef-back",
  "timestamp": "2026-01-11T..."
}
```

### 7.2 Probar Login
```bash
curl -X POST https://stockchef-back-production.up.railway.app/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "developer@stockchef.com",
    "password": "devpass123"
  }'
```

**Respuesta esperada:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "developer@stockchef.com",
  "role": "ROLE_DEVELOPER"
}
```

---

## 📊 PASO 8: Configuración Dual (Railway + Render)

Ahora tienes DOS backends funcionando:

### URLs Disponibles:
```javascript
// Railway (Principal - €5/mes)
const RAILWAY_API = 'https://stockchef-back-production.up.railway.app/api';

// Render (Respaldo - €20/mes o gratis temporal)
const RENDER_API = 'https://stockchef-back.onrender.com/api';
```

---

## 🔄 PASO 9: Sincronizar Datos (Opcional)

### Si necesitas migrar datos de Render a Railway:

#### 9.1 Exportar desde Render
```bash
# Conectarse a Render PostgreSQL
pg_dump -h dpg-d4vclg7fte5s73fj1d0g-a.oregon-postgres.render.com \
  -U stockchef_postgresql_user \
  -d stockchef_postgresql \
  -f render_backup.sql
```

#### 9.2 Importar a Railway
```bash
# Conectarse a Railway PostgreSQL
psql $DATABASE_URL < render_backup.sql
```

### O usar pgAdmin/DBeaver para copiar datos visualmente

---

## 🎯 PASO 10: Configurar Frontend

### 10.1 Configuración Simple (un solo backend)
```typescript
// src/config/api.ts
const API_BASE_URL = process.env.REACT_APP_API_URL || 
  'https://stockchef-back-production.up.railway.app/api';

export default API_BASE_URL;
```

### 10.2 Configuración con Failover (ambos backends)
```typescript
// src/config/api.ts
export const API_ENDPOINTS = {
  primary: 'https://stockchef-back-production.up.railway.app/api',
  fallback: 'https://stockchef-back.onrender.com/api'
};

// Helper para intentar con failover
export async function fetchWithFallback(endpoint: string, options?: RequestInit) {
  try {
    const response = await fetch(`${API_ENDPOINTS.primary}${endpoint}`, options);
    if (!response.ok) throw new Error('Primary failed');
    return response;
  } catch (error) {
    console.warn('Primary API failed, using fallback...');
    return fetch(`${API_ENDPOINTS.fallback}${endpoint}`, options);
  }
}
```

### 10.3 Variables de Entorno (.env)
```bash
# .env.production
REACT_APP_API_URL=https://stockchef-back-production.up.railway.app/api

# .env.development
REACT_APP_API_URL=http://localhost:8090/api
```

---

## 🛡️ MEJORES PRÁCTICAS

### ✅ Seguridad
- ✅ Nunca commites credenciales al repositorio
- ✅ Usa variables de entorno para TODOS los secretos
- ✅ Regenera JWT_SECRET en producción
- ✅ Activa autenticación 2FA en Railway

### ✅ Mantenimiento
- ✅ Revisa logs regularmente en Railway Dashboard
- ✅ Configura alertas para errores
- ✅ Mantén un backup reciente de la base de datos
- ✅ Prueba ambos servicios semanalmente

### ✅ Costos
- Railway: €5/mes
- Render: €20/mes (o gratis mientras dure)
- **Total temporal**: €5-€25/mes
- **Después de migración completa**: €5/mes solo Railway

---

## 🔍 Solución de Problemas

### Error: "DATABASE_URL not found"
**Solución:** Verifica que conectaste el servicio PostgreSQL al backend en Railway

### Error: "Port already in use"
**Solución:** Railway asigna el puerto automáticamente vía `$PORT`, no necesitas configurarlo

### Error: "Build failed"
**Solución:** 
1. Revisa los logs en Railway
2. Verifica que `pom.xml` esté correcto
3. Asegúrate de que Java 21+ esté especificado

### El health check falla
**Solución:**
1. Verifica que `/api/actuator/health` esté habilitado
2. Aumenta el `healthcheckTimeout` en `railway.json`
3. Revisa los logs de la aplicación

---

## 📚 Recursos Adicionales

- [Railway Documentation](https://docs.railway.app)
- [Railway Discord](https://discord.gg/railway)
- [PostgreSQL en Railway](https://docs.railway.app/databases/postgresql)

---

## ✅ Checklist Final

- [ ] Cuenta Railway creada y suscripción activa (€5/mes)
- [ ] Base de datos PostgreSQL creada en Railway
- [ ] Backend desplegado desde GitHub
- [ ] Variables de entorno configuradas
- [ ] URL pública generada
- [ ] Health check funcionando
- [ ] Login endpoint probado
- [ ] Frontend configurado con nueva URL
- [ ] (Opcional) Datos migrados de Render a Railway

---

## 🎉 ¡Listo!

Ahora tienes:
- ✅ Backend en Railway (€5/mes)
- ✅ Backend en Render (respaldo temporal)
- ✅ Flexibilidad para cambiar entre ambos
- ✅ Ahorro de €15/mes cuando canceles Render

**¿Necesitas ayuda con algún paso?** 🚀

---

*Última actualización: Enero 11, 2026*
