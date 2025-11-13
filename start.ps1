# Script de Lancement StockChef Backend - Version Simple
param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("h2", "mysql", "postgresql")]
    [string]$Database
)

Write-Host "===============================================" -ForegroundColor Green
Write-Host "        STOCKCHEF BACKEND LAUNCHER" -ForegroundColor Green  
Write-Host "         Multi-Database Support" -ForegroundColor Green
Write-Host "===============================================" -ForegroundColor Green

function Test-DockerContainer {
    param($ContainerName)
    try {
        $result = docker ps --filter "name=$ContainerName" --format "table {{.Names}}" 2>$null
        return $result -like "*$ContainerName*"
    }
    catch {
        return $false
    }
}

function Start-Backend {
    param($DatabaseType)
    
    Write-Host ""
    Write-Host "Démarrage de StockChef Backend avec le profil: $DatabaseType" -ForegroundColor Green
    Write-Host ""
    
    switch ($DatabaseType) {
        "mysql" {
            Write-Host "Vérification du conteneur MySQL..." -ForegroundColor Yellow
            if (!(Test-DockerContainer "stockchef-mysql")) {
                Write-Host "Création du conteneur MySQL..." -ForegroundColor Yellow
                docker run -d --name stockchef-mysql -p 3307:3306 -e MYSQL_ROOT_PASSWORD=UserAdmin -e MYSQL_DATABASE=stockchef_db mysql:8.4
                Start-Sleep -Seconds 10
            }
            Write-Host "Conteneur Docker MySQL prêt" -ForegroundColor Green
        }
        "postgresql" {
            Write-Host "Vérification du conteneur PostgreSQL..." -ForegroundColor Yellow
            if (!(Test-DockerContainer "stockchef-postgres")) {
                Write-Host "Création du conteneur PostgreSQL..." -ForegroundColor Yellow
                docker run -d --name stockchef-postgres -p 5432:5432 -e POSTGRES_PASSWORD=UserAdmin postgres:15
                Start-Sleep -Seconds 15
                Write-Host "Création de la base de données stockchef_db..." -ForegroundColor Yellow
                docker exec stockchef-postgres psql -U postgres -c "CREATE DATABASE stockchef_db;"
            }
            Write-Host "Conteneur Docker PostgreSQL prêt" -ForegroundColor Green
        }
        "h2" {
            Write-Host "H2 Database - Sans dépendances externes" -ForegroundColor Green
        }
    }
    
    Write-Host "Chargement de la configuration: application-$DatabaseType.properties" -ForegroundColor Cyan
    Write-Host ""
    
    $env:JAVA_HOME = "C:\Program Files\Java\jdk-24"
    $env:SPRING_PROFILES_ACTIVE = $DatabaseType
    
    Write-Host "Démarrage du backend..." -ForegroundColor Yellow
    mvn spring-boot:run
}

if (-not $Database) {
    Write-Host ""
    Write-Host "Sélectionnez la base de données:" -ForegroundColor Cyan
    Write-Host "1. H2 Database (En mémoire - Développement)" -ForegroundColor Yellow
    Write-Host "2. MySQL (Docker - Persistant)" -ForegroundColor Yellow  
    Write-Host "3. PostgreSQL (Docker - Persistant)" -ForegroundColor Yellow
    Write-Host ""
    
    $choice = Read-Host "Choisissez une option (1-3)"
    
    switch ($choice) {
        "1" { Start-Backend "h2" }
        "2" { Start-Backend "mysql" }
        "3" { Start-Backend "postgresql" }
        default { 
            Write-Host "Option invalide" -ForegroundColor Red
            exit 1
        }
    }
} else {
    Start-Backend $Database
}