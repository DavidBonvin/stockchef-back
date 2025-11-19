Write-Host "Probando UUID endpoint..." -ForegroundColor Green

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8090/api/admin/users" -Method GET -ContentType "application/json"
    $users = $response.Content | ConvertFrom-Json
    
    Write-Host "`n✅ ¡Respuesta recibida!" -ForegroundColor Green
    Write-Host "Total de usuarios: " -NoNewline
    Write-Host $users.Count -ForegroundColor Yellow
    
    Write-Host "`n📋 Lista de usuarios con sus IDs:" -ForegroundColor Cyan
    Write-Host "================================" -ForegroundColor Cyan
    
    foreach ($user in $users) {
        Write-Host "ID: " -NoNewline -ForegroundColor White
        Write-Host $user.id -NoNewline -ForegroundColor Yellow
        Write-Host " | Email: " -NoNewline -ForegroundColor White
        Write-Host $user.email -NoNewline -ForegroundColor Green
        Write-Host " | Role: " -NoNewline -ForegroundColor White
        Write-Host $user.role -ForegroundColor Magenta
    }
    
    # Verificar si son UUIDs
    Write-Host "`n🔍 Análisis de IDs:" -ForegroundColor Cyan
    Write-Host "==================" -ForegroundColor Cyan
    
    $allUuids = $true
    foreach ($user in $users) {
        $isUuid = $user.id -match "^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$"
        if ($isUuid) {
            Write-Host "✅ $($user.email) - UUID válido: $($user.id)" -ForegroundColor Green
        } else {
            Write-Host "❌ $($user.email) - NO es UUID: $($user.id)" -ForegroundColor Red
            $allUuids = $false
        }
    }
    
    if ($allUuids) {
        Write-Host "`n🎉 ¡ÉXITO! Todos los IDs son UUIDs válidos" -ForegroundColor Green -BackgroundColor Black
    } else {
        Write-Host "`n⚠️  Algunos IDs NO son UUIDs" -ForegroundColor Red -BackgroundColor Black
    }
    
} catch {
    Write-Host "❌ Error al conectar con el servidor:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    Write-Host "`nAsegúrate de que la aplicación esté ejecutándose en http://localhost:8090" -ForegroundColor Yellow
}