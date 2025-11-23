# Test Data Documentation

## Developer Account Credentials
Para autenticación en tests y desarrollo:

### Login Request
```json
{
    "email": "developer@stockchef.com",
    "password": "devpass123"
}
```

### Login Response
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiUk9MRV9ERVZFTE9QRVIiLCJmdWxsTmFtZSI6IkRldmVsb3BlciBBZG1pbiIsInVzZXJJZCI6IjQyNzBkMTJiLWY2MDMtNDQ2MC05NTlhLTc5YWFlMDQ1ZTEyMSIsInN1YiI6ImRldmVsb3BlckBzdG9ja2NoZWYuY29tIiwiaWF0IjoxNzYzODY1MDk0LCJleHAiOjE3NjM5NTE0OTR9.FYD_In6HKMa6tBG91pPPSgpdocdv0_Dvv4b_MfwMcRU",
    "email": "developer@stockchef.com",
    "fullName": "Developer Admin",
    "role": "ROLE_DEVELOPER",
    "expiresIn": 86400000
}
```

## Base de Datos - Usuarios Existentes

### Usuario Developer (para crear admins en tests)
```json
{
    "id": "4270d12b-f603-4460-959a-79aae045e121",
    "email": "developer@stockchef.com",
    "firstName": "Developer",
    "lastName": "Admin",
    "fullName": "Developer Admin",
    "role": "ROLE_DEVELOPER",
    "effectiveRole": "ROLE_DEVELOPER",
    "isActive": true,
    "createdAt": "2025-11-19T18:57:50.88619",
    "lastLoginAt": null,
    "createdBy": "system"
}
```

### Usuario Admin
```json
{
    "id": "07521955-5984-485e-a805-9b60ebde8768",
    "email": "admin@stockchef.com",
    "firstName": "Admin",
    "lastName": "User",
    "fullName": "Admin User",
    "role": "ROLE_ADMIN",
    "effectiveRole": "ROLE_ADMIN",
    "isActive": true,
    "createdAt": "2025-11-19T18:57:51.021304",
    "lastLoginAt": null,
    "createdBy": "system"
}
```

### Usuario Chef
```json
{
    "id": "a6704f23-6e28-4b8f-9353-2ae3567955f6",
    "email": "chef@stockchef.com",
    "firstName": "Head",
    "lastName": "Chef",
    "fullName": "Head Chef",
    "role": "ROLE_CHEF",
    "effectiveRole": "ROLE_CHEF",
    "isActive": true,
    "createdAt": "2025-11-19T18:57:51.098009",
    "lastLoginAt": null,
    "createdBy": "system"
}
```

### Usuario Employee (Inactivo)
```json
{
    "id": "5b2a5217-3f21-42bc-8cf6-a91bbc205196",
    "email": "employee@stockchef.com",
    "firstName": "Kitchen",
    "lastName": "Employee",
    "fullName": "Kitchen Employee",
    "role": "ROLE_EMPLOYEE",
    "effectiveRole": null,
    "isActive": false,
    "createdAt": "2025-11-19T18:57:51.176525",
    "lastLoginAt": null,
    "createdBy": "system"
}
```

### Usuario Employee (Activo)
```json
{
    "id": "d1c88254-f4e3-4ec0-90ec-2399e005b87d",
    "email": "test@stockchef.com",
    "firstName": "Test",
    "lastName": "User",
    "fullName": "Test User",
    "role": "ROLE_EMPLOYEE",
    "effectiveRole": "ROLE_EMPLOYEE",
    "isActive": true,
    "createdAt": "2025-11-19T20:13:31.898663",
    "lastLoginAt": null,
    "createdBy": "system"
}
```

## Notas para Tests

### Permisos por Rol:
- **DEVELOPER**: Puede crear cualquier rol (ADMIN, CHEF, EMPLOYEE, DEVELOPER)
- **ADMIN**: Puede crear CHEF y EMPLOYEE, pero NO DEVELOPER
- **CHEF**: Sin permisos administrativos
- **EMPLOYEE**: Sin permisos administrativos

### URLs de Endpoints Admin:
- `GET /admin/users` - Listar usuarios
- `PUT /admin/users/{id}/role` - Actualizar rol
- `PUT /admin/users/{id}/status` - Actualizar status activo/inactivo

### Para Tests de Integración:
Usar el usuario `developer@stockchef.com` con token JWT para crear usuarios ADMIN adicionales para pruebas.