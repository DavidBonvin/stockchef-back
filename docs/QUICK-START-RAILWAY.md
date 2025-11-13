# ⚡ Guide de Démarrage Rapide - Railway Production

## 🎯 Accès Rapide

**Production URL** : `https://stockchef-back-production.up.railway.app`

## 🔐 Test d'Authentification Immédiat

### Thunder Client / Postman

```http
POST https://stockchef-back-production.up.railway.app/api/auth/login
Content-Type: application/json

{
  "email": "developer@stockchef.com",
  "password": "devpass123"
}
```

### Réponse Attendue ✅
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "developer@stockchef.com",
  "fullName": "Developer Admin",
  "role": "ROLE_DEVELOPER",
  "expiresIn": 86400000
}
```

## 👤 Comptes de Test Disponibles

| Rôle | Email | Mot de passe |
|------|-------|--------------|
| Développeur | `developer@stockchef.com` | `devpass123` |
| Admin | `admin@stockchef.com` | `adminpass123` |
| Chef | `chef@stockchef.com` | `chefpass123` |
| Employé | `employee@stockchef.com` | `emppass123` |

## 🩺 Health Checks

```bash
# Status application
GET https://stockchef-back-production.up.railway.app/

# Détails santé
GET https://stockchef-back-production.up.railway.app/api/health
```

## ⚠️ **IMPORTANT - Branches**

> **`main` = PRODUCTION AUTOMATIQUE** 
> 
> Tout commit sur `main` → Déploiement immédiat !

```bash
# ✅ CORRECT - Développer sur branche
git checkout -b feature/mon-changement

# ❌ DANGER - Éviter développement direct sur main
git checkout main  # Seulement pour merger du code validé !
```

## 📚 Documentation Complète

👉 **[PRODUCTION-RAILWAY.md](PRODUCTION-RAILWAY.md)** - Guide détaillé complet

---

*Créé avec ❤️ pour un déploiement Railway sans stress* 🚀