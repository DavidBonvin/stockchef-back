# Test de Validation UUID - Thunder Client

## Configuration Préalable

1. **Base de données PostgreSQL** doit être démarrée :
```bash
docker run --name stockchef-postgres -e POSTGRES_USER=stockchef -e POSTGRES_PASSWORD=stockchef123 -e POSTGRES_DB=stockchef_db -p 5432:5432 -d postgres:15
```

2. **Application démarrée** sur port 8080

## Tests UUID avec Thunder Client

### 1. Test Création Utilisateur avec UUID

**POST** `http://localhost:8080/api/users/create`

Headers:
```
Content-Type: application/json
```

Body (JSON):
```json
{
  "username": "test_user_uuid_001",
  "password": "TestPassword123!",
  "email": "test.uuid@stockchef.com",
  "firstName": "Test",
  "lastName": "UUID"
}
```

**Résultat attendu :**
- Status: 201 Created
- Response doit contenir un `id` au format UUID (ex: "f47ac10b-58cc-4372-a567-0e02b2c3d479")
- Vérifier que l'ID n'est pas un nombre séquentiel

### 2. Test Récupération par UUID

**GET** `http://localhost:8080/api/users/{uuid}`

Remplacer `{uuid}` par l'ID reçu lors de la création.

**Résultat attendu :**
- Status: 200 OK
- Utilisateur trouvé avec l'UUID correct

### 3. Test Admin - Modification Role par UUID

**PUT** `http://localhost:8080/api/admin/users/{uuid}/role?role=MODERATOR`

Headers:
```
Authorization: Bearer {jwt_token_admin}
```

**Résultat attendu :**
- Status: 200 OK
- Role modifié avec succès

### 4. Test Admin - Modification Status par UUID

**PUT** `http://localhost:8080/api/admin/users/{uuid}/status?active=false`

Headers:
```
Authorization: Bearer {jwt_token_admin}
```

**Résultat attendu :**
- Status: 200 OK
- Status modifié avec succès

### 5. Test d'erreur - UUID invalide

**GET** `http://localhost:8080/api/users/invalid-uuid-format`

**Résultat attendu :**
- Status: 400 Bad Request
- Message d'erreur approprié

### 6. Validation Format UUID

Les UUIDs générés doivent respecter le format :
- 8-4-4-4-12 caractères hexadécimaux
- Séparés par des tirets
- Exemple: `f47ac10b-58cc-4372-a567-0e02b2c3d479`

### Points de Vérification

1. **Unicité** : Chaque utilisateur créé reçoit un UUID unique
2. **Non-Prédictibilité** : Les UUIDs ne suivent pas une séquence prévisible
3. **Compatibilité** : Toutes les opérations CRUD fonctionnent avec les UUIDs
4. **Validation** : Les UUIDs invalides sont rejetés avec erreurs appropriées

### Tests de Performance UUID

**Création multiple d'utilisateurs** pour vérifier l'unicité :

```json
[
  {"username": "user_001", "password": "Pass123!", "email": "user001@test.com", "firstName": "User", "lastName": "001"},
  {"username": "user_002", "password": "Pass123!", "email": "user002@test.com", "firstName": "User", "lastName": "002"},
  {"username": "user_003", "password": "Pass123!", "email": "user003@test.com", "firstName": "User", "lastName": "003"}
]
```

Vérifier que tous reçoivent des UUIDs différents.

## Comparaison Sécurité

### Avant (Long ID séquentiel)
- IDs prévisibles : 1, 2, 3, 4...
- Énumération possible des utilisateurs
- Facilite les attaques par force brute

### Après (UUID)
- IDs cryptographiquement sécurisés
- 2^128 possibilités (340 undécillions)
- Énumération quasi-impossible
- Améliore significativement la sécurité API