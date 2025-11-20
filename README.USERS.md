# 👥 API de Gestion des Utilisateurs - StockChef

Ce document décrit tous les endpoints liés aux utilisateurs et comment les tester en utilisant Thunder Client.

## 📋 Index

1. [Authentification](#authentification)
2. [Endpoints Publics](#endpoints-publics)
3. [Endpoints Protégés](#endpoints-protégés)
4. [Configuration Thunder Client](#configuration-thunder-client)
5. [Variables d'Environnement](#variables-denvironnement)
6. [Cas d'Usage Complets](#cas-dusage-complets)

---

## 🔐 Authentification

Tous les endpoints protégés nécessitent un token JWT dans le header `Authorization: Bearer <token>`.

### Système de Rôles

- **ROLE_EMPLOYEE**: Utilisateur de base
- **ROLE_CHEF**: Cuisinier avec des permissions supplémentaires
- **ROLE_DEVELOPER**: Développeur avec des permissions administratives
- **ROLE_ADMIN**: Administrateur avec les permissions maximales

---

## 🌐 Endpoints Publics

### 1. Inscription d'Utilisateur

**Permet à n'importe quel utilisateur de créer un compte avec le rôle EMPLOYEE par défaut**

```http
POST http://localhost:8090/api/users/register
Content-Type: application/json

{
  "firstName": "Jean",
  "lastName": "Dupont",
  "email": "jean.dupont@stockchef.com",
  "password": "MonMotDePasse123!"
}
```

**Réponse Réussie (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "email": "jean.dupont@stockchef.com",
  "firstName": "Jean",
  "lastName": "Dupont",
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
  "firstName": "Jean",
  "lastName": "Dupont",
  "email": "jean.dupont@stockchef.com",
  "password": "MonMotDePasse123!"
}
```

### 2. Connexion Utilisateur

**Obtient un token JWT pour l'authentification**

```http
POST http://localhost:8090/api/auth/login
Content-Type: application/json

{
  "email": "employee@stockchef.com",
  "password": "employee123"
}
```

**Réponse Réussie (200 OK):**
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

## 🔒 Endpoints Protégés

### 3. Obtenir le Profil Actuel

**Obtient les informations de l'utilisateur authentifié**

```http
GET http://localhost:8090/api/users/me
Authorization: Bearer {{jwt_token}}
```

**Réponse Réussie (200 OK):**
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

### 4. Mettre à Jour les Informations Personnelles

**Permet de mettre à jour firstName, lastName et email de l'utilisateur**

#### 4.1. Mettre à Jour ses Propres Informations (Tous les rôles)

```http
PUT http://localhost:8090/api/users/550e8400-e29b-41d4-a716-446655440040
Authorization: Bearer {{employee_token}}
Content-Type: application/json

{
  "firstName": "Employe",
  "lastName": "MisAJour",
  "email": "employe.nouveau@stockchef.com"
}
```

**Réponse Réussie (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440040",
  "email": "employe.nouveau@stockchef.com",
  "firstName": "Employe",
  "lastName": "MisAJour",
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
  "firstName": "Employe",
  "lastName": "MisAJour",
  "email": "employe.nouveau@stockchef.com"
}
```

#### 4.2. DEVELOPER/ADMIN Mettant à Jour d'Autres Utilisateurs

```http
PUT http://localhost:8090/api/users/550e8400-e29b-41d4-a716-446655440040
Authorization: Bearer {{developer_token}}
Content-Type: application/json

{
  "firstName": "Utilisateur",
  "lastName": "Modifie par Dev",
  "email": "modifie.par.dev@stockchef.com"
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
  "firstName": "Utilisateur",
  "lastName": "Modifie par Dev",
  "email": "modifie.par.dev@stockchef.com"
}
```

### 5. Lister Tous les Utilisateurs

**Obtient la liste de tous les utilisateurs (Seulement ADMIN et DEVELOPER)**

#### 5.1. Lister Tous les Utilisateurs

```http
GET http://localhost:8090/api/users
Authorization: Bearer {{admin_token}}
```

**Réponse Réussie (200 OK):**
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

#### 5.2. Filtrer par Rôle

```http
GET http://localhost:8090/api/users?role=ROLE_EMPLOYEE
Authorization: Bearer {{admin_token}}
```

#### 5.3. Filtrer par État Actif

```http
GET http://localhost:8090/api/users?active=true
Authorization: Bearer {{admin_token}}
```

#### 5.4. Filtrer par Rôle et État

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

**Avec filtres:**
```
Method: GET
URL: http://localhost:8090/api/users?role=ROLE_EMPLOYEE&active=true
Headers:
  Authorization: Bearer {{admin_token}}
```

### 6. Obtenir Utilisateur par ID

**Récupère les informations d'un utilisateur spécifique**

#### 6.1. Voir Son Propre Profil par ID (Tous les rôles)

```http
GET http://localhost:8090/api/users/550e8400-e29b-41d4-a716-446655440040
Authorization: Bearer {{employee_token}}
```

**Réponse Réussie (200 OK):**
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

#### 6.2. ADMIN/DEVELOPER Consulter Tout Utilisateur

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

### 7. Supprimer Utilisateur

**Désactive un utilisateur du système (ADMIN uniquement)**

```http
DELETE http://localhost:8090/api/users/550e8400-e29b-41d4-a716-446655440040
Authorization: Bearer {{admin_token}}
```

**Réponse Réussie (204 No Content):**
```
(Pas de contenu dans la réponse)
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

#### 4.2. DEVELOPER/ADMIN Mettre à Jour Autres Utilisateurs

```http
PUT http://localhost:8090/api/users/550e8400-e29b-41d4-a716-446655440040
Authorization: Bearer {{developer_token}}
Content-Type: application/json

{
  "firstName": "Utilisateur",
  "lastName": "Modifié par Dev",
  "email": "modifie.par.dev@stockchef.com"
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
  "firstName": "Utilisateur",
  "lastName": "Modifié par Dev",
  "email": "modifie.par.dev@stockchef.com"
}
```

---

## ⚙️ Configuration Thunder Client

### Variables d'Environnement

Créez un environnement dans Thunder Client avec ces variables :

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

### Utilisateurs Prédéfinis dans le Système

| Email | Mot de passe | Rôle | UUID |
|-------|----------|-----|------|
| `employee@stockchef.com` | `employee123` | ROLE_EMPLOYEE | `550e8400-e29b-41d4-a716-446655440040` |
| `chef@stockchef.com` | `chef123` | ROLE_CHEF | `550e8400-e29b-41d4-a716-446655440050` |
| `developer@stockchef.com` | `developer123` | ROLE_DEVELOPER | `550e8400-e29b-41d4-a716-446655440020` |
| `admin@stockchef.com` | `admin123` | ROLE_ADMIN | `550e8400-e29b-41d4-a716-446655440010` |

---

## 🧪 Cas d'Usage Complets

### Flux 1 : Inscription et Connexion d'un Nouvel Utilisateur

1. **Enregistrer un utilisateur :**
```
POST {{base_url}}/users/register
{
  "firstName": "Test",
  "lastName": "User",
  "email": "test.user@stockchef.com",
  "password": "TestPassword123!"
}
```

2. **Se connecter :**
```
POST {{base_url}}/auth/login
{
  "email": "test.user@stockchef.com",
  "password": "TestPassword123!"
}
```

3. **Sauvegarder le token** et l'utiliser pour les requêtes suivantes

### Flux 2 : Obtenir des Tokens pour Tous les Rôles

#### 2.1. Token d'Employee
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

#### 2.4. Token d'Admin
```
POST {{base_url}}/auth/login
{
  "email": "admin@stockchef.com",
  "password": "admin123"
}
```

### Flux 3 : Tester l'Autorisation et les Nouveaux Endpoints

#### 3.1. Employee mettant à jour ses propres informations (✅ Autorisé)
```
PUT {{base_url}}/users/{{employee_id}}
Authorization: Bearer {{employee_token}}
{
  "firstName": "Employee",
  "lastName": "Updated",
  "email": "employee.updated@stockchef.com"
}
```

#### 3.2. Employee tentant de mettre à jour un autre utilisateur (❌ Interdit - 403)
```
PUT {{base_url}}/users/{{admin_id}}
Authorization: Bearer {{employee_token}}
{
  "firstName": "Should",
  "lastName": "Fail",
  "email": "should.fail@stockchef.com"
}
```

#### 3.3. Developer mettant à jour tout utilisateur (✅ Autorisé)
```
PUT {{base_url}}/users/{{employee_id}}
Authorization: Bearer {{developer_token}}
{
  "firstName": "Modifié",
  "lastName": "Par Developer",
  "email": "modifie.par.dev@stockchef.com"
}
```

#### 3.4. ADMIN listant tous les utilisateurs (✅ Autorisé)
```
GET {{base_url}}/users
Authorization: Bearer {{admin_token}}
```

#### 3.5. Employee tentant de lister les utilisateurs (❌ Interdit - 403)
```
GET {{base_url}}/users
Authorization: Bearer {{employee_token}}
```

#### 3.6. DEVELOPER obtenant un utilisateur spécifique (✅ Autorisé)
```
GET {{base_url}}/users/{{employee_id}}
Authorization: Bearer {{developer_token}}
```

#### 3.7. Employee consultant le profil d'un autre utilisateur (❌ Interdit - 403)
```
GET {{base_url}}/users/{{admin_id}}
Authorization: Bearer {{employee_token}}
```

#### 3.8. ADMIN supprimant un utilisateur (✅ Autorisé)
```
DELETE {{base_url}}/users/{{employee_id}}
Authorization: Bearer {{admin_token}}
```

#### 3.9. Developer tentant de supprimer un utilisateur (❌ Interdit - 403)
```
DELETE {{base_url}}/users/{{employee_id}}
Authorization: Bearer {{developer_token}}
```

### Flux 4 : Tester les Filtres dans la Liste des Utilisateurs

#### 4.1. Lister tous les utilisateurs actifs
```
GET {{base_url}}/users?active=true
Authorization: Bearer {{admin_token}}
```

#### 4.2. Lister uniquement les employés
```
GET {{base_url}}/users?role=ROLE_EMPLOYEE
Authorization: Bearer {{admin_token}}
```

#### 4.3. Lister les développeurs actifs
```
GET {{base_url}}/users?role=ROLE_DEVELOPER&active=true
Authorization: Bearer {{admin_token}}
```

#### 4.4. Lister les administrateurs
```
GET {{base_url}}/users?role=ROLE_ADMIN
Authorization: Bearer {{admin_token}}
```

---

## 🚨 Gestion des Erreurs

### Erreur 400 - Bad Request (Données Invalides)
```json
{
  "timestamp": "2025-11-19T21:30:00.123456",
  "status": 400,
  "error": "Validation Failed",
  "message": "Données d'entrée invalides",
  "code": "VALIDATION_ERROR",
  "validationErrors": {
    "email": "L'email doit avoir un format valide",
    "firstName": "Le prénom ne peut pas être vide"
  }
}
```

### Erreur 401 - Unauthorized (Token Invalide ou Expiré)
```json
{
  "timestamp": "2025-11-19T21:30:00.123456",
  "status": 401,
  "error": "Unauthorized",
  "message": "Token JWT invalide ou expiré",
  "code": "INVALID_TOKEN"
}
```

### Erreur 403 - Forbidden (Sans Permissions)
```json
{
  "timestamp": "2025-11-19T21:30:00.123456",
  "status": 403,
  "error": "Forbidden",
  "message": "Vous n'avez pas les permissions pour modifier cet utilisateur",
  "code": "UNAUTHORIZED_USER"
}
```

### Erreur 404 - Not Found (Utilisateur N'Existe Pas)
```json
{
  "timestamp": "2025-11-19T21:30:00.123456",
  "status": 404,
  "error": "Not Found",
  "message": "Utilisateur non trouvé",
  "code": "USER_NOT_FOUND"
}
```

### Erreur 409 - Conflict (Email Dupliqué)
```json
{
  "timestamp": "2025-11-19T21:30:00.123456",
  "status": 409,
  "error": "Conflict",
  "message": "L'email est déjà utilisé",
  "code": "EMAIL_ALREADY_EXISTS"
}
```

---

## 📝 Validations des Données

### UpdateUserRequest (PUT /users/{id})
- **firstName** : 2-50 caractères, non vide
- **lastName** : 2-50 caractères, non vide  
- **email** : Format email valide, maximum 100 caractères

### RegisterRequest (POST /users/register)
- **firstName** : 2-50 caractères, non vide
- **lastName** : 2-50 caractères, non vide
- **email** : Format email valide, unique dans le système
- **password** : Minimum 8 caractères, maximum 100

---

## 🔧 Tests avec Thunder Client - Étapes Rapides

1. **Configurer l'environnement** avec les variables base_url et tokens
2. **Obtenir des tokens** pour chaque rôle en utilisant l'endpoint de connexion
3. **Tester les endpoints** en utilisant les tokens correspondants
4. **Vérifier l'autorisation** en testant les accès non autorisés
5. **Valider les erreurs** en envoyant des données invalides

### Collection Recommandée Thunder Client

Créez une collection avec ces requêtes dans l'ordre :

1. `Login Employee` → Sauvegarde le token dans `employee_token`
2. `Login Developer` → Sauvegarde le token dans `developer_token` 
3. `Login Admin` → Sauvegarde le token dans `admin_token`
4. `Get My Profile` → Utilise `employee_token`
5. `Update Own Info` → Employee met à jour ses infos
6. `Developer Updates Employee` → Developer modifie l'employé
7. `Employee Tries Update Admin` → Doit échouer avec 403
8. `Invalid Data Update` → Doit échouer avec 400
9. `Admin List All Users` → Liste tous les utilisateurs
10. `Employee Try List Users` → Doit échouer avec 403
11. `Admin Get User By ID` → Obtient un utilisateur spécifique
12. `Employee View Other Profile` → Doit échouer avec 403
13. `Admin Delete User` → Supprime un utilisateur
14. `Developer Try Delete User` → Doit échouer avec 403
15. `Filter Users by Role` → Liste les utilisateurs par rôle
16. `Filter Users by Active Status` → Liste les utilisateurs actifs

---

## 🎯 Résumé des Permissions

| Action | EMPLOYEE | CHEF | DEVELOPER | ADMIN |
|--------|----------|------|-----------|-------|
| Voir son profil (`/me` ou `/users/{son_id}`) | ✅ | ✅ | ✅ | ✅ |
| Mettre à jour ses infos | ✅ | ✅ | ✅ | ✅ |
| Mettre à jour d'autres utilisateurs | ❌ | ❌ | ✅ | ✅ |
| Lister tous les utilisateurs | ❌ | ❌ | ✅ | ✅ |
| Voir le profil d'autres utilisateurs | ❌ | ❌ | ✅ | ✅ |
| Supprimer des utilisateurs | ❌ | ❌ | ❌ | ✅ |
| Créer un compte | ✅ | ✅ | ✅ | ✅ |
| Filtrer les utilisateurs (par rôle/état) | ❌ | ❌ | ✅ | ✅ |

### Nouveaux Endpoints Implémentés ✨

| Endpoint | Méthode | Description | Permissions |
|----------|--------|-------------|----------|
| `/users` | GET | Liste tous les utilisateurs avec filtres optionnels | ADMIN, DEVELOPER |
| `/users/{id}` | GET | Obtient un utilisateur spécifique par ID | Propre profil : Tous / Autres : ADMIN, DEVELOPER |
| `/users/{id}` | DELETE | Supprime/désactive un utilisateur | ADMIN uniquement |

### Paramètres de Filtre Disponibles

| Paramètre | Type | Description | Exemple |
|-----------|------|-------------|---------|  
| `role` | String | Filtre par rôle spécifique | `?role=ROLE_EMPLOYEE` |
| `active` | Boolean | Filtre par état actif | `?active=true` |
| Combinés | - | Multiples filtres | `?role=ROLE_DEVELOPER&active=true` |

Avec ce guide vous devriez pouvoir tester complètement toute la fonctionnalité des utilisateurs en utilisant Thunder Client ! 🚀