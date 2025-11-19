# Script para probar generacion UUID en usuarios iniciales
Write-Host "🔧 Probando generacion UUID en usuarios iniciales..." -ForegroundColor Cyan

# Eliminar contenedor anterior si existe
Write-Host "Eliminando contenedor PostgreSQL anterior..." -ForegroundColor Yellow
docker stop stockchef-postgres 2>$null | Out-Null
docker rm stockchef-postgres 2>$null | Out-Null

# Crear nuevo contenedor PostgreSQL
Write-Host "Creando nuevo contenedor PostgreSQL..." -ForegroundColor Green
docker run --name stockchef-postgres `
    -e POSTGRES_USER=stockchef `
    -e POSTGRES_PASSWORD=stockchef123 `
    -e POSTGRES_DB=stockchef_db `
    -p 5432:5432 `
    -d postgres:15

# Esperar a que PostgreSQL este listo
Write-Host "Esperando PostgreSQL..." -ForegroundColor Yellow
Start-Sleep 10

# Verificar conexion
$connection = Test-NetConnection -ComputerName localhost -Port 5432 -WarningAction SilentlyContinue
if ($connection.TcpTestSucceeded) {
    Write-Host "✅ PostgreSQL listo en puerto 5432" -ForegroundColor Green
} else {
    Write-Host "❌ PostgreSQL no accesible" -ForegroundColor Red
    exit 1
}

Write-Host "`n🎯 Ahora puedes:" -ForegroundColor Cyan
Write-Host "1. Iniciar la aplicacion Spring Boot" -ForegroundColor White
Write-Host "2. Hacer GET /api/admin/users para verificar que los IDs son UUID" -ForegroundColor White
Write-Host "3. Los usuarios deben tener IDs como: 550e8400-e29b-41d4-a716-446655440001" -ForegroundColor White

Write-Host "`n💡 Ejemplo verificacion:" -ForegroundColor Yellow
Write-Host "curl http://localhost:8080/api/admin/users" -ForegroundColor Gray