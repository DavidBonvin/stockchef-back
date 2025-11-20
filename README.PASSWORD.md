# 🔐 Password Management & Authentication Endpoints

Este documento explica cómo funcionan los endpoints de gestión de contraseñas y autenticación implementados en StockChef Backend, y cómo testearlos usando Thunder Client.

## 📋 Tabla de Contenidos

1. [Endpoints Implementados](#endpoints-implementados)
2. [Configuración Previa](#configuración-previa)
3. [Testing con Thunder Client](#testing-con-thunder-client)
4. [Casos de Uso](#casos-de-uso)
5. [Códigos de Error](#códigos-de-error)

## 🎯 Endpoints Implementados

### 1. **PUT** `/users/{id}/password` - Cambio de contraseña
- **Descripción**: Permite cambiar la contraseña de un usuario
- **Autorización**: Usuario propio o ADMIN
- **Autenticación**: Requerida (JWT Token)

### 2. **POST** `/users/{id}/reset-password` - Reset de contraseña (ADMIN)
- **Descripción**: Reset de contraseña por parte de un administrador
- **Autorización**: Solo ADMIN
- **Autenticación**: Requerida (JWT Token)

### 3. **POST** `/auth/refresh` - Renovar token JWT
- **Descripción**: Genera nuevos tokens usando un refresh token
- **Autorización**: No requerida
- **Autenticación**: Refresh Token válido

### 4. **POST** `/auth/logout` - Invalidar token
- **Descripción**: Invalida el token actual (blacklist)
- **Autorización**: Usuario autenticado
- **Autenticación**: Requerida (JWT Token)

### 5. **POST** `/users/change-password` - Cambio de contraseña personal
- **Descripción**: Cambio de contraseña del usuario autenticado
- **Autorización**: Usuario autenticado
- **Autenticación**: Requerida (JWT Token)

### 6. **POST** `/users/forgot-password` - Solicitar reset de contraseña
- **Descripción**: Solicita un reset de contraseña por email
- **Autorización**: No requerida (endpoint público)
- **Autenticación**: No requerida

## ⚙️ Configuración Previa

### 1. Variables de Entorno Thunder Client

Crea las siguientes variables en Thunder Client:

```json
{
  "baseUrl": "http://localhost:8080",
  "authToken": "",
  "refreshToken": "",
  "adminUserId": "1",
  "employeeUserId": "2"
}
```

### 2. Obtener Tokens de Autenticación

Primero necesitas autenticarte para obtener tokens:

**Endpoint Login (ejemplo):**
```
POST {{baseUrl}}/auth/login
Content-Type: application/json

{
  "email": "admin@stockchef.com",
  "password": "password123"
}
```

Guarda los tokens devueltos:
- `access_token` → variable `authToken`
- `refresh_token` → variable `refreshToken`

## 🧪 Testing con Thunder Client

### 1. **PUT** `/users/{id}/password` - Cambio de contraseña

#### Como ADMIN cambiando contraseña de otro usuario:
```http
PUT {{baseUrl}}/users/{{employeeUserId}}/password
Authorization: Bearer {{authToken}}
Content-Type: application/json

{
  "currentPassword": "oldPassword123",
  "newPassword": "NewSecurePass456!",
  "confirmPassword": "NewSecurePass456!"
}
```

#### Como usuario cambiando su propia contraseña:
```http
PUT {{baseUrl}}/users/{{employeeUserId}}/password
Authorization: Bearer {{authToken}}
Content-Type: application/json

{
  "currentPassword": "currentPassword123",
  "newPassword": "MyNewPassword789!",
  "confirmPassword": "MyNewPassword789!"
}
```

**Respuesta esperada (200):**
```json
{
  "message": "Contraseña actualizada exitosamente",
  "timestamp": "2025-11-20T10:30:00"
}
```

### 2. **POST** `/users/{id}/reset-password` - Reset de contraseña (ADMIN)

```http
POST {{baseUrl}}/users/{{employeeUserId}}/reset-password
Authorization: Bearer {{authToken}}
Content-Type: application/json

{
  "newPassword": "AdminResetPass123!",
  "confirmPassword": "AdminResetPass123!"
}
```

**Respuesta esperada (200):**
```json
{
  "message": "Contraseña reseteada exitosamente",
  "timestamp": "2025-11-20T10:30:00"
}
```

### 3. **POST** `/auth/refresh` - Renovar token JWT

```http
POST {{baseUrl}}/auth/refresh
Content-Type: application/json

{
  "refreshToken": "{{refreshToken}}"
}
```

**Respuesta esperada (200):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### 4. **POST** `/auth/logout` - Invalidar token

```http
POST {{baseUrl}}/auth/logout
Authorization: Bearer {{authToken}}
Content-Type: application/json
```

**Respuesta esperada (200):**
```json
{
  "message": "Logout exitoso",
  "timestamp": "2025-11-20T10:30:00"
}
```

### 5. **POST** `/users/change-password` - Cambio de contraseña personal

```http
POST {{baseUrl}}/users/change-password
Authorization: Bearer {{authToken}}
Content-Type: application/json

{
  "currentPassword": "currentPassword123",
  "newPassword": "MyNewSecurePass456!",
  "confirmPassword": "MyNewSecurePass456!"
}
```

**Respuesta esperada (200):**
```json
{
  "message": "Contraseña cambiada exitosamente",
  "timestamp": "2025-11-20T10:30:00"
}
```

### 6. **POST** `/users/forgot-password` - Solicitar reset de contraseña

```http
POST {{baseUrl}}/users/forgot-password
Content-Type: application/json

{
  "email": "user@stockchef.com"
}
```

**Respuesta esperada (200):**
```json
{
  "message": "Si el email existe, se ha enviado un enlace de recuperación",
  "timestamp": "2025-11-20T10:30:00"
}
```

## 📝 Casos de Uso

### Escenario 1: Usuario cambia su propia contraseña
1. Usuario se autentica con `POST /auth/login`
2. Usuario cambia su contraseña con `POST /users/change-password`
3. Usuario debe volver a autenticarse con la nueva contraseña

### Escenario 2: Admin resetea contraseña de empleado
1. Admin se autentica con `POST /auth/login`
2. Admin resetea la contraseña con `POST /users/{id}/reset-password`
3. Empleado debe usar la nueva contraseña temporal

### Escenario 3: Usuario olvida su contraseña
1. Usuario solicita reset con `POST /users/forgot-password`
2. Sistema envía email con enlace de recuperación
3. Usuario sigue el enlace y establece nueva contraseña

### Escenario 4: Renovación de tokens
1. Cuando el access token expira, usar `POST /auth/refresh`
2. Actualizar las variables de Thunder Client con los nuevos tokens
3. Continuar usando la aplicación

### Escenario 5: Logout seguro
1. Usuario termina sesión con `POST /auth/logout`
2. El token queda invalidado (blacklist)
3. Usuario debe autenticarse nuevamente

## ❌ Códigos de Error

### 400 - Bad Request
```json
{
  "timestamp": "2025-11-20T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Las contraseñas no coinciden",
  "code": "INVALID_PASSWORD"
}
```

### 401 - Unauthorized
```json
{
  "timestamp": "2025-11-20T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Token inválido o expirado",
  "code": "INVALID_TOKEN"
}
```

### 403 - Forbidden
```json
{
  "timestamp": "2025-11-20T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "No tienes permisos para realizar esta acción",
  "code": "INSUFFICIENT_PERMISSIONS"
}
```

### 404 - Not Found
```json
{
  "timestamp": "2025-11-20T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Usuario no encontrado",
  "code": "USER_NOT_FOUND"
}
```

## 🔧 Tips para Testing

### 1. **Colección Thunder Client**
Crea una colección específica para estos endpoints y organízalos por funcionalidad:
- **Password Management**
  - Change Own Password
  - Admin Change Password
  - Admin Reset Password
  - Forgot Password
- **Authentication**
  - Refresh Token
  - Logout

### 2. **Variables Dinámicas**
Usa variables de Thunder Client para hacer el testing más eficiente:
```javascript
// En la pestaña Tests de Thunder Client
tc.setVar("authToken", json.accessToken);
tc.setVar("refreshToken", json.refreshToken);
```

### 3. **Headers Comunes**
Configura headers por defecto para la colección:
```
Authorization: Bearer {{authToken}}
Content-Type: application/json
Accept: application/json
```

### 4. **Testing de Errores**
No olvides testear los casos de error:
- Contraseñas que no coinciden
- Tokens expirados o inválidos
- Usuarios sin permisos
- IDs de usuario inexistentes

### 5. **Secuencia de Testing**
Sigue esta secuencia para un testing completo:
1. Login para obtener tokens
2. Test casos exitosos
3. Test casos de error
4. Test autorización (diferentes roles)
5. Logout y verificar invalidación

## 🛡️ Consideraciones de Seguridad

### Validaciones Implementadas:
- ✅ **Longitud mínima de contraseña**: 8 caracteres
- ✅ **Confirmación de contraseña**: Debe coincidir
- ✅ **Autorización por roles**: ADMIN vs EMPLOYEE
- ✅ **Verificación de contraseña actual**: Para cambios
- ✅ **Tokens JWT**: Expiración y validación
- ✅ **Rate limiting**: Protección contra ataques de fuerza bruta
- ✅ **Logging de auditoría**: Todas las operaciones quedan registradas

### Para Producción:
- 🔒 **HTTPS**: Todos los endpoints deben usar HTTPS
- 🔒 **Encriptación**: Contraseñas hasheadas con bcrypt
- 🔒 **Email real**: Implementar envío de emails para forgot-password
- 🔒 **Expiración de tokens**: Configurar tiempos apropiados
- 🔒 **Blacklist de tokens**: Implementar almacenamiento persistente

---

**¡Happy Testing! 🚀**

Para más información sobre la API, consulta la documentación completa de StockChef Backend.