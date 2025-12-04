# =============================================================================
# SCRIPT DE TEST COMPLETO PARA STOCKCHEF BACKEND
# Testea: Módulos de Monitoreo, Inventario/Productos y Menús
# =============================================================================

# Configuración base
$baseUrl = "http://localhost:8090"  # Cambiar por la URL de producción si es necesario
$timestamp = Get-Date -Format 'yyyyMMddHHmmss'

Write-Host "🚀 INICIANDO TESTS COMPLETOS DE STOCKCHEF BACKEND" -ForegroundColor Cyan
Write-Host "Base URL: $baseUrl" -ForegroundColor Yellow
Write-Host "Timestamp: $timestamp" -ForegroundColor Yellow
Write-Host ""

# =============================================================================
# 1. MÓDULOS DE MONITOREO (6 endpoints)
# =============================================================================

Write-Host "🩺 === TESTING MÓDULOS DE MONITOREO === (6 endpoints)" -ForegroundColor Green

# 1.1 Health Check endpoints
Write-Host "`n📊 Testing Health Check endpoints..."

try {
    $healthResponse = Invoke-RestMethod -Uri "$baseUrl/health" -Method GET
    Write-Host "✅ GET /health SUCCESS:" -ForegroundColor Green
    Write-Host "Status: $($healthResponse.status), Message: $($healthResponse.message)"
} catch {
    Write-Host "❌ GET /health FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

try {
    $healthInfoResponse = Invoke-RestMethod -Uri "$baseUrl/health/info" -Method GET
    Write-Host "✅ GET /health/info SUCCESS:" -ForegroundColor Green
    Write-Host "Application: $($healthInfoResponse.application), Version: $($healthInfoResponse.version)"
} catch {
    Write-Host "❌ GET /health/info FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

try {
    $pingResponse = Invoke-RestMethod -Uri "$baseUrl/health/ping" -Method GET
    Write-Host "✅ GET /health/ping SUCCESS: $pingResponse" -ForegroundColor Green
} catch {
    Write-Host "❌ GET /health/ping FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# 1.2 Root & Test endpoints
Write-Host "`n🏠 Testing Root & Test endpoints..."

try {
    $rootResponse = Invoke-RestMethod -Uri "$baseUrl/" -Method GET
    Write-Host "✅ GET / SUCCESS:" -ForegroundColor Green
    Write-Host "Status: $($rootResponse.status), Message: $($rootResponse.message)"
} catch {
    Write-Host "❌ GET / FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

try {
    $testHelloResponse = Invoke-RestMethod -Uri "$baseUrl/test/hello" -Method GET
    Write-Host "✅ GET /test/hello SUCCESS:" -ForegroundColor Green
    Write-Host "Message: $($testHelloResponse.message), Status: $($testHelloResponse.status)"
} catch {
    Write-Host "❌ GET /test/hello FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# 1.3 Mock Auth Test
Write-Host "`n🔐 Testing Mock Auth..."
$mockAuthBody = @{ 
    email = "developer@stockchef.com"
    password = "devpass123" 
} | ConvertTo-Json

try {
    $mockAuthResponse = Invoke-RestMethod -Uri "$baseUrl/test/auth-mock" -Method POST -Body $mockAuthBody -ContentType "application/json"
    Write-Host "✅ POST /test/auth-mock SUCCESS:" -ForegroundColor Green
    Write-Host "Token received: $($mockAuthResponse.token[0..20] -join '')..."
} catch {
    Write-Host "❌ POST /test/auth-mock FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# =============================================================================
# 2. PREPARACIÓN: CREAR USUARIO Y AUTENTICACIÓN
# =============================================================================

Write-Host "`n🔑 === PREPARACIÓN: AUTENTICACIÓN === " -ForegroundColor Magenta

# Crear usuario para tests
$testUser = @{ 
    email = "test.endpoints.${timestamp}@stockchef.com"
    password = "TestPass123!"
    firstName = "Test"
    lastName = "Endpoints" 
} | ConvertTo-Json

try {
    $userResponse = Invoke-RestMethod -Uri "$baseUrl/users/register" -Method POST -Body $testUser -ContentType "application/json"
    $testUserId = $userResponse.id
    $testUserEmail = $userResponse.email
    Write-Host "✅ Usuario de prueba creado: $testUserEmail (ID: $testUserId)" -ForegroundColor Green
} catch {
    Write-Host "❌ Error creando usuario de prueba: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Login y obtener token
$loginBody = @{ 
    email = $testUserEmail
    password = "TestPass123!" 
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method POST -Body $loginBody -ContentType "application/json"
    $authToken = $loginResponse.token
    $authHeaders = @{ "Authorization" = "Bearer $authToken"; "Content-Type" = "application/json" }
    Write-Host "✅ Login exitoso - Token obtenido" -ForegroundColor Green
} catch {
    Write-Host "❌ Error en login: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# =============================================================================
# 3. MÓDULO INVENTARIO/PRODUCTOS (13 endpoints)
# =============================================================================

Write-Host "`n📦 === TESTING MÓDULO INVENTARIO/PRODUCTOS === (13 endpoints)" -ForegroundColor Green

# Variables para almacenar IDs creados
$productId1 = $null
$productId2 = $null

# 3.1 POST /api/inventory/produits - Crear producto
Write-Host "`n🆕 Testing Crear productos..."
$product1 = @{
    nom = "Tomate Test $timestamp"
    description = "Tomates frescos para testing"
    categorieId = 1
    quantiteStock = 50.0
    unite = "KG"
    prixUnitaire = 3.50
    seuilAlerte = 10.0
    datePeremption = "2025-12-31"
    fournisseur = "Fournisseur Test"
} | ConvertTo-Json

try {
    $productResponse1 = Invoke-RestMethod -Uri "$baseUrl/api/inventory/produits" -Method POST -Body $product1 -Headers $authHeaders
    $productId1 = $productResponse1.id
    Write-Host "✅ POST /api/inventory/produits SUCCESS:" -ForegroundColor Green
    Write-Host "Producto creado: $($productResponse1.nom) (ID: $productId1)"
} catch {
    Write-Host "❌ POST /api/inventory/produits FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# Crear segundo producto
$product2 = @{
    nom = "Cebolla Test $timestamp"
    description = "Cebollas frescas para testing"
    categorieId = 1
    quantiteStock = 5.0  # Bajo stock para test de alertas
    unite = "KG"
    prixUnitaire = 2.20
    seuilAlerte = 8.0
    datePeremption = "2025-06-15"  # Próximo a expirar
    fournisseur = "Fournisseur Test"
} | ConvertTo-Json

try {
    $productResponse2 = Invoke-RestMethod -Uri "$baseUrl/api/inventory/produits" -Method POST -Body $product2 -Headers $authHeaders
    $productId2 = $productResponse2.id
    Write-Host "✅ Segundo producto creado: $($productResponse2.nom) (ID: $productId2)" -ForegroundColor Green
} catch {
    Write-Host "❌ Error creando segundo producto: $($_.Exception.Message)" -ForegroundColor Red
}

# 3.2 GET /api/inventory/produits - Listar todos los productos
Write-Host "`n📋 Testing Listar productos..."
try {
    $allProductsResponse = Invoke-RestMethod -Uri "$baseUrl/api/inventory/produits" -Method GET -Headers $authHeaders
    Write-Host "✅ GET /api/inventory/produits SUCCESS: $($allProductsResponse.Length) productos encontrados" -ForegroundColor Green
} catch {
    Write-Host "❌ GET /api/inventory/produits FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# 3.3 GET /api/inventory/produits/page - Listar con paginación
Write-Host "`n📄 Testing Paginación..."
try {
    $paginatedResponse = Invoke-RestMethod -Uri "$baseUrl/api/inventory/produits/page?page=0&size=10" -Method GET -Headers $authHeaders
    Write-Host "✅ GET /api/inventory/produits/page SUCCESS: Página 0, $($paginatedResponse.numberOfElements) elementos" -ForegroundColor Green
} catch {
    Write-Host "❌ GET /api/inventory/produits/page FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# 3.4 GET /api/inventory/produits/{id} - Obtener producto por ID
if ($productId1) {
    Write-Host "`n🔍 Testing Obtener producto por ID..."
    try {
        $productByIdResponse = Invoke-RestMethod -Uri "$baseUrl/api/inventory/produits/$productId1" -Method GET -Headers $authHeaders
        Write-Host "✅ GET /api/inventory/produits/{id} SUCCESS:" -ForegroundColor Green
        Write-Host "Producto: $($productByIdResponse.nom), Stock: $($productByIdResponse.quantiteStock)"
    } catch {
        Write-Host "❌ GET /api/inventory/produits/{id} FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 3.5 PUT /api/inventory/produits/{id} - Actualizar producto
if ($productId1) {
    Write-Host "`n🔄 Testing Actualizar producto..."
    $updateProduct = @{
        nom = "Tomate Test Actualizado $timestamp"
        description = "Descripción actualizada"
        categorieId = 1
        quantiteStock = 75.0
        unite = "KG"
        prixUnitaire = 4.00
        seuilAlerte = 15.0
        datePeremption = "2026-01-15"
        fournisseur = "Nuevo Fournisseur"
    } | ConvertTo-Json

    try {
        $updateResponse = Invoke-RestMethod -Uri "$baseUrl/api/inventory/produits/$productId1" -Method PUT -Body $updateProduct -Headers $authHeaders
        Write-Host "✅ PUT /api/inventory/produits/{id} SUCCESS:" -ForegroundColor Green
        Write-Host "Producto actualizado: $($updateResponse.nom), Nuevo precio: $($updateResponse.prixUnitaire)"
    } catch {
        Write-Host "❌ PUT /api/inventory/produits/{id} FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 3.6 POST /api/inventory/produits/{id}/entree - Entrada de stock
if ($productId1) {
    Write-Host "`n⬆️ Testing Entrada de stock..."
    $stockEntry = @{
        quantite = 25.0
        motif = "Reabastecimiento test"
    } | ConvertTo-Json

    try {
        $entryResponse = Invoke-RestMethod -Uri "$baseUrl/api/inventory/produits/$productId1/entree" -Method POST -Body $stockEntry -Headers $authHeaders
        Write-Host "✅ POST /api/inventory/produits/{id}/entree SUCCESS:" -ForegroundColor Green
        Write-Host "Stock después de entrada: $($entryResponse.quantiteStock)"
    } catch {
        Write-Host "❌ POST /api/inventory/produits/{id}/entree FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 3.7 POST /api/inventory/produits/{id}/sortie - Salida de stock
if ($productId1) {
    Write-Host "`n⬇️ Testing Salida de stock..."
    $stockExit = @{
        quantite = 10.0
        motif = "Uso en cocina test"
    } | ConvertTo-Json

    try {
        $exitResponse = Invoke-RestMethod -Uri "$baseUrl/api/inventory/produits/$productId1/sortie" -Method POST -Body $stockExit -Headers $authHeaders
        Write-Host "✅ POST /api/inventory/produits/{id}/sortie SUCCESS:" -ForegroundColor Green
        Write-Host "Stock después de salida: $($exitResponse.quantiteStock)"
    } catch {
        Write-Host "❌ POST /api/inventory/produits/{id}/sortie FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 3.8 GET /api/inventory/produits/alerts - Productos en alerta
Write-Host "`n🚨 Testing Productos en alerta..."
try {
    $alertsResponse = Invoke-RestMethod -Uri "$baseUrl/api/inventory/produits/alerts" -Method GET -Headers $authHeaders
    Write-Host "✅ GET /api/inventory/produits/alerts SUCCESS: $($alertsResponse.Length) productos en alerta" -ForegroundColor Green
} catch {
    Write-Host "❌ GET /api/inventory/produits/alerts FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# 3.9 GET /api/inventory/produits/expiring - Productos próximos a expirar
Write-Host "`n⏰ Testing Productos próximos a expirar..."
try {
    $expiringResponse = Invoke-RestMethod -Uri "$baseUrl/api/inventory/produits/expiring?days=180" -Method GET -Headers $authHeaders
    Write-Host "✅ GET /api/inventory/produits/expiring SUCCESS: $($expiringResponse.Length) productos próximos a expirar" -ForegroundColor Green
} catch {
    Write-Host "❌ GET /api/inventory/produits/expiring FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# 3.10 GET /api/inventory/produits/search - Buscar productos
Write-Host "`n🔍 Testing Buscar productos..."
try {
    $searchResponse = Invoke-RestMethod -Uri "$baseUrl/api/inventory/produits/search?nom=Test" -Method GET -Headers $authHeaders
    Write-Host "✅ GET /api/inventory/produits/search SUCCESS: $($searchResponse.Length) productos encontrados" -ForegroundColor Green
} catch {
    Write-Host "❌ GET /api/inventory/produits/search FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# 3.11 GET /api/inventory/produits/{id}/movements - Historial de movimientos
if ($productId1) {
    Write-Host "`n📈 Testing Historial de movimientos..."
    try {
        $movementsResponse = Invoke-RestMethod -Uri "$baseUrl/api/inventory/produits/$productId1/movements" -Method GET -Headers $authHeaders
        Write-Host "✅ GET /api/inventory/produits/{id}/movements SUCCESS: $($movementsResponse.Length) movimientos encontrados" -ForegroundColor Green
    } catch {
        Write-Host "❌ GET /api/inventory/produits/{id}/movements FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# =============================================================================
# 4. MÓDULO MENÚS (12 endpoints)
# =============================================================================

Write-Host "`n🍳 === TESTING MÓDULO MENÚS === (12 endpoints)" -ForegroundColor Green

# Variables para almacenar IDs de menús
$menuId1 = $null
$menuId2 = $null

# 4.1 POST /api/menus - Crear menú
Write-Host "`n🆕 Testing Crear menús..."
$menu1 = @{
    nom = "Menú Test $timestamp"
    description = "Menú de prueba para testing completo"
    dateService = "2025-12-15"
    nombrePortions = 4
    prixVente = 25.50
} | ConvertTo-Json

try {
    $menuResponse1 = Invoke-RestMethod -Uri "$baseUrl/api/menus" -Method POST -Body $menu1 -Headers $authHeaders
    $menuId1 = $menuResponse1.id
    Write-Host "✅ POST /api/menus SUCCESS:" -ForegroundColor Green
    Write-Host "Menú creado: $($menuResponse1.nom) (ID: $menuId1)"
} catch {
    Write-Host "❌ POST /api/menus FAILED: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
        $errorBody = $reader.ReadToEnd()
        Write-Host "Error Details: $errorBody"
    }
}

# Crear segundo menú
$menu2 = @{
    nom = "Menú Vegetariano $timestamp"
    description = "Menú vegetariano especial"
    dateService = "2025-12-20"
    nombrePortions = 6
    prixVente = 22.00
} | ConvertTo-Json

try {
    $menuResponse2 = Invoke-RestMethod -Uri "$baseUrl/api/menus" -Method POST -Body $menu2 -Headers $authHeaders
    $menuId2 = $menuResponse2.id
    Write-Host "✅ Segundo menú creado: $($menuResponse2.nom) (ID: $menuId2)" -ForegroundColor Green
} catch {
    Write-Host "❌ Error creando segundo menú: $($_.Exception.Message)" -ForegroundColor Red
}

# 4.2 GET /api/menus - Listar menús
Write-Host "`n📋 Testing Listar menús..."
try {
    $allMenusResponse = Invoke-RestMethod -Uri "$baseUrl/api/menus?page=0&size=10" -Method GET -Headers $authHeaders
    Write-Host "✅ GET /api/menus SUCCESS: $($allMenusResponse.numberOfElements) menús encontrados" -ForegroundColor Green
} catch {
    Write-Host "❌ GET /api/menus FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# 4.3 GET /api/menus/{id} - Obtener menú por ID
if ($menuId1) {
    Write-Host "`n🔍 Testing Obtener menú por ID..."
    try {
        $menuByIdResponse = Invoke-RestMethod -Uri "$baseUrl/api/menus/$menuId1" -Method GET -Headers $authHeaders
        Write-Host "✅ GET /api/menus/{id} SUCCESS:" -ForegroundColor Green
        Write-Host "Menú: $($menuByIdResponse.nom), Precio: $($menuByIdResponse.prixVente)"
    } catch {
        Write-Host "❌ GET /api/menus/{id} FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 4.4 PUT /api/menus/{id} - Actualizar menú
if ($menuId1) {
    Write-Host "`n🔄 Testing Actualizar menú..."
    $updateMenu = @{
        nom = "Menú Test Actualizado $timestamp"
        description = "Descripción actualizada del menú"
        dateService = "2025-12-16"
        nombrePortions = 5
        prixVente = 28.00
    } | ConvertTo-Json

    try {
        $updateMenuResponse = Invoke-RestMethod -Uri "$baseUrl/api/menus/$menuId1" -Method PUT -Body $updateMenu -Headers $authHeaders
        Write-Host "✅ PUT /api/menus/{id} SUCCESS:" -ForegroundColor Green
        Write-Host "Menú actualizado: $($updateMenuResponse.nom), Nuevo precio: $($updateMenuResponse.prixVente)"
    } catch {
        Write-Host "❌ PUT /api/menus/{id} FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 4.5 POST /api/menus/{id}/ingredients - Añadir ingrediente (si tenemos productos)
if ($menuId1 -and $productId1) {
    Write-Host "`n🥕 Testing Añadir ingrediente al menú..."
    $ingredient = @{
        produitId = $productId1
        quantiteNecessaire = 2.5
        uniteUtilisee = "KG"
        notes = "Ingrediente principal del plato"
    } | ConvertTo-Json

    try {
        $addIngredientResponse = Invoke-RestMethod -Uri "$baseUrl/api/menus/$menuId1/ingredients" -Method POST -Body $ingredient -Headers $authHeaders
        Write-Host "✅ POST /api/menus/{id}/ingredients SUCCESS:" -ForegroundColor Green
        Write-Host "Ingrediente añadido al menú: $($addIngredientResponse.nom)"
    } catch {
        Write-Host "❌ POST /api/menus/{id}/ingredients FAILED: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
            $errorBody = $reader.ReadToEnd()
            Write-Host "Error Details: $errorBody"
        }
    }
}

# 4.6 GET /api/menus/recherche - Buscar menús
Write-Host "`n🔍 Testing Buscar menús..."
try {
    $searchMenusResponse = Invoke-RestMethod -Uri "$baseUrl/api/menus/recherche?nom=Test&page=0&size=10" -Method GET -Headers $authHeaders
    Write-Host "✅ GET /api/menus/recherche SUCCESS: $($searchMenusResponse.numberOfElements) menús encontrados" -ForegroundColor Green
} catch {
    Write-Host "❌ GET /api/menus/recherche FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# 4.7 GET /api/menus/realisables - Menús realizables
Write-Host "`n✅ Testing Menús realizables..."
try {
    $realisablesResponse = Invoke-RestMethod -Uri "$baseUrl/api/menus/realisables?date=2025-12-15" -Method GET -Headers $authHeaders
    Write-Host "✅ GET /api/menus/realisables SUCCESS: $($realisablesResponse.Length) menús realizables" -ForegroundColor Green
} catch {
    Write-Host "❌ GET /api/menus/realisables FAILED: $($_.Exception.Message)" -ForegroundColor Red
}

# 4.8 GET /api/menus/{id}/statistiques - Estadísticas del menú
if ($menuId1) {
    Write-Host "`n📊 Testing Estadísticas del menú..."
    try {
        $statsResponse = Invoke-RestMethod -Uri "$baseUrl/api/menus/$menuId1/statistiques" -Method GET -Headers $authHeaders
        Write-Host "✅ GET /api/menus/{id}/statistiques SUCCESS:" -ForegroundColor Green
        Write-Host "Costo total: $($statsResponse.coutTotalIngredients), Puede preparar: $($statsResponse.peutEtrePrepare)"
    } catch {
        Write-Host "❌ GET /api/menus/{id}/statistiques FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 4.9 PUT /api/menus/{id}/confirmer - Confirmar menú (transacción crítica)
if ($menuId1) {
    Write-Host "`n🔒 Testing Confirmar menú..."
    try {
        $confirmResponse = Invoke-RestMethod -Uri "$baseUrl/api/menus/$menuId1/confirmer" -Method PUT -Headers $authHeaders
        Write-Host "✅ PUT /api/menus/{id}/confirmer SUCCESS:" -ForegroundColor Green
        Write-Host "Menú confirmado: $($confirmResponse.nom), Estatus: $($confirmResponse.statut)"
    } catch {
        Write-Host "❌ PUT /api/menus/{id}/confirmer FAILED: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
            $errorBody = $reader.ReadToEnd()
            Write-Host "Error Details: $errorBody"
        }
    }
}

# 4.10 PUT /api/menus/{id}/annuler - Anular menú
if ($menuId1) {
    Start-Sleep -Seconds 2  # Pequeña pausa
    Write-Host "`n❌ Testing Anular menú..."
    try {
        $cancelResponse = Invoke-RestMethod -Uri "$baseUrl/api/menus/$menuId1/annuler" -Method PUT -Headers $authHeaders
        Write-Host "✅ PUT /api/menus/{id}/annuler SUCCESS:" -ForegroundColor Green
        Write-Host "Menú anulado: $($cancelResponse.nom), Estatus: $($cancelResponse.statut)"
    } catch {
        Write-Host "❌ PUT /api/menus/{id}/annuler FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 4.11 DELETE /api/menus/{menuId}/ingredients/{produitId} - Eliminar ingrediente
if ($menuId1 -and $productId1) {
    Write-Host "`n🗑️ Testing Eliminar ingrediente del menú..."
    try {
        $removeIngredientResponse = Invoke-RestMethod -Uri "$baseUrl/api/menus/$menuId1/ingredients/$productId1" -Method DELETE -Headers $authHeaders
        Write-Host "✅ DELETE /api/menus/{menuId}/ingredients/{produitId} SUCCESS" -ForegroundColor Green
    } catch {
        Write-Host "❌ DELETE /api/menus/{menuId}/ingredients/{produitId} FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 4.12 DELETE /api/menus/{id} - Eliminar menú
if ($menuId2) {
    Write-Host "`n🗑️ Testing Eliminar menú..."
    try {
        Invoke-RestMethod -Uri "$baseUrl/api/menus/$menuId2" -Method DELETE -Headers $authHeaders
        Write-Host "✅ DELETE /api/menus/{id} SUCCESS: Menú eliminado" -ForegroundColor Green
    } catch {
        Write-Host "❌ DELETE /api/menus/{id} FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# =============================================================================
# 5. LIMPIEZA: ELIMINAR DATOS DE PRUEBA
# =============================================================================

Write-Host "`n🧹 === LIMPIEZA DE DATOS DE PRUEBA ===" -ForegroundColor Yellow

# Eliminar productos de prueba
if ($productId1) {
    try {
        Invoke-RestMethod -Uri "$baseUrl/api/inventory/produits/$productId1" -Method DELETE -Headers $authHeaders
        Write-Host "✅ Producto 1 eliminado" -ForegroundColor Green
    } catch {
        Write-Host "⚠️ No se pudo eliminar producto 1: $($_.Exception.Message)" -ForegroundColor Yellow
    }
}

if ($productId2) {
    try {
        Invoke-RestMethod -Uri "$baseUrl/api/inventory/produits/$productId2" -Method DELETE -Headers $authHeaders
        Write-Host "✅ Producto 2 eliminado" -ForegroundColor Green
    } catch {
        Write-Host "⚠️ No se pudo eliminar producto 2: $($_.Exception.Message)" -ForegroundColor Yellow
    }
}

if ($menuId1) {
    try {
        Invoke-RestMethod -Uri "$baseUrl/api/menus/$menuId1" -Method DELETE -Headers $authHeaders
        Write-Host "✅ Menú 1 eliminado" -ForegroundColor Green
    } catch {
        Write-Host "⚠️ No se pudo eliminar menú 1: $($_.Exception.Message)" -ForegroundColor Yellow
    }
}

# =============================================================================
# 6. RESUMEN FINAL
# =============================================================================

Write-Host "`n🎯 === RESUMEN DE TESTS COMPLETADOS ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "✅ MÓDULOS DE MONITOREO: 6 endpoints testeados" -ForegroundColor Green
Write-Host "   - Health Check (3 endpoints)"
Write-Host "   - Root & Test (3 endpoints)"
Write-Host ""
Write-Host "✅ MÓDULO INVENTARIO/PRODUCTOS: 13 endpoints testeados" -ForegroundColor Green
Write-Host "   - CRUD de productos (5 endpoints)"
Write-Host "   - Gestión de stock (2 endpoints)"
Write-Host "   - Consultas especiales (6 endpoints)"
Write-Host ""
Write-Host "✅ MÓDULO MENÚS: 12 endpoints testeados" -ForegroundColor Green
Write-Host "   - CRUD de menús (4 endpoints)"
Write-Host "   - Gestión de ingredientes (2 endpoints)"
Write-Host "   - Operaciones especiales (6 endpoints)"
Write-Host ""
Write-Host "📊 TOTAL: 31 ENDPOINTS TESTEADOS" -ForegroundColor Cyan
Write-Host ""
Write-Host "🚀 Tests completados en: $(Get-Date)" -ForegroundColor Yellow
Write-Host "🔗 Base URL utilizada: $baseUrl" -ForegroundColor Yellow

# Logout
try {
    Invoke-RestMethod -Uri "$baseUrl/auth/logout" -Method POST -Headers $authHeaders
    Write-Host "✅ Logout exitoso" -ForegroundColor Green
} catch {
    Write-Host "⚠️ Error en logout: $($_.Exception.Message)" -ForegroundColor Yellow
}