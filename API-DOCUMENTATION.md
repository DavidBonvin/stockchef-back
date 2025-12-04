# StockChef Backend API - Documentation Complète

## 📋 Table des Matières
1. [Vue d'ensemble](#vue-densemble)
2. [Authentification](#authentification)
3. [Structure de Base de Données](#structure-de-base-de-données)
4. [Endpoints API](#endpoints-api)
5. [Gestion des Utilisateurs](#gestion-des-utilisateurs)
6. [Gestion d'Inventaire](#gestion-dinventaire)
7. [Gestion des Menus](#gestion-des-menus)
8. [Modèles de Données](#modèles-de-données)
9. [Exemples de Requêtes](#exemples-de-requêtes)
10. [Gestion des Erreurs](#gestion-des-erreurs)

---

## 🎯 Vue d'ensemble

**StockChef Backend API** est un système complet de gestion d'inventaire de restaurant avec authentification utilisateur, suivi des stocks, et gestion des menus.

- **Base URL**: `http://localhost:8090/api`
- **Framework**: Spring Boot 3.5.0
- **Base de données**: PostgreSQL 15 / MySQL 8.4
- **Authentification**: JWT (JSON Web Token)
- **Déploiement**: Docker + Docker Compose

---

## 🔐 Authentification

### Système de Rôles (Hiérarchique)

| Rôle | Description | Permissions |
|------|-------------|-------------|
| **ROLE_DEVELOPER** | Super administrateur | Accès total au système |
| **ROLE_ADMIN** | Administrateur restaurant | Gestion utilisateurs + inventaire + menus |
| **ROLE_CHEF** | Chef de cuisine | Gestion inventaire + menus + préparation |
| **ROLE_EMPLOYEE** | Employé | Consultation inventaire (lecture seule) |

### Token JWT
Inclure le token dans l'header `Authorization` :
```
Authorization: Bearer <your-jwt-token>
```

### Utilisateurs par défaut
```bash
# Developer (Super-Admin)
Email: developer@stockchef.com
Mot de passe: devpass123

# Administrator  
Email: admin@stockchef.com
Mot de passe: adminpass123

# Chef
Email: chef@stockchef.com  
Mot de passe: chefpass123

# Employee
Email: employee@stockchef.com
Mot de passe: emppass123
```

---

## 🗄️ Structure de Base de Données

### Table `users`
| Colonne | Type | Description |
|---------|------|-------------|
| `id` | VARCHAR(36) | UUID unique |
| `email` | VARCHAR | Email unique (username) |
| `password` | VARCHAR | Mot de passe hashé (BCrypt) |
| `first_name` | VARCHAR | Prénom |
| `last_name` | VARCHAR | Nom de famille |
| `role` | ENUM | Rôle utilisateur |
| `is_active` | BOOLEAN | Utilisateur actif/inactif |
| `created_at` | TIMESTAMP | Date de création |
| `updated_at` | TIMESTAMP | Dernière modification |
| `last_login_at` | TIMESTAMP | Dernière connexion |
| `created_by` | VARCHAR | Créateur du compte |

### Table `produits` (Inventaire)
| Colonne | Type | Description |
|---------|------|-------------|
| `id` | BIGINT | ID auto-incrémenté |
| `nom` | VARCHAR(100) | Nom du produit |
| `quantite_stock` | DECIMAL(10,3) | Quantité en stock |
| `unite` | ENUM | Unité de mesure |
| `prix_unitaire` | DECIMAL(10,2) | Prix par unité |
| `seuil_alerte` | DECIMAL(10,3) | Seuil d'alerte stock |
| `date_peremption` | DATE | Date d'expiration |
| `date_entree` | TIMESTAMP | Date d'ajout |
| `last_modified` | TIMESTAMP | Dernière modification |
| `deleted` | BOOLEAN | Suppression logique |

### Table `menus`
| Colonne | Type | Description |
|---------|------|-------------|
| `id` | BIGINT | ID auto-incrémenté |
| `nom` | VARCHAR(100) | Nom du menu |
| `description` | VARCHAR(500) | Description |
| `date_service` | DATE | Date de service |
| `nombre_portions` | INTEGER | Nombre de portions |
| `prix_vente` | DECIMAL(10,2) | Prix de vente |
| `statut` | ENUM | Statut du menu |
| `chef_responsable` | VARCHAR(100) | Chef responsable |
| `date_creation` | TIMESTAMP | Date de création |
| `date_modification` | TIMESTAMP | Dernière modification |
| `cout_total_ingredients` | DECIMAL(10,2) | Coût total calculé |
| `marge_percentage` | DECIMAL(5,2) | Marge bénéficiaire |

### Table `menu_ingredients` (Relation)
| Colonne | Type | Description |
|---------|------|-------------|
| `id` | BIGINT | ID auto-incrémenté |
| `menu_id` | BIGINT | Référence menu |
| `produit_id` | BIGINT | Référence produit |
| `quantite_necessaire` | DECIMAL(10,3) | Quantité requise |
| `unite_utilisee` | ENUM | Unité utilisée |
| `quantite_convertie` | DECIMAL(10,3) | Quantité convertie |
| `cout_ingredient` | DECIMAL(10,2) | Coût de l'ingrédient |
| `notes` | VARCHAR | Notes spéciales |

### Table `stock_movements` (Historique)
| Colonne | Type | Description |
|---------|------|-------------|
| `id` | BIGINT | ID auto-incrémenté |
| `produit_id` | BIGINT | Référence produit |
| `type_mouvement` | ENUM | Type (ENTREE/SORTIE) |
| `quantite` | DECIMAL(10,3) | Quantité déplacée |
| `quantite_avant` | DECIMAL(10,3) | Stock avant |
| `quantite_apres` | DECIMAL(10,3) | Stock après |
| `motif` | VARCHAR | Raison du mouvement |
| `date_mouvement` | TIMESTAMP | Date du mouvement |
| `utilisateur` | VARCHAR | Utilisateur responsable |

---

## 🌐 Endpoints API

### 🏠 Endpoints de Base

#### Status et Health Check
| Méthode | Endpoint | Rôle Requis | Description |
|---------|----------|-------------|-------------|
| **GET** | `/` | Aucun | Status API racine |
| **GET** | `/health` | Aucun | Health check détaillé |
| **GET** | `/health/ping` | Aucun | Ping simple |

**Exemple - Health Check :**
```http
GET http://localhost:8090/api/health
```
**Réponse :**
```json
{
  "status": "UP",
  "message": "StockChef Backend fonctionne correctement",
  "timestamp": "2025-12-04T15:30:00",
  "version": "0.0.1-SNAPSHOT",
  "service": "stockchef-back",
  "server_port": "8090",
  "profile": "postgresql"
}
```

---

## 👥 Gestion des Utilisateurs

### 🔓 Endpoints Publics

#### Inscription Utilisateur
| Méthode | Endpoint | Rôle Requis | Description |
|---------|----------|-------------|-------------|
| **POST** | `/users/register` | Aucun | Création compte (ROLE_EMPLOYEE par défaut) |

**Exemple :**
```http
POST /api/users/register
Content-Type: application/json

{
  "firstName": "Jean",
  "lastName": "Dupont", 
  "email": "jean.dupont@stockchef.com",
  "password": "MonMotDePasse123!"
}
```
**Réponse (201 Created) :**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "email": "jean.dupont@stockchef.com",
  "firstName": "Jean",
  "lastName": "Dupont",
  "fullName": "Jean Dupont",
  "role": "ROLE_EMPLOYEE",
  "effectiveRole": "ROLE_EMPLOYEE",
  "isActive": true,
  "createdAt": "2025-12-04T15:30:00",
  "lastLoginAt": null,
  "createdBy": "system"
}
```

#### Connexion Utilisateur  
| Méthode | Endpoint | Rôle Requis | Description |
|---------|----------|-------------|-------------|
| **POST** | `/auth/login` | Aucun | Authentification et obtention token JWT |

**Exemple :**
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "chef@stockchef.com",
  "password": "chefpass123"
}
```
**Réponse (200 OK) :**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjaGVmQHN0b2NrY2hlZi5jb20iLCJpYXQiOjE3MzM0MDU1MzUsImV4cCI6MTczMzQ5MTkzNX0.VXGZtWGE7zrOOGLXHN6EWVOgKbWlJK-zrX5WGTueSUU",
  "user": {
    "id": "a2d3f7b7-9863-4ba6-8cb4-a231faf65728",
    "email": "chef@stockchef.com",
    "firstName": "Head",
    "lastName": "Chef",
    "fullName": "Head Chef",
    "role": "ROLE_CHEF",
    "effectiveRole": "ROLE_CHEF",
    "isActive": true,
    "createdAt": "2025-12-04T12:00:00",
    "lastLoginAt": "2025-12-04T15:30:00"
  },
  "expiresIn": 86400000
}
```

### 🔒 Endpoints Protégés

#### Profil Utilisateur Actuel
| Méthode | Endpoint | Rôle Requis | Description |
|---------|----------|-------------|-------------|
| **GET** | `/users/me` | Authentifié | Profil utilisateur connecté |

**Exemple :**
```http
GET /api/users/me
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### Gestion des Utilisateurs
| Méthode | Endpoint | Rôle Requis | Description |
|---------|----------|-------------|-------------|
| **GET** | `/users/{id}` | Propriétaire/ADMIN/DEVELOPER | Détails utilisateur spécifique |
| **PUT** | `/users/{id}` | Propriétaire/ADMIN/DEVELOPER | Mise à jour informations |
| **DELETE** | `/users/{id}` | ADMIN/DEVELOPER | Suppression utilisateur |
| **PUT** | `/users/{id}/password` | Propriétaire/ADMIN | Changement mot de passe |
| **POST** | `/users/{id}/reset-password` | ADMIN/DEVELOPER | Reset mot de passe |

#### Administration
| Méthode | Endpoint | Rôle Requis | Description |
|---------|----------|-------------|-------------|
| **GET** | `/admin/users` | ADMIN/DEVELOPER | Liste tous les utilisateurs |
| **PUT** | `/admin/users/{id}/role` | ADMIN/DEVELOPER | Modification rôle |
| **PUT** | `/admin/users/{id}/status` | ADMIN/DEVELOPER | Activation/désactivation |

**Exemple - Liste utilisateurs (Admin) :**
```http
GET /api/admin/users
Authorization: Bearer <admin_token>
```
**Réponse :**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440010",
    "email": "admin@stockchef.com",
    "firstName": "Admin",
    "lastName": "User", 
    "role": "ROLE_ADMIN",
    "effectiveRole": "ROLE_ADMIN",
    "isActive": true,
    "createdAt": "2025-12-04T12:00:00"
  },
  {
    "id": "550e8400-e29b-41d4-a716-446655440020",
    "email": "chef@stockchef.com",
    "firstName": "Head",
    "lastName": "Chef",
    "role": "ROLE_CHEF", 
    "effectiveRole": "ROLE_CHEF",
    "isActive": true,
    "createdAt": "2025-12-04T12:00:00"
  }
]
```

---

## 📦 Gestion d'Inventaire

### Endpoints Produits
| Méthode | Endpoint | Rôle Requis | Description |
|---------|----------|-------------|-------------|
| **POST** | `/inventory/produits` | CHEF/ADMIN/DEVELOPER | Création nouveau produit |
| **GET** | `/inventory/produits` | CHEF/ADMIN/DEVELOPER | Liste tous les produits |
| **GET** | `/inventory/produits/page` | CHEF/ADMIN/DEVELOPER | Liste paginée |
| **GET** | `/inventory/produits/{id}` | CHEF/ADMIN/DEVELOPER | Détails produit |
| **PUT** | `/inventory/produits/{id}` | CHEF/ADMIN/DEVELOPER | Mise à jour produit |
| **DELETE** | `/inventory/produits/{id}` | CHEF/ADMIN/DEVELOPER | Suppression produit |

### Gestion des Stocks
| Méthode | Endpoint | Rôle Requis | Description |
|---------|----------|-------------|-------------|
| **POST** | `/inventory/produits/{id}/entree` | CHEF/ADMIN/DEVELOPER | Entrée de stock |
| **POST** | `/inventory/produits/{id}/sortie` | CHEF/ADMIN/DEVELOPER | Sortie de stock |
| **GET** | `/inventory/produits/{id}/movements` | CHEF/ADMIN/DEVELOPER | Historique mouvements |

### Alertes et Recherche
| Méthode | Endpoint | Rôle Requis | Description |
|---------|----------|-------------|-------------|
| **GET** | `/inventory/produits/alerts` | CHEF/ADMIN/DEVELOPER | Produits sous seuil d'alerte |
| **GET** | `/inventory/produits/expiring` | CHEF/ADMIN/DEVELOPER | Produits expirant bientôt |
| **GET** | `/inventory/produits/search` | CHEF/ADMIN/DEVELOPER | Recherche par nom |

**Exemple - Création produit :**
```http
POST /api/inventory/produits
Authorization: Bearer <chef_token>
Content-Type: application/json

{
  "nom": "Tomates cerises",
  "quantiteStock": 5.000,
  "unite": "KILOGRAMME",
  "prixUnitaire": 8.50,
  "seuilAlerte": 1.000,
  "datePeremption": "2025-12-10"
}
```
**Réponse (201 Created) :**
```json
{
  "id": 1,
  "nom": "Tomates cerises",
  "quantiteStock": 5.000,
  "unite": "KILOGRAMME", 
  "unitSymbol": "kg",
  "prixUnitaire": 8.50,
  "seuilAlerte": 1.000,
  "datePeremption": "2025-12-10",
  "dateEntree": "2025-12-04T15:30:00",
  "lastModified": "2025-12-04T15:30:00",
  "isUnderAlertThreshold": false,
  "isExpired": false,
  "deleted": false
}
```

**Exemple - Entrée de stock :**
```http
POST /api/inventory/produits/1/entree
Authorization: Bearer <chef_token>
Content-Type: application/json

{
  "quantite": 2.500,
  "motif": "Livraison fournisseur",
  "notes": "Produits frais - qualité excellente"
}
```

**Exemple - Produits en alerte :**
```http
GET /api/inventory/produits/alerts
Authorization: Bearer <chef_token>
```
**Réponse :**
```json
[
  {
    "id": 3,
    "nom": "Huile d'olive",
    "quantiteStock": 0.250,
    "seuilAlerte": 0.500,
    "unite": "LITRE",
    "isUnderAlertThreshold": true,
    "quantiteManquante": 0.250
  }
]
```

---

## 🍽️ Gestion des Menus

### Endpoints Menus
| Méthode | Endpoint | Rôle Requis | Description |
|---------|----------|-------------|-------------|
| **POST** | `/menus` | CHEF/ADMIN/DEVELOPER | Création nouveau menu |
| **GET** | `/menus` | CHEF/ADMIN/DEVELOPER | Liste des menus (paginée) |
| **GET** | `/menus/{id}` | CHEF/ADMIN/DEVELOPER | Détails menu |
| **PUT** | `/menus/{id}` | CHEF/ADMIN/DEVELOPER | Mise à jour menu |
| **DELETE** | `/menus/{id}` | CHEF/ADMIN/DEVELOPER | Suppression menu |

### Gestion des Ingrédients
| Méthode | Endpoint | Rôle Requis | Description |
|---------|----------|-------------|-------------|
| **POST** | `/menus/{id}/ingredients` | CHEF/ADMIN/DEVELOPER | Ajout ingrédient au menu |
| **DELETE** | `/menus/{menuId}/ingredients/{produitId}` | CHEF/ADMIN/DEVELOPER | Suppression ingrédient |

### Opérations Avancées
| Méthode | Endpoint | Rôle Requis | Description |
|---------|----------|-------------|-------------|
| **PUT** | `/menus/{id}/confirmer` | CHEF/ADMIN/DEVELOPER | Confirmation menu (décrémente stock) |
| **PUT** | `/menus/{id}/annuler` | CHEF/ADMIN/DEVELOPER | Annulation menu (restaure stock) |
| **GET** | `/menus/realisables` | CHEF/ADMIN/DEVELOPER | Menus réalisables (stock suffisant) |
| **GET** | `/menus/{id}/statistiques` | CHEF/ADMIN/DEVELOPER | Statistiques coût/marge |
| **GET** | `/menus/recherche` | CHEF/ADMIN/DEVELOPER | Recherche menus par critères |

**Exemple - Création menu :**
```http
POST /api/menus
Authorization: Bearer <chef_token>
Content-Type: application/json

{
  "nom": "Salade méditerranéenne",
  "description": "Salade fraîche aux tomates cerises, mozzarella et basilic",
  "dateService": "2025-12-06",
  "prixVente": 12.50
}
```
**Réponse (201 Created) :**
```json
{
  "id": 1,
  "nom": "Salade méditerranéenne", 
  "description": "Salade fraîche aux tomates cerises, mozzarella et basilic",
  "dateService": "2025-12-06",
  "dateCreation": "2025-12-04T15:30:00",
  "statut": "BROUILLON",
  "prixVente": 12.50,
  "coutTotalIngredients": 0.00,
  "marge": 12.50,
  "margePercentage": 100.00,
  "peutEtrePrepare": true,
  "ingredients": []
}
```

**Exemple - Ajout ingrédient :**
```http
POST /api/menus/1/ingredients
Authorization: Bearer <chef_token>
Content-Type: application/json

{
  "produitId": 1,
  "quantiteNecessaire": 0.200,
  "uniteUtilisee": "KILOGRAMME",
  "notes": "Tomates bien mûres"
}
```

**Exemple - Confirmation menu :**
```http
PUT /api/menus/1/confirmer
Authorization: Bearer <chef_token>
```
**Réponse :**
```json
{
  "id": 1,
  "nom": "Salade méditerranéenne",
  "statut": "CONFIRME",
  "coutTotalIngredients": 3.45,
  "marge": 9.05,
  "margePercentage": 72.40,
  "peutEtrePrepare": true,
  "ingredients": [
    {
      "id": 1,
      "produitId": 1,
      "produitNom": "Tomates cerises",
      "quantiteNecessaire": 0.200,
      "uniteUtilisee": "KILOGRAMME",
      "coutIngredient": 1.70,
      "stockSuffisant": true,
      "notes": "Tomates bien mûres"
    }
  ]
}
```

---

## 📊 Modèles de Données

### UserResponse
```json
{
  "id": "string (UUID)",
  "email": "string",
  "firstName": "string", 
  "lastName": "string",
  "fullName": "string (calculé)",
  "role": "ROLE_DEVELOPER|ROLE_ADMIN|ROLE_CHEF|ROLE_EMPLOYEE",
  "effectiveRole": "string (rôle effectif)",
  "isActive": "boolean",
  "createdAt": "datetime",
  "lastLoginAt": "datetime",
  "createdBy": "string"
}
```

### ProduitResponse
```json
{
  "id": "number",
  "nom": "string",
  "quantiteStock": "decimal",
  "unite": "KILOGRAMME|GRAMME|LITRE|MILLILITRE|UNITE|PIECE",
  "unitSymbol": "string",
  "prixUnitaire": "decimal",
  "seuilAlerte": "decimal", 
  "datePeremption": "date",
  "dateEntree": "datetime",
  "lastModified": "datetime",
  "isUnderAlertThreshold": "boolean",
  "isExpired": "boolean",
  "deleted": "boolean"
}
```

### MenuResponse
```json
{
  "id": "number",
  "nom": "string",
  "description": "string",
  "dateService": "date",
  "dateCreation": "datetime", 
  "dateModification": "datetime",
  "statut": "BROUILLON|CONFIRME|REALISE|ANNULE",
  "prixVente": "decimal",
  "coutTotalIngredients": "decimal",
  "marge": "decimal",
  "margePercentage": "decimal", 
  "peutEtrePrepare": "boolean",
  "ingredients": "MenuIngredient[]"
}
```

### MenuIngredient
```json
{
  "id": "number",
  "produitId": "number",
  "produitNom": "string",
  "quantiteNecessaire": "decimal",
  "uniteUtilisee": "string",
  "quantiteConvertieStockUnit": "decimal",
  "coutIngredient": "decimal",
  "notes": "string",
  "stockSuffisant": "boolean", 
  "quantiteManquante": "decimal (si insufficient)"
}
```

### Enums

#### UserRole
- `ROLE_DEVELOPER` - Super administrateur
- `ROLE_ADMIN` - Administrateur restaurant
- `ROLE_CHEF` - Chef de cuisine
- `ROLE_EMPLOYEE` - Employé

#### Unite (Unités de mesure)
- `KILOGRAMME` (kg) - Poids
- `GRAMME` (g) - Poids 
- `LITRE` (L) - Volume
- `MILLILITRE` (ml) - Volume
- `UNITE` (unité) - Comptage
- `PIECE` (pièce) - Comptage

#### StatutMenu
- `BROUILLON` - Menu en préparation
- `CONFIRME` - Menu confirmé (stock décrementé)
- `REALISE` - Menu préparé et servi
- `ANNULE` - Menu annulé (stock restauré)

#### TypeMouvement (Stock)
- `ENTREE` - Ajout de stock
- `SORTIE` - Retrait de stock

---

## 📝 Exemples de Requêtes

### Workflow Complet - Gestion d'un Menu

#### 1. Connexion Chef
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "chef@stockchef.com",
  "password": "chefpass123"
}
```

#### 2. Vérification Stock Disponible
```http
GET /api/inventory/produits
Authorization: Bearer <token>
```

#### 3. Création Menu
```http
POST /api/menus
Authorization: Bearer <token>
Content-Type: application/json

{
  "nom": "Pasta Carbonara",
  "description": "Pâtes à la carbonara traditionnelle",
  "dateService": "2025-12-06",
  "prixVente": 14.90
}
```

#### 4. Ajout Ingrédients
```http
POST /api/menus/{menuId}/ingredients
Authorization: Bearer <token>
Content-Type: application/json

{
  "produitId": 2,
  "quantiteNecessaire": 0.100,
  "uniteUtilisee": "KILOGRAMME",
  "notes": "Pâtes fraîches de qualité"
}
```

#### 5. Vérification Faisabilité
```http
GET /api/menus/realisables?date=2025-12-06
Authorization: Bearer <token>
```

#### 6. Confirmation Menu
```http
PUT /api/menus/{menuId}/confirmer
Authorization: Bearer <token>
```

---

## ❌ Gestion des Erreurs

### Codes de Statut HTTP

| Code | Description | Signification |
|------|-------------|---------------|
| **200** | OK | Succès |
| **201** | Created | Ressource créée |
| **204** | No Content | Succès sans contenu |
| **400** | Bad Request | Données invalides |
| **401** | Unauthorized | Non authentifié |
| **403** | Forbidden | Accès refusé |
| **404** | Not Found | Ressource introuvable |
| **409** | Conflict | Conflit de données |
| **500** | Internal Server Error | Erreur serveur |

### Exemples de Réponses d'Erreur

#### Erreur de Validation (400)
```json
{
  "timestamp": "2025-12-04T15:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": {
    "nom": "Le nom du produit est requis",
    "quantiteStock": "La quantité doit être positive"
  }
}
```

#### Erreur d'Authentification (401)
```json
{
  "timestamp": "2025-12-04T15:30:00", 
  "status": 401,
  "error": "Unauthorized",
  "message": "Token JWT invalide ou expiré"
}
```

#### Erreur d'Autorisation (403)
```json
{
  "timestamp": "2025-12-04T15:30:00",
  "status": 403,
  "error": "Forbidden", 
  "message": "Accès refusé - Permissions insuffisantes"
}
```

#### Ressource Non Trouvée (404)
```json
{
  "timestamp": "2025-12-04T15:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Produit avec ID 999 introuvable"
}
```

#### Erreur Métier (409)
```json
{
  "timestamp": "2025-12-04T15:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Stock insuffisant pour confirmer le menu",
  "details": {
    "produit": "Tomates cerises",
    "stockDisponible": 0.5,
    "quantiteRequise": 2.0
  }
}
```

---

## 🚀 Informations Supplémentaires

### Déploiement
- **Local**: `docker-compose up -d --profile postgresql --profile app`
- **Production**: Railway (https://stockchef-back-production.up.railway.app)

### Base de Données
- **PostgreSQL**: Port 5433 (Docker)
- **MySQL**: Port 3307 (Docker) 
- **H2**: Profil de développement

### Monitoring
- Actuator endpoints: `/api/actuator`
- Health check: `/api/actuator/health`
- Logs: DEBUG niveau pour troubleshooting

### Sécurité
- JWT tokens avec expiration 24h
- Passwords hachés avec BCrypt
- CORS activé pour développement
- Validation des données avec Bean Validation

---

*Documentation générée le 2025-12-04 - Version API: 0.0.1-SNAPSHOT*