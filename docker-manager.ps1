# Script de PowerShell para gestionar el entorno Docker de StockChef
# Uso: .\docker-manager.ps1 [comando] [opciones]

param(
    [string]$Command = "help",
    [string]$Option = "mysql"
)

# Función para mostrar ayuda
function Show-Help {
    Write-Host "StockChef Docker Manager" -ForegroundColor Blue
    Write-Host ""
    Write-Host "Uso: .\docker-manager.ps1 [comando] [opciones]"
    Write-Host ""
    Write-Host "Comandos disponibles:"
    Write-Host "  up [mysql|postgres]    - Iniciar el stack con la base de datos especificada" -ForegroundColor Green
    Write-Host "  down                   - Parar y remover todos los contenedores" -ForegroundColor Green
    Write-Host "  restart [mysql|postgres] - Reiniciar el stack" -ForegroundColor Green
    Write-Host "  logs [servicio]        - Mostrar logs de un servicio específico" -ForegroundColor Green
    Write-Host "  status                 - Mostrar estado de los servicios" -ForegroundColor Green
    Write-Host "  clean                  - Limpiar volúmenes y contenedores" -ForegroundColor Green
    Write-Host "  tools                  - Iniciar herramientas de administración" -ForegroundColor Green
    Write-Host "  build                  - Reconstruir la imagen de la aplicación" -ForegroundColor Green
    Write-Host "  help                   - Mostrar esta ayuda" -ForegroundColor Green
    Write-Host ""
    Write-Host "Ejemplos:"
    Write-Host "  .\docker-manager.ps1 up mysql           # Iniciar con MySQL" -ForegroundColor Yellow
    Write-Host "  .\docker-manager.ps1 up postgres        # Iniciar con PostgreSQL" -ForegroundColor Yellow
    Write-Host "  .\docker-manager.ps1 logs backend       # Ver logs del backend" -ForegroundColor Yellow
    Write-Host "  .\docker-manager.ps1 tools              # Iniciar phpMyAdmin y pgAdmin" -ForegroundColor Yellow
}

# Función para configurar el entorno
function Setup-Environment {
    param([string]$DbType = "mysql")
    
    if (!(Test-Path ".env")) {
        Write-Host "Creando archivo .env desde .env.example" -ForegroundColor Yellow
        Copy-Item ".env.example" ".env"
    }
    
    # Actualizar tipo de base de datos en .env
    (Get-Content ".env") -replace "DATABASE_TYPE=.*", "DATABASE_TYPE=$DbType" | Set-Content ".env"
    
    Write-Host "Configurado para usar: $DbType" -ForegroundColor Green
}

# Función para iniciar servicios
function Start-Services {
    param([string]$DbType = "mysql")
    
    Setup-Environment $DbType
    
    Write-Host "Iniciando StockChef con $DbType..." -ForegroundColor Blue
    
    switch ($DbType) {
        "mysql" {
            Write-Host "Iniciando MySQL y backend..." -ForegroundColor Yellow
            docker-compose up -d mysql
            Start-Sleep -Seconds 20  # Esperar a que MySQL esté listo
            docker-compose --profile app up -d stockchef-backend
        }
        "postgres" {
            Write-Host "Iniciando PostgreSQL y backend..." -ForegroundColor Yellow
            docker-compose up -d postgres
            Start-Sleep -Seconds 15  # Esperar a que PostgreSQL esté listo
            docker-compose --profile app up -d stockchef-backend
        }
        default {
            Write-Host "Tipo de base de datos no válido: $DbType" -ForegroundColor Red
            Write-Host "Usa: mysql o postgres"
            exit 1
        }
    }
    
    Write-Host "Servicios iniciados exitosamente" -ForegroundColor Green
    Write-Host "Backend disponible en: http://localhost:8090/api/health" -ForegroundColor Yellow
    
    if ($DbType -eq "mysql") {
        Write-Host "MySQL disponible en: localhost:3307" -ForegroundColor Yellow
    } else {
        Write-Host "PostgreSQL disponible en: localhost:5433" -ForegroundColor Yellow
    }
}

# Función para parar servicios
function Stop-Services {
    Write-Host "Parando todos los servicios..." -ForegroundColor Yellow
    docker-compose down
    Write-Host "Servicios parados" -ForegroundColor Green
}

# Función para reiniciar servicios
function Restart-Services {
    param([string]$DbType = "mysql")
    
    Write-Host "Reiniciando servicios..." -ForegroundColor Yellow
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