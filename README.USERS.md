# 👥 API de Gestión de Usuarios - StockChef

Este documento describe todos los endpoints relacionados con usuarios y cómo probarlos usando Thunder Client.

## 📋 Índice

1. [Autenticación](#autenticación)
2. [Endpoints Públicos](#endpoints-públicos)
3. [Endpoints Protegidos](#endpoints-protegidos)
4. [Configuración Thunder Client](#configuración-thunder-client)
5. [Variables de Entorno](#variables-de-entorno)
6. [Casos de Uso Completos](#casos-de-uso-completos)

---

## 🔐 Autenticación

Todos los endpoints protegidos requieren un token JWT en el header `Authorization: Bearer <token>`.

### Sistema de Roles

- **ROLE_EMPLOYEE**: Usuario básico
- **ROLE_CHEF**: Cocinero con permisos adicionales
- **ROLE_DEVELOPER**: Desarrollador con permisos administrativos
- **ROLE_ADMIN**: Administrador con máximos permisos

---

## 🌐 Endpoints Públicos

### 1. Registro de Usuario

**Permite a cualquier usuario crear una cuenta con rol EMPLOYEE por defecto**

```http
POST http://localhost:8090/api/users/register
Content-Type: application/json

{
  "firstName": "Juan",
  "lastName": "Pérez",
  "email": "juan.perez@stockchef.com",
  "password": "MiPassword123!"
}
```

**Respuesta Exitosa (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "email": "juan.perez@stockchef.com",
  "firstName": "Juan",
  "lastName": "Pérez",
  "role": "ROLE_EMPLOYEE",
  "originalRole": "ROLE_EMPLOYEE",
  "active": true,
  "createdAt": "2025-11-19T20:30:15.123456",
  "updatedAt": null,
  "createdBy": "system"
}
```

**Thunder Client:**
```
Method: POST
URL: http://localhost:8090/api/users/register
Headers:
  Content-Type: application/json
Body (JSON):
{
  "firstName": "Juan",
  "lastName": "Pérez",
  "email": "juan.perez@stockchef.com",
  "password": "MiPassword123!"
}
```

### 2. Login de Usuario

**Obtiene un token JWT para autenticación**

```http
POST http://localhost:8090/api/auth/login
Content-Type: application/json

{
  "email": "employee@stockchef.com",
  "password": "employee123"
}
```

**Respuesta Exitosa (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlbXBsb3llZUBzdG9ja2NoZWYuY29tIiwiaWF0IjoxNjQzNzM2MDAwLCJleHAiOjE2NDM4MjI0MDB9.XYZ",
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440040",
    "email": "employee@stockchef.com",
    "firstName": "Employee",
    "lastName": "User",
    "role": "ROLE_EMPLOYEE"
  }
}
```

**Thunder Client:**
```
Method: POST
URL: http://localhost:8090/api/auth/login
Headers:
  Content-Type: application/json
Body (JSON):
{
  "email": "employee@stockchef.com",
  "password": "employee123"
}
```

---

## 🔒 Endpoints Protegidos

### 3. Obtener Perfil Actual

**Obtiene la información del usuario autenticado**

```http
GET http://localhost:8090/api/users/me
Authorization: Bearer {{jwt_token}}
```

**Respuesta Exitosa (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440040",
  "email": "employee@stockchef.com",
  "firstName": "Employee",
  "lastName": "User",
  "role": "ROLE_EMPLOYEE",
  "originalRole": "ROLE_EMPLOYEE",
  "active": true,
  "createdAt": "2025-11-19T19:54:04.123456",
  "updatedAt": null,
  "createdBy": "system"
}
```

**Thunder Client:**
```
Method: GET
URL: http://localhost:8090/api/users/me
Headers:
  Authorization: Bearer {{jwt_token}}
```

### 4. Actualizar Información Personal

**Permite actualizar firstName, lastName y email del usuario**

#### 4.1. Actualizar Propia Información (Todos los roles)

```http
PUT http://localhost:8090/api/users/550e8400-e29b-41d4-a716-446655440040
Authorization: Bearer {{employee_token}}
Content-Type: application/json

{
  "firstName": "Empleado",
  "lastName": "Actualizado",
  "email": "empleado.nuevo@stockchef.com"
}
```

**Respuesta Exitosa (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440040",
  "email": "empleado.nuevo@stockchef.com",
  "firstName": "Empleado",
  "lastName": "Actualizado",
  "role": "ROLE_EMPLOYEE",
  "originalRole": "ROLE_EMPLOYEE",
  "active": true,
  "createdAt": "2025-11-19T19:54:04.123456",
  "updatedAt": "2025-11-19T21:15:30.987654",
  "createdBy": "system"
}
```

**Thunder Client:**
```
Method: PUT
URL: http://localhost:8090/api/users/550e8400-e29b-41d4-a716-446655440040
Headers:
  Authorization: Bearer {{employee_token}}
  Content-Type: application/json
Body (JSON):
{
  "firstName": "Empleado",
  "lastName": "Actualizado",
  "email": "empleado.nuevo@stockchef.com"
}
```

#### 4.2. DEVELOPER/ADMIN Actualizando Otros Usuarios

```http
PUT http://localhost:8090/api/users/550e8400-e29b-41d4-a716-446655440040
Authorization: Bearer {{developer_token}}
Content-Type: application/json

{
  "firstName": "Usuario",
  "lastName": "Modificado por Dev",
  "email": "modificado.por.dev@stockchef.com"
}
```

**Thunder Client:**
```
Method: PUT
URL: http://localhost:8090/api/users/550e8400-e29b-41d4-a716-446655440040
Headers:
  Authorization: Bearer {{developer_token}}
  Content-Type: application/json
Body (JSON):
{
  "firstName": "Usuario",
  "lastName": "Modificado por Dev",
  "email": "modificado.por.dev@stockchef.com"
}
```

### 5. Listar Todos los Usuarios

**Obtiene lista de todos los usuarios (Solo ADMIN y DEVELOPER)**

#### 5.1. Listar Todos los Usuarios

```http
GET http://localhost:8090/api/users
Authorization: Bearer {{admin_token}}
```

**Respuesta Exitosa (200 OK):**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440010",
    "email": "admin@stockchef.com",
    "firstName": "Admin",
    "lastName": "User",
    "role": "ROLE_ADMIN",
    "originalRole": "ROLE_ADMIN",
    "active": true,
    "createdAt": "2025-11-19T19:54:04.123456",
    "updatedAt": null,
    "createdBy": "system"
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440020",
    "email": "developer@stockchef.com",
    "firstName": "Developer",
    "lastName": "User",
    "role": "ROLE_DEVELOPER",
    "originalRole": "ROLE_DEVELOPER",
    "active": true,
    "createdAt": "2025-11-19T19:54:04.123456",
    "updatedAt": null,
    "createdBy": "system"
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440040",
    "email": "employee@stockchef.com",
    "firstName": "Employee",
    "lastName": "User",
    "role": "ROLE_EMPLOYEE",
    "originalRole": "ROLE_EMPLOYEE",
    "active": true,
    "createdAt": "2025-11-19T19:54:04.123456",
    "updatedAt": null,
    "createdBy": "system"
  }
]
```

#### 5.2. Filtrar por Rol

```http
GET http://localhost:8090/api/users?role=ROLE_EMPLOYEE
Authorization: Bearer {{admin_token}}
```

#### 5.3. Filtrar por Estado Activo

```http
GET http://localhost:8090/api/users?active=true
Authorization: Bearer {{admin_token}}
```

#### 5.4. Filtrar por Rol y Estado

```http
GET http://localhost:8090/api/users?role=ROLE_DEVELOPER&active=true
Authorization: Bearer {{admin_token}}
```

**Thunder Client:**
```
Method: GET
URL: http://localhost:8090/api/users
Headers:
  Authorization: Bearer {{admin_token}}
```

**Con filtros:**
```
Method: GET
URL: http://localhost:8090/api/users?role=ROLE_EMPLOYEE&active=true
Headers:
  Authorization: Bearer {{admin_token}}
```

### 6. Obtener Usuario por ID

**Obtiene información de un usuario específico**

#### 6.1. Ver Propio Perfil por ID (Todos los roles)

```http
GET http://localhost:8090/api/users/550e8400-e29b-41d4-a716-446655440040
Authorization: Bearer {{employee_token}}
```

**Respuesta Exitosa (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440040",
  "email": "employee@stockchef.com",
  "firstName": "Employee",
  "lastName": "User",
  "role": "ROLE_EMPLOYEE",
  "originalRole": "ROLE_EMPLOYEE",
  "active": true,
  "createdAt": "2025-11-19T19:54:04.123456",
  "updatedAt": null,
  "createdBy": "system"
}
```

#### 6.2. ADMIN/DEVELOPER Viendo Cualquier Usuario

```http
GET http://localhost:8090/api/users/550e8400-e29b-41d4-a716-446655440040
Authorization: Bearer {{admin_token}}
```

**Thunder Client:**
```
Method: GET
URL: http://localhost:8090/api/users/{{employee_id}}
Headers:
  Authorization: Bearer {{admin_token}}
```

### 7. Eliminar Usuario

**Desactiva un usuario del sistema (Solo ADMIN)**

```http
DELETE http://localhost:8090/api/users/550e8400-e29b-41d4-a716-446655440040
Authorization: Bearer {{admin_token}}
```

**Respuesta Exitosa (204 No Content):**
```
(Sin contenido en la respuesta)
```

**Thunder Client:**
```
Method: DELETE
URL: http://localhost:8090/api/users/{{employee_id}}
Headers:
  Authorization: Bearer {{admin_token}}
```
  "email": "empleado.nuevo@stockchef.com"
}
```

#### 4.2. DEVELOPER/ADMIN Actualizando Otros Usuarios

```http
PUT http://localhost:8090/api/users/550e8400-e29b-41d4-a716-446655440040
Authorization: Bearer {{developer_token}}
Content-Type: application/json

{
  "firstName": "Usuario",
  "lastName": "Modificado por Dev",
  "email": "modificado.por.dev@stockchef.com"
}
```

**Thunder Client:**
```
Method: PUT
URL: http://localhost:8090/api/users/550e8400-e29b-41d4-a716-446655440040
Headers:
  Authorization: Bearer {{developer_token}}
  Content-Type: application/json
Body (JSON):
{
  "firstName": "Usuario",
  "lastName": "Modificado por Dev",
  "email": "modificado.por.dev@stockchef.com"
}
```

---

## ⚙️ Configuración Thunder Client

### Variables de Entorno

Crea un entorno en Thunder Client con estas variables:

```json
{
  "base_url": "http://localhost:8090/api",
  "jwt_token": "",
  "employee_token": "",
  "chef_token": "",
  "developer_token": "",
  "admin_token": "",
  "employee_id": "550e8400-e29b-41d4-a716-446655440040",
  "chef_id": "550e8400-e29b-41d4-a716-446655440050",
  "developer_id": "550e8400-e29b-41d4-a716-446655440020",
  "admin_id": "550e8400-e29b-41d4-a716-446655440010"
}
```

### Usuarios Predefinidos en el Sistema

| Email | Password | Rol | UUID |
|-------|----------|-----|------|
| `employee@stockchef.com` | `employee123` | ROLE_EMPLOYEE | `550e8400-e29b-41d4-a716-446655440040` |
| `chef@stockchef.com` | `chef123` | ROLE_CHEF | `550e8400-e29b-41d4-a716-446655440050` |
| `developer@stockchef.com` | `developer123` | ROLE_DEVELOPER | `550e8400-e29b-41d4-a716-446655440020` |
| `admin@stockchef.com` | `admin123` | ROLE_ADMIN | `550e8400-e29b-41d4-a716-446655440010` |

---

## 🧪 Casos de Uso Completos

### Flujo 1: Registro y Login de Nuevo Usuario

1. **Registrar usuario:**
```
POST {{base_url}}/users/register
{
  "firstName": "Test",
  "lastName": "User",
  "email": "test.user@stockchef.com",
  "password": "TestPassword123!"
}
```

2. **Hacer login:**
```
POST {{base_url}}/auth/login
{
  "email": "test.user@stockchef.com",
  "password": "TestPassword123!"
}
```

3. **Guardar el token** y usarlo para siguientes peticiones

### Flujo 2: Obtener Tokens para Todos los Roles

#### 2.1. Token de Employee
```
POST {{base_url}}/auth/login
{
  "email": "employee@stockchef.com",
  "password": "employee123"
}
```

#### 2.2. Token de Chef
```
POST {{base_url}}/auth/login
{
  "email": "chef@stockchef.com",
  "password": "chef123"
}
```

#### 2.3. Token de Developer
```
POST {{base_url}}/auth/login
{
  "email": "developer@stockchef.com",
  "password": "developer123"
}
```

#### 2.4. Token de Admin
```
POST {{base_url}}/auth/login
{
  "email": "admin@stockchef.com",
  "password": "admin123"
}
```

### Flujo 3: Probar Autorización y Nuevos Endpoints

#### 3.1. Employee actualizando su propia información (✅ Permitido)
```
PUT {{base_url}}/users/{{employee_id}}
Authorization: Bearer {{employee_token}}
{
  "firstName": "Employee",
  "lastName": "Updated",
  "email": "employee.updated@stockchef.com"
}
```

#### 3.2. Employee intentando actualizar otro usuario (❌ Prohibido - 403)
```
PUT {{base_url}}/users/{{admin_id}}
Authorization: Bearer {{employee_token}}
{
  "firstName": "Should",
  "lastName": "Fail",
  "email": "should.fail@stockchef.com"
}
```

#### 3.3. Developer actualizando cualquier usuario (✅ Permitido)
```
PUT {{base_url}}/users/{{employee_id}}
Authorization: Bearer {{developer_token}}
{
  "firstName": "Modified",
  "lastName": "By Developer",
  "email": "modified.by.dev@stockchef.com"
}
```

#### 3.4. ADMIN listando todos los usuarios (✅ Permitido)
```
GET {{base_url}}/users
Authorization: Bearer {{admin_token}}
```

#### 3.5. Employee intentando listar usuarios (❌ Prohibido - 403)
```
GET {{base_url}}/users
Authorization: Bearer {{employee_token}}
```

#### 3.6. DEVELOPER obteniendo usuario específico (✅ Permitido)
```
GET {{base_url}}/users/{{employee_id}}
Authorization: Bearer {{developer_token}}
```

#### 3.7. Employee viendo perfil de otro usuario (❌ Prohibido - 403)
```
GET {{base_url}}/users/{{admin_id}}
Authorization: Bearer {{employee_token}}
```

#### 3.8. ADMIN eliminando un usuario (✅ Permitido)
```
DELETE {{base_url}}/users/{{employee_id}}
Authorization: Bearer {{admin_token}}
```

#### 3.9. Developer intentando eliminar usuario (❌ Prohibido - 403)
```
DELETE {{base_url}}/users/{{employee_id}}
Authorization: Bearer {{developer_token}}
```

### Flujo 4: Probar Filtros en Lista de Usuarios

#### 4.1. Listar todos los usuarios activos
```
GET {{base_url}}/users?active=true
Authorization: Bearer {{admin_token}}
```

#### 4.2. Listar solo empleados
```
GET {{base_url}}/users?role=ROLE_EMPLOYEE
Authorization: Bearer {{admin_token}}
```

#### 4.3. Listar desarrolladores activos
```
GET {{base_url}}/users?role=ROLE_DEVELOPER&active=true
Authorization: Bearer {{admin_token}}
```

#### 4.4. Listar administradores
```
GET {{base_url}}/users?role=ROLE_ADMIN
Authorization: Bearer {{admin_token}}
```

---

## 🚨 Manejo de Errores

### Error 400 - Bad Request (Datos Inválidos)
```json
{
  "timestamp": "2025-11-19T21:30:00.123456",
  "status": 400,
  "error": "Validation Failed",
  "message": "Datos de entrada inválidos",
  "code": "VALIDATION_ERROR",
  "validationErrors": {
    "email": "El email debe tener un formato válido",
    "firstName": "El nombre no puede estar vacío"
  }
}
```

### Error 401 - Unauthorized (Token Inválido o Expirado)
```json
{
  "timestamp": "2025-11-19T21:30:00.123456",
  "status": 401,
  "error": "Unauthorized",
  "message": "Token JWT inválido o expirado",
  "code": "INVALID_TOKEN"
}
```

### Error 403 - Forbidden (Sin Permisos)
```json
{
  "timestamp": "2025-11-19T21:30:00.123456",
  "status": 403,
  "error": "Forbidden",
  "message": "No tienes permisos para modificar este usuario",
  "code": "UNAUTHORIZED_USER"
}
```

### Error 404 - Not Found (Usuario No Existe)
```json
{
  "timestamp": "2025-11-19T21:30:00.123456",
  "status": 404,
  "error": "Not Found",
  "message": "Usuario no encontrado",
  "code": "USER_NOT_FOUND"
}
```

### Error 409 - Conflict (Email Duplicado)
```json
{
  "timestamp": "2025-11-19T21:30:00.123456",
  "status": 409,
  "error": "Conflict",
  "message": "El email ya está en uso",
  "code": "EMAIL_ALREADY_EXISTS"
}
```

---

## 📝 Validaciones de Datos

### UpdateUserRequest (PUT /users/{id})
- **firstName**: 2-50 caracteres, no vacío
- **lastName**: 2-50 caracteres, no vacío  
- **email**: Formato email válido, máximo 100 caracteres

### RegisterRequest (POST /users/register)
- **firstName**: 2-50 caracteres, no vacío
- **lastName**: 2-50 caracteres, no vacío
- **email**: Formato email válido, único en el sistema
- **password**: Mínimo 8 caracteres, máximo 100

---

## 🔧 Testing con Thunder Client - Pasos Rápidos

1. **Configurar entorno** con las variables base_url y tokens
2. **Obtener tokens** para cada rol usando el endpoint de login
3. **Probar endpoints** usando los tokens correspondientes
4. **Verificar autorización** probando accesos no permitidos
5. **Validar errores** enviando datos inválidos

### Colección Recomendada Thunder Client

Crea una colección con estas peticiones en orden:

1. `Login Employee` → Guarda token en `employee_token`
2. `Login Developer` → Guarda token en `developer_token` 
3. `Login Admin` → Guarda token en `admin_token`
4. `Get My Profile` → Usa `employee_token`
5. `Update Own Info` → Employee actualiza su info
6. `Developer Updates Employee` → Developer modifica employee
7. `Employee Tries Update Admin` → Debe fallar con 403
8. `Invalid Data Update` → Debe fallar con 400
9. `Admin List All Users` → Lista todos los usuarios
10. `Employee Try List Users` → Debe fallar con 403
11. `Admin Get User By ID` → Obtiene usuario específico
12. `Employee View Other Profile` → Debe fallar con 403
13. `Admin Delete User` → Elimina usuario
14. `Developer Try Delete User` → Debe fallar con 403
15. `Filter Users by Role` → Lista usuarios por rol
16. `Filter Users by Active Status` → Lista usuarios activos

---

## 🎯 Resumen de Permisos

| Acción | EMPLOYEE | CHEF | DEVELOPER | ADMIN |
|--------|----------|------|-----------|-------|
| Ver su perfil (`/me` o `/users/{su_id}`) | ✅ | ✅ | ✅ | ✅ |
| Actualizar su info | ✅ | ✅ | ✅ | ✅ |
| Actualizar otros usuarios | ❌ | ❌ | ✅ | ✅ |
| Listar todos los usuarios | ❌ | ❌ | ✅ | ✅ |
| Ver perfil de otros usuarios | ❌ | ❌ | ✅ | ✅ |
| Eliminar usuarios | ❌ | ❌ | ❌ | ✅ |
| Registrar cuenta | ✅ | ✅ | ✅ | ✅ |
| Filtrar usuarios (por rol/estado) | ❌ | ❌ | ✅ | ✅ |

### Nuevos Endpoints Implementados ✨

| Endpoint | Método | Descripción | Permisos |
|----------|--------|-------------|----------|
| `/users` | GET | Lista todos los usuarios con filtros opcionales | ADMIN, DEVELOPER |
| `/users/{id}` | GET | Obtiene usuario específico por ID | Propio perfil: Todos / Otros: ADMIN, DEVELOPER |
| `/users/{id}` | DELETE | Elimina/desactiva usuario | Solo ADMIN |

### Parámetros de Filtro Disponibles

| Parámetro | Tipo | Descripción | Ejemplo |
|-----------|------|-------------|---------|
| `role` | String | Filtra por rol específico | `?role=ROLE_EMPLOYEE` |
| `active` | Boolean | Filtra por estado activo | `?active=true` |
| Combinados | - | Múltiples filtros | `?role=ROLE_DEVELOPER&active=true` |

¡Con esta guía deberías poder probar completamente toda la funcionalidad de usuarios usando Thunder Client! 🚀