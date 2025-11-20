# 🔐 Gestion des Mots de Passe & Endpoints d'Authentification

Ce document explique comment fonctionnent les endpoints de gestion des mots de passe et d'authentification implémentés dans StockChef Backend, et comment les tester avec Thunder Client.

## 📋 Table des Matières

1. [Endpoints Implémentés](#endpoints-implémentés)
2. [Configuration Préalable](#configuration-préalable)
3. [Tests avec Thunder Client](#tests-avec-thunder-client)
4. [Cas d'Usage](#cas-dusage)
5. [Codes d'Erreur](#codes-derreur)

## 🎯 Endpoints Implémentés

### 1. **PUT** `/users/{id}/password` - Changement de mot de passe
- **Description**: Permet de changer le mot de passe d'un utilisateur
- **Autorisation**: Utilisateur propriétaire ou ADMIN
- **Authentification**: Requise (Token JWT)

### 2. **POST** `/users/{id}/reset-password` - Reset de mot de passe (ADMIN)
- **Description**: Reset de mot de passe par un administrateur
- **Autorisation**: Uniquement ADMIN
- **Authentification**: Requise (Token JWT)

### 3. **POST** `/auth/refresh` - Renouveler le token JWT
- **Description**: Génère de nouveaux tokens en utilisant un refresh token
- **Autorisation**: Non requise
- **Authentification**: Refresh Token valide

### 4. **POST** `/auth/logout` - Invalider le token
- **Description**: Invalide le token actuel (blacklist)
- **Autorisation**: Utilisateur authentifié
- **Authentification**: Requise (Token JWT)

### 5. **POST** `/users/change-password` - Changement de mot de passe personnel
- **Description**: Changement de mot de passe de l'utilisateur authentifié
- **Autorisation**: Utilisateur authentifié
- **Authentification**: Requise (Token JWT)

### 6. **POST** `/users/forgot-password` - Demander un reset de mot de passe
- **Description**: Demande un reset de mot de passe par email
- **Autorisation**: Non requise (endpoint public)
- **Authentification**: Non requise

## ⚙️ Configuration Préalable

### 1. Variables d'Environnement Thunder Client

Créez les variables suivantes dans Thunder Client:

```json
{
  "baseUrl": "http://localhost:8080",
  "authToken": "",
  "refreshToken": "",
  "adminUserId": "1",
  "employeeUserId": "2"
}
```

### 2. Obtenir les Tokens d'Authentification

D'abord, vous devez vous authentifier pour obtenir les tokens :

**Endpoint Login (exemple) :**
```
POST {{baseUrl}}/auth/login
Content-Type: application/json

{
  "email": "admin@stockchef.com",
  "password": "password123"
}
```

Sauvegardez les tokens retournés :
- `access_token` → variable `authToken`
- `refresh_token` → variable `refreshToken`

## 🧪 Tests avec Thunder Client

### 1. **PUT** `/users/{id}/password` - Changement de mot de passe

#### En tant qu'ADMIN changeant le mot de passe d'un autre utilisateur :
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

#### En tant qu'utilisateur changeant son propre mot de passe :
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

**Réponse attendue (200) :**
```json
{
  "message": "Mot de passe mis à jour avec succès",
  "timestamp": "2025-11-20T10:30:00"
}
```

### 2. **POST** `/users/{id}/reset-password` - Reset de mot de passe (ADMIN)

```http
POST {{baseUrl}}/users/{{employeeUserId}}/reset-password
Authorization: Bearer {{authToken}}
Content-Type: application/json

{
  "newPassword": "AdminResetPass123!"
}
```

**Réponse attendue (200) :**
```json
{
  "message": "Mot de passe réinitialisé avec succès",
  "timestamp": "2025-11-20T10:30:00"
}
```

### 3. **POST** `/auth/refresh` - Renouveler le token JWT

```http
POST {{baseUrl}}/auth/refresh
Content-Type: application/json

{
  "refreshToken": "{{refreshToken}}"
}
```

**Réponse attendue (200) :**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### 4. **POST** `/auth/logout` - Invalider le token

```http
POST {{baseUrl}}/auth/logout
Authorization: Bearer {{authToken}}
Content-Type: application/json
```

**Réponse attendue (200) :**
```json
{
  "message": "Déconnexion réussie",
  "timestamp": "2025-11-20T10:30:00"
}
```

### 5. **POST** `/users/change-password` - Changement de mot de passe personnel

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

**Réponse attendue (200) :**
```json
{
  "message": "Mot de passe changé avec succès",
  "timestamp": "2025-11-20T10:30:00"
}
```

### 6. **POST** `/users/forgot-password` - Demander un reset de mot de passe

```http
POST {{baseUrl}}/users/forgot-password
Content-Type: application/json

{
  "email": "user@stockchef.com"
}
```

**Réponse attendue (200) :**
```json
{
  "message": "Si l'email existe, un lien de récupération a été envoyé",
  "timestamp": "2025-11-20T10:30:00"
}
```

## 📝 Cas d'Usage

### Scénario 1 : L'utilisateur change son propre mot de passe
1. L'utilisateur s'authentifie avec `POST /auth/login`
2. L'utilisateur change son mot de passe avec `POST /users/change-password`
3. L'utilisateur doit se ré-authentifier avec le nouveau mot de passe

### Scénario 2 : L'admin réinitialise le mot de passe d'un employé
1. L'admin s'authentifie avec `POST /auth/login`
2. L'admin réinitialise le mot de passe avec `POST /users/{id}/reset-password`
3. L'employé doit utiliser le nouveau mot de passe temporaire

### Scénario 3 : L'utilisateur oublie son mot de passe
1. L'utilisateur demande une réinitialisation avec `POST /users/forgot-password`
2. Le système envoie un email avec un lien de récupération
3. L'utilisateur suit le lien et établit un nouveau mot de passe

### Scénario 4 : Renouvellement de tokens
1. Quand l'access token expire, utiliser `POST /auth/refresh`
2. Mettre à jour les variables Thunder Client avec les nouveaux tokens
3. Continuer à utiliser l'application

### Scénario 5 : Déconnexion sécurisée
1. L'utilisateur termine sa session avec `POST /auth/logout`
2. Le token est invalidé (blacklist)
3. L'utilisateur doit se ré-authentifier

## ❌ Codes d'Erreur

### 400 - Bad Request
```json
{
  "timestamp": "2025-11-20T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Les mots de passe ne correspondent pas",
  "code": "INVALID_PASSWORD"
}
```

### 401 - Unauthorized
```json
{
  "timestamp": "2025-11-20T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Token invalide ou expiré",
  "code": "INVALID_TOKEN"
}
```

### 403 - Forbidden
```json
{
  "timestamp": "2025-11-20T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Vous n'avez pas les permissions pour effectuer cette action",
  "code": "INSUFFICIENT_PERMISSIONS"
}
```

### 404 - Not Found
```json
{
  "timestamp": "2025-11-20T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Utilisateur non trouvé",
  "code": "USER_NOT_FOUND"
}
```

## 🔧 Conseils pour les Tests

### 1. **Collection Thunder Client**
Créez une collection spécifique pour ces endpoints et organisez-les par fonctionnalité :
- **Gestion des Mots de Passe**
  - Changer son Propre Mot de Passe
  - Admin Change Mot de Passe
  - Admin Reset Mot de Passe
  - Mot de Passe Oublié
- **Authentification**
  - Refresh Token
  - Déconnexion

### 2. **Variables Dynamiques**
Utilisez les variables Thunder Client pour rendre les tests plus efficaces :
```javascript
// Dans l'onglet Tests de Thunder Client
tc.setVar("authToken", json.accessToken);
tc.setVar("refreshToken", json.refreshToken);
```

### 3. **Headers Communs**
Configurez des headers par défaut pour la collection :
```
Authorization: Bearer {{authToken}}
Content-Type: application/json
Accept: application/json
```

### 4. **Tests d'Erreurs**
N'oubliez pas de tester les cas d'erreur :
- Mots de passe qui ne correspondent pas
- Tokens expirés ou invalides
- Utilisateurs sans permissions
- IDs d'utilisateur inexistants

### 5. **Séquence de Tests**
Suivez cette séquence pour des tests complets :
1. Login pour obtenir les tokens
2. Tester les cas de succès
3. Tester les cas d'erreur
4. Tester l'autorisation (différents rôles)
5. Déconnexion et vérifier l'invalidation

## 🛡️ Considérations de Sécurité

### Validations Implémentées :
- ✅ **Longueur minimale du mot de passe** : 8 caractères
- ✅ **Confirmation du mot de passe** : Doit correspondre
- ✅ **Autorisation par rôles** : ADMIN vs EMPLOYEE
- ✅ **Vérification du mot de passe actuel** : Pour les changements
- ✅ **Tokens JWT** : Expiration et validation
- ✅ **Rate limiting** : Protection contre les attaques par force brute
- ✅ **Logging d'audit** : Toutes les opérations sont enregistrées

### Pour la Production :
- 🔒 **HTTPS** : Tous les endpoints doivent utiliser HTTPS
- 🔒 **Chiffrement** : Mots de passe hachés avec bcrypt
- 🔒 **Email réel** : Implémenter l'envoi d'emails pour forgot-password
- 🔒 **Expiration de tokens** : Configurer des temps appropriés
- 🔒 **Blacklist de tokens** : Implémenter un stockage persistant

---

**Bons Tests ! 🚀**

Pour plus d'informations sur l'API, consultez la documentation complète de StockChef Backend.