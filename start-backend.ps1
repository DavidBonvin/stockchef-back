# StockChef Backend Launcher Script
# Permite elegir entre H2, MySQL o PostgreSQL

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("h2", "mysql", "postgresql", "interactive")]
    [string]$Database = "interactive"
)

# Colors for console output
$Host.UI.RawUI.ForegroundColor = "Green"

Write-Host @"
╔══════════════════════════════════════════════════════════════╗
║                    STOCKCHEF BACKEND LAUNCHER               ║
║                     Multi-Database Support                  ║
╚══════════════════════════════════════════════════════════════╝
"@

$Host.UI.RawUI.ForegroundColor = "White"

function Show-DatabaseMenu {
    Write-Host ""
    Write-Host "Selecciona la base de datos para el backend:" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "1. H2 Database (En memoria - Desarrollo rápido)" -ForegroundColor Yellow
    Write-Host "   - ✓ Sin configuración adicional" -ForegroundColor DarkGray
    Write-Host "   - ✓ Datos se pierden al reiniciar" -ForegroundColor DarkGray
    Write-Host "   - ✓ Consola H2 disponible en /h2-console" -ForegroundColor DarkGray
    Write-Host ""
    Write-Host "2. MySQL (Docker - Persistente)" -ForegroundColor Yellow  
    Write-Host "   - ✓ Datos persistentes" -ForegroundColor DarkGray
    Write-Host "   - ✓ Contenedor Docker en puerto 3307" -ForegroundColor DarkGray
    Write-Host "   - ⚠  Requiere Docker corriendo" -ForegroundColor DarkGray
    Write-Host ""
    Write-Host "3. PostgreSQL (Docker - Persistente)" -ForegroundColor Yellow
    Write-Host "   - ✓ Datos persistentes" -ForegroundColor DarkGray  
    Write-Host "   - ✓ Contenedor Docker en puerto 5432" -ForegroundColor DarkGray
    Write-Host "   - ⚠  Requiere Docker corriendo" -ForegroundColor DarkGray
    Write-Host ""
    Write-Host "4. Mostrar documentación" -ForegroundColor Magenta
    Write-Host "5. Salir" -ForegroundColor Red
    Write-Host ""
}

function Test-DockerContainer {
    param($ContainerName)
    
    try {
        $result = docker ps --filter "name=$ContainerName" --format "table {{.Names}}\t{{.Status}}" 2>$null
        return $result -like "*$ContainerName*Up*"
    }
    catch {
        return $false
    }
}

function Start-Backend {
    param($DatabaseProfile)
    
    Write-Host ""
    Write-Host "🚀 Iniciando StockChef Backend con perfil: $DatabaseProfile" -ForegroundColor Green
    Write-Host ""
    
    # Verificar dependencias según el perfil
    switch ($DatabaseProfile) {
        "mysql" {
            Write-Host "🔍 Verificando contenedor MySQL..." -ForegroundColor Yellow
            if (!(Test-DockerContainer "stockchef-mysql")) {
                Write-Host "❌ Contenedor MySQL no encontrado o no está corriendo" -ForegroundColor Red
                Write-Host "💡 Ejecuta: docker run -d --name stockchef-mysql -p 3307:3306 -e MYSQL_ROOT_PASSWORD=UserAdmin -e MYSQL_DATABASE=stockchef_db mysql:8.4" -ForegroundColor Cyan
                return
            }
            Write-Host "✅ MySQL Docker container está corriendo" -ForegroundColor Green
        }
        "postgresql" {
            Write-Host "🔍 Verificando contenedor PostgreSQL..." -ForegroundColor Yellow
            if (!(Test-DockerContainer "stockchef-postgres")) {
                Write-Host "❌ Contenedor PostgreSQL no encontrado o no está corriendo" -ForegroundColor Red
                Write-Host "💡 Ejecuta: docker run -d --name stockchef-postgres -p 5432:5432 -e POSTGRES_PASSWORD=stockchef123 -e POSTGRES_DB=stockchef_db -e POSTGRES_USER=stockchef_user postgres:15" -ForegroundColor Cyan
                return
            }
            Write-Host "✅ PostgreSQL Docker container está corriendo" -ForegroundColor Green
        }
        "h2" {
            Write-Host "✅ H2 Database - Sin dependencias externas" -ForegroundColor Green
        }
    }
    
    Write-Host ""
    Write-Host "📚 Cargando configuración: application-$DatabaseProfile.properties" -ForegroundColor Cyan
    Write-Host ""
    
    # Iniciar el backend con el perfil seleccionado
    $env:SPRING_PROFILES_ACTIVE = $DatabaseProfile
    
    try {
        & mvn spring-boot:run -Dspring-boot.run.profiles=$DatabaseProfile
    }
    catch {
        Write-Host "❌ Error al iniciar el backend" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
    }
}

function Show-Documentation {
    Write-Host ""
    Write-Host "📖 DOCUMENTACIÓN DE CONFIGURACIÓN" -ForegroundColor Cyan
    Write-Host "=================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "🗂️  Archivos de configuración:" -ForegroundColor Yellow
    Write-Host "  - application.properties (configuración base)"
    Write-Host "  - application-h2.properties (H2 Database)"
    Write-Host "  - application-mysql.properties (MySQL Docker)"
    Write-Host "  - application-postgresql.properties (PostgreSQL Docker)"
    Write-Host ""
    Write-Host "🔧 Comandos Docker útiles:" -ForegroundColor Yellow
    Write-Host "  MySQL:"
    Write-Host "    docker run -d --name stockchef-mysql -p 3307:3306 -e MYSQL_ROOT_PASSWORD=UserAdmin -e MYSQL_DATABASE=stockchef_db mysql:8.4"
    Write-Host "  PostgreSQL:"
    Write-Host "    docker run -d --name stockchef-postgres -p 5432:5432 -e POSTGRES_PASSWORD=stockchef123 -e POSTGRES_DB=stockchef_db -e POSTGRES_USER=stockchef_user postgres:15"
    Write-Host ""
    Write-Host "🌐 URLs de acceso:" -ForegroundColor Yellow
    Write-Host "  - Backend API: http://localhost:8090/api"
    Write-Host "  - H2 Console: http://localhost:8090/api/h2-console (solo con perfil H2)"
    Write-Host "  - Auth Login: POST http://localhost:8090/api/auth/login"
    Write-Host ""
    Write-Host "🔐 Credenciales por defecto:" -ForegroundColor Yellow
    Write-Host "  Email: developer@stockchef.com"
    Write-Host "  Password: devpass123"
    Write-Host ""
}

# Main script logic
if ($Database -eq "interactive") {
    do {
        Show-DatabaseMenu
        $choice = Read-Host "Elige una opción (1-5)"
        
        switch ($choice) {
            "1" { Start-Backend "h2"; break }
            "2" { Start-Backend "mysql"; break }
            "3" { Start-Backend "postgresql"; break }
            "4" { Show-Documentation }
            "5" { 
                Write-Host "👋 ¡Hasta luego!" -ForegroundColor Green
                exit 0 
            }
            default { 
                Write-Host "❌ Opción inválida. Elige un número del 1 al 5." -ForegroundColor Red
                Start-Sleep -Seconds 2
            }
        }
    } while ($choice -ne "5")
} else {
    # Direct launch with specified database
    Start-Backend $Database
}