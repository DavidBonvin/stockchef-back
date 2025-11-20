# Script PowerShell pour gérer l'environnement Docker de StockChef
# Usage: .\docker-manager.ps1 [commande] [options]

param(
    [string]$Command = "help",
    [string]$Option = "mysql"
)

# Fonction pour afficher l'aide
function Show-Help {
    Write-Host "StockChef Docker Manager" -ForegroundColor Blue
    Write-Host ""
    Write-Host "Usage: .\docker-manager.ps1 [commande] [options]"
    Write-Host ""
    Write-Host "Commandes disponibles:"
    Write-Host "  up [mysql|postgres]    - Démarrer le stack avec la base de données spécifiée" -ForegroundColor Green
    Write-Host "  down                   - Arrêter et supprimer tous les conteneurs" -ForegroundColor Green
    Write-Host "  restart [mysql|postgres] - Redémarrer le stack" -ForegroundColor Green
    Write-Host "  logs [service]        - Afficher logs d'un service spécifique" -ForegroundColor Green
    Write-Host "  status                 - Afficher état des services" -ForegroundColor Green
    Write-Host "  clean                  - Nettoyer volumes et conteneurs" -ForegroundColor Green
    Write-Host "  tools                  - Démarrer outils d'administration" -ForegroundColor Green
    Write-Host "  build                  - Reconstruire l'image de l'application" -ForegroundColor Green
    Write-Host "  help                   - Afficher cette aide" -ForegroundColor Green
    Write-Host ""
    Write-Host "Exemples:"
    Write-Host "  .\docker-manager.ps1 up mysql           # Démarrer avec MySQL" -ForegroundColor Yellow
    Write-Host "  .\docker-manager.ps1 up postgres        # Démarrer avec PostgreSQL" -ForegroundColor Yellow
    Write-Host "  .\docker-manager.ps1 logs backend       # Voir logs du backend" -ForegroundColor Yellow
    Write-Host "  .\docker-manager.ps1 tools              # Démarrer phpMyAdmin et pgAdmin" -ForegroundColor Yellow
}

# Fonction pour configurer l'environnement
function Setup-Environment {
    param([string]$DbType = "mysql")
    
    if (!(Test-Path ".env")) {
        Write-Host "Création fichier .env depuis .env.example" -ForegroundColor Yellow
        Copy-Item ".env.example" ".env"
    }
    
    # Actualizar tipo de base de datos en .env
    (Get-Content ".env") -replace "DATABASE_TYPE=.*", "DATABASE_TYPE=$DbType" | Set-Content ".env"
    
    Write-Host "Configuré pour utiliser: $DbType" -ForegroundColor Green
}

# Fonction pour démarrer les services
function Start-Services {
    param([string]$DbType = "mysql")
    
    Setup-Environment $DbType
    
    Write-Host "Démarrage StockChef avec $DbType..." -ForegroundColor Blue
    
    switch ($DbType) {
        "mysql" {
            Write-Host "Démarrage MySQL et backend..." -ForegroundColor Yellow
            docker-compose up -d mysql
            Start-Sleep -Seconds 20  # Attendre que MySQL soit prêt
            docker-compose --profile app up -d stockchef-backend
        }
        "postgres" {
            Write-Host "Démarrage PostgreSQL et backend..." -ForegroundColor Yellow
            docker-compose up -d postgres
            Start-Sleep -Seconds 15  # Attendre que PostgreSQL soit prêt
            docker-compose --profile app up -d stockchef-backend
        }
        default {
            Write-Host "Type de base de données non valide: $DbType" -ForegroundColor Red
            Write-Host "Utilisez: mysql ou postgres"
            exit 1
        }
    }
    
    Write-Host "Services démarrés avec succès" -ForegroundColor Green
    Write-Host "Backend disponible sur: http://localhost:8090/api/health" -ForegroundColor Yellow
    
    if ($DbType -eq "mysql") {
        Write-Host "MySQL disponible sur: localhost:3307" -ForegroundColor Yellow
    } else {
        Write-Host "PostgreSQL disponible sur: localhost:5433" -ForegroundColor Yellow
    }
}

# Fonction pour arrêter les services
function Stop-Services {
    Write-Host "Arrêt de tous les services..." -ForegroundColor Yellow
    docker-compose down
    Write-Host "Services arrêtés" -ForegroundColor Green
}

# Fonction pour redémarrer les services
function Restart-Services {
    param([string]$DbType = "mysql")
    
    Write-Host "Redémarrage des services..." -ForegroundColor Yellow
    Stop-Services
    Start-Services $DbType
}

# Función para mostrar logs
function Show-Logs {
    param([string]$Service = "stockchef-backend")
    
    switch ($Service) {
        { $_ -in @("backend", "app", "stockchef-backend") } {
            docker-compose logs -f stockchef-backend
        }
        { $_ -in @("mysql", "db-mysql") } {
            docker-compose logs -f mysql
        }
        { $_ -in @("postgres", "db-postgres") } {
            docker-compose logs -f postgres
        }
        "all" {
            docker-compose logs -f
        }
        default {
            docker-compose logs -f $Service
        }
    }
}

# Función para mostrar estado
function Show-Status {
    Write-Host "Estado de los servicios:" -ForegroundColor Blue
    docker-compose ps
    Write-Host ""
    Write-Host "Uso de recursos:" -ForegroundColor Blue
    try {
        docker stats --no-stream --format "table {{.Container}}`t{{.CPUPerc}}`t{{.MemUsage}}"
    }
    catch {
        Write-Host "No hay contenedores ejecutándose"
    }
}

# Función para limpiar
function Clean-All {
    $response = Read-Host "¿Estás seguro de que quieres limpiar todos los datos? (y/N)"
    if ($response -match "^[Yy]$") {
        Write-Host "Limpiando contenedores y volúmenes..." -ForegroundColor Yellow
        docker-compose down -v --remove-orphans
        docker system prune -f
        Write-Host "Limpieza completada" -ForegroundColor Green
    }
    else {
        Write-Host "Operación cancelada" -ForegroundColor Blue
    }
}

# Función para iniciar herramientas de administración
function Start-Tools {
    Write-Host "Iniciando herramientas de administración..." -ForegroundColor Blue
    docker-compose --profile tools up -d phpmyadmin pgadmin
    Write-Host "Herramientas iniciadas:" -ForegroundColor Green
    Write-Host "phpMyAdmin: http://localhost:8080" -ForegroundColor Yellow
    Write-Host "pgAdmin: http://localhost:8081" -ForegroundColor Yellow
}

# Función para reconstruir la aplicación
function Rebuild-App {
    Write-Host "Reconstruyendo la imagen de la aplicación..." -ForegroundColor Blue
    docker-compose build --no-cache stockchef-backend
    Write-Host "Imagen reconstruida exitosamente" -ForegroundColor Green
}

# Main
switch ($Command) {
    { $_ -in @("up", "start") } {
        Start-Services $Option
    }
    { $_ -in @("down", "stop") } {
        Stop-Services
    }
    "restart" {
        Restart-Services $Option
    }
    "logs" {
        Show-Logs $Option
    }
    "status" {
        Show-Status
    }
    "clean" {
        Clean-All
    }
    "tools" {
        Start-Tools
    }
    "build" {
        Rebuild-App
    }
    { $_ -in @("help", "--help", "-h") } {
        Show-Help
    }
    default {
        Write-Host "Comando no reconocido: $Command" -ForegroundColor Red
        Write-Host ""
        Show-Help
        exit 1
    }
}