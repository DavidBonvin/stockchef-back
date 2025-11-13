# 📚 **Documentation StockChef Backend**

## 📄 **Guides Disponibles**

### 🚀 **[QUICK-START.md](QUICK-START.md)**
- Démarrage rapide en 3 étapes
- Configuration express pour le développement
- Commandes de base pour les tests

### 🔧 **[MULTI-DATABASE-SETUP.md](MULTI-DATABASE-SETUP.md)**
- Guide complet d'installation
- Configuration détaillée pour H2, MySQL et PostgreSQL
- Dépannage avancé
- Exemples de configuration personnalisés

## 🏗️ **Architecture du Projet**

```
stockchef-back/
├── start.ps1              # Script de démarrage interactif
├── src/main/java/com/stockchef/stockchefback/
│   ├── controller/        # Contrôleurs REST
│   │   └── AuthController.java
│   ├── service/          # Services métier
│   │   └── JwtService.java
│   ├── repository/       # Accès aux données
│   │   └── UserRepository.java
│   ├── entity/           # Entités JPA
│   │   └── User.java
│   └── config/           # Configuration
│       └── DataInitConfig.java
└── src/main/resources/
    ├── application.properties           # Configuration de base
    ├── application-h2.properties       # Profil H2
    ├── application-mysql.properties    # Profil MySQL
    └── application-postgresql.properties # Profil PostgreSQL
```

## 🌟 **Fonctionnalités Principales**

- ✅ **Authentification JWT** - Système complet de jetons
- ✅ **Multi-base de données** - H2, MySQL, PostgreSQL
- ✅ **Gestion automatique** - Conteneurs Docker automatiques
- ✅ **Profils Spring** - Configurations indépendantes
- ✅ **Initialisation des données** - Utilisateur de développement automatique
- ✅ **Documentation complète** - Guides étape par étape

## 🔐 **Identifiants de Développement**

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