# Script PowerShell para crear datos de muestra en StockChef
# Llena la base de datos con productos, menús y movimientos históricos

Write-Host "=== CREANDO DATOS DE MUESTRA PARA STOCKCHEF ===" -ForegroundColor Green

# Obtener token de autenticación
Write-Host "`n1. Obteniendo token de autenticación..."
try {
    $loginResponse = Invoke-WebRequest -Uri "http://localhost:8090/api/auth/login" -Method POST -Body '{"email": "chef@stockchef.com", "password": "chefpass123"}' -ContentType "application/json" -UseBasicParsing
    $loginData = $loginResponse.Content | ConvertFrom-Json
    $token = $loginData.token
    $headers = @{ Authorization = "Bearer $token" }
    Write-Host "✅ Token obtenido exitosamente" -ForegroundColor Green
} catch {
    Write-Host "❌ Error obteniendo token: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Productos de muestra con diferentes fechas de vencimiento
Write-Host "`n2. Creando productos de inventario..."
$productos = @(
    @{ nom = "Tomates Cherry"; quantiteStock = 5.500; unite = "KILOGRAMME"; prixUnitaire = 6.80; seuilAlerte = 1.000; datePeremption = "2025-12-08" },
    @{ nom = "Aceite de Oliva Extra"; quantiteStock = 2.000; unite = "LITRE"; prixUnitaire = 12.50; seuilAlerte = 0.500; datePeremption = "2026-06-15" },
    @{ nom = "Queso Mozzarella"; quantiteStock = 1.800; unite = "KILOGRAMME"; prixUnitaire = 15.00; seuilAlerte = 0.300; datePeremption = "2025-12-10" },
    @{ nom = "Pasta Penne"; quantiteStock = 8.000; unite = "KILOGRAMME"; prixUnitaire = 3.20; seuilAlerte = 2.000; datePeremption = "2026-03-20" },
    @{ nom = "Cebolla Blanca"; quantiteStock = 4.200; unite = "KILOGRAMME"; prixUnitaire = 2.10; seuilAlerte = 1.000; datePeremption = "2025-12-20" },
    @{ nom = "Ajo"; quantiteStock = 0.800; unite = "KILOGRAMME"; prixUnitaire = 8.50; seuilAlerte = 0.200; datePeremption = "2025-12-15" },
    @{ nom = "Albahaca Fresca"; quantiteStock = 0.150; unite = "KILOGRAMME"; prixUnitaire = 25.00; seuilAlerte = 0.100; datePeremption = "2025-12-06" },
    @{ nom = "Sal Marina"; quantiteStock = 5.000; unite = "KILOGRAMME"; prixUnitaire = 1.80; seuilAlerte = 1.000; datePeremption = "2027-01-01" },
    @{ nom = "Pimienta Negra"; quantiteStock = 0.500; unite = "KILOGRAMME"; prixUnitaire = 18.00; seuilAlerte = 0.100; datePeremption = "2026-12-31" },
    @{ nom = "Carne de Res"; quantiteStock = 3.500; unite = "KILOGRAMME"; prixUnitaire = 28.00; seuilAlerte = 1.000; datePeremption = "2025-12-07" },
    @{ nom = "Pollo Entero"; quantiteStock = 6.000; unite = "KILOGRAMME"; prixUnitaire = 8.50; seuilAlerte = 2.000; datePeremption = "2025-12-09" },
    @{ nom = "Salmón Fresco"; quantiteStock = 2.200; unite = "KILOGRAMME"; prixUnitaire = 35.00; seuilAlerte = 0.500; datePeremption = "2025-12-05" },
    @{ nom = "Leche Entera"; quantiteStock = 3.000; unite = "LITRE"; prixUnitaire = 1.20; seuilAlerte = 1.000; datePeremption = "2025-12-12" },
    @{ nom = "Mantequilla"; quantiteStock = 1.000; unite = "KILOGRAMME"; prixUnitaire = 6.50; seuilAlerte = 0.250; datePeremption = "2025-12-18" },
    @{ nom = "Huevos"; quantiteStock = 120.000; unite = "UNITE"; prixUnitaire = 0.25; seuilAlerte = 24.000; datePeremption = "2025-12-14" },
    @{ nom = "Harina de Trigo"; quantiteStock = 10.000; unite = "KILOGRAMME"; prixUnitaire = 1.50; seuilAlerte = 2.000; datePeremption = "2026-08-30" },
    @{ nom = "Azúcar Blanco"; quantiteStock = 5.000; unite = "KILOGRAMME"; prixUnitaire = 1.10; seuilAlerte = 1.000; datePeremption = "2027-01-01" },
    @{ nom = "Limones"; quantiteStock = 2.000; unite = "KILOGRAMME"; prixUnitaire = 3.80; seuilAlerte = 0.500; datePeremption = "2025-12-11" }
)

$productIds = @{}
foreach ($producto in $productos) {
    try {
        $body = $producto | ConvertTo-Json
        $response = Invoke-WebRequest -Uri "http://localhost:8090/api/inventory/produits" -Method POST -Body $body -Headers $headers -ContentType "application/json" -UseBasicParsing
        $productData = $response.Content | ConvertFrom-Json
        $productIds[$producto.nom] = $productData.id
        Write-Host "✅ Producto creado: $($producto.nom) (ID: $($productData.id))" -ForegroundColor Green
    } catch {
        Write-Host "⚠️  Error creando producto $($producto.nom): $($_.Exception.Message)" -ForegroundColor Yellow
    }
}

# Crear menús con diferentes fechas
Write-Host "`n3. Creando menús históricos..."
$menus = @(
    @{ nom = "Pasta Carbonara Clásica"; description = "Pasta con salsa carbonara tradicional italiana"; dateService = "2025-11-28"; prixVente = 16.50 },
    @{ nom = "Ensalada Caprese"; description = "Tomates cherry, mozzarella y albahaca fresca"; dateService = "2025-11-29"; prixVente = 12.00 },
    @{ nom = "Filete de Salmón Grillado"; description = "Salmón fresco con hierbas mediterráneas"; dateService = "2025-11-30"; prixVente = 24.00 },
    @{ nom = "Pollo al Limón"; description = "Pollo tierno con salsa de limón y hierbas"; dateService = "2025-12-01"; prixVente = 18.50 },
    @{ nom = "Pasta Aglio e Olio"; description = "Pasta con ajo, aceite de oliva y perejil"; dateService = "2025-12-02"; prixVente = 14.00 },
    @{ nom = "Filete de Res Premium"; description = "Corte premium de res con guarnición"; dateService = "2025-12-03"; prixVente = 32.00 },
    @{ nom = "Tortilla Francesa"; description = "Tortilla esponjosa con hierbas finas"; dateService = "2025-12-04"; prixVente = 9.50 }
)

$menuIds = @{}
foreach ($menu in $menus) {
    try {
        $body = $menu | ConvertTo-Json
        $response = Invoke-WebRequest -Uri "http://localhost:8090/api/menus" -Method POST -Body $body -Headers $headers -ContentType "application/json" -UseBasicParsing
        $menuData = $response.Content | ConvertFrom-Json
        $menuIds[$menu.nom] = $menuData.id
        Write-Host "✅ Menú creado: $($menu.nom) (ID: $($menuData.id))" -ForegroundColor Green
    } catch {
        Write-Host "⚠️  Error creando menú $($menu.nom): $($_.Exception.Message)" -ForegroundColor Yellow
    }
}

# Agregar ingredientes a los menús
Write-Host "`n4. Agregando ingredientes a menús..."

# Pasta Carbonara
if ($menuIds["Pasta Carbonara Clásica"] -and $productIds["Pasta Penne"]) {
    $ingredients = @(
        @{ produitId = $productIds["Pasta Penne"]; quantiteNecessaire = 0.120; uniteUtilisee = "KILOGRAMME"; notes = "Pasta al dente" },
        @{ produitId = $productIds["Huevos"]; quantiteNecessaire = 2.000; uniteUtilisee = "UNITE"; notes = "Huevos frescos" },
        @{ produitId = $productIds["Queso Mozzarella"]; quantiteNecessaire = 0.050; uniteUtilisee = "KILOGRAMME"; notes = "Queso rallado" }
    )
    foreach ($ingredient in $ingredients) {
        try {
            $body = $ingredient | ConvertTo-Json
            Invoke-WebRequest -Uri "http://localhost:8090/api/menus/$($menuIds['Pasta Carbonara Clásica'])/ingredients" -Method POST -Body $body -Headers $headers -ContentType "application/json" -UseBasicParsing | Out-Null
        } catch { Write-Host "⚠️  Error agregando ingrediente" -ForegroundColor Yellow }
    }
}

# Ensalada Caprese
if ($menuIds["Ensalada Caprese"] -and $productIds["Tomates Cherry"]) {
    $ingredients = @(
        @{ produitId = $productIds["Tomates Cherry"]; quantiteNecessaire = 0.200; uniteUtilisee = "KILOGRAMME"; notes = "Tomates maduros" },
        @{ produitId = $productIds["Queso Mozzarella"]; quantiteNecessaire = 0.150; uniteUtilisee = "KILOGRAMME"; notes = "Mozzarella fresca" },
        @{ produitId = $productIds["Albahaca Fresca"]; quantiteNecessaire = 0.010; uniteUtilisee = "KILOGRAMME"; notes = "Hojas frescas" },
        @{ produitId = $productIds["Aceite de Oliva Extra"]; quantiteNecessaire = 0.020; uniteUtilisee = "LITRE"; notes = "Aceite de calidad" }
    )
    foreach ($ingredient in $ingredients) {
        try {
            $body = $ingredient | ConvertTo-Json
            Invoke-WebRequest -Uri "http://localhost:8090/api/menus/$($menuIds['Ensalada Caprese'])/ingredients" -Method POST -Body $body -Headers $headers -ContentType "application/json" -UseBasicParsing | Out-Null
        } catch { Write-Host "⚠️  Error agregando ingrediente" -ForegroundColor Yellow }
    }
}

# Salmón Grillado
if ($menuIds["Filete de Salmón Grillado"] -and $productIds["Salmón Fresco"]) {
    $ingredients = @(
        @{ produitId = $productIds["Salmón Fresco"]; quantiteNecessaire = 0.180; uniteUtilisee = "KILOGRAMME"; notes = "Filete fresco" },
        @{ produitId = $productIds["Limones"]; quantiteNecessaire = 0.100; uniteUtilisee = "KILOGRAMME"; notes = "Para salsa" },
        @{ produitId = $productIds["Aceite de Oliva Extra"]; quantiteNecessaire = 0.015; uniteUtilisee = "LITRE"; notes = "Para cocción" }
    )
    foreach ($ingredient in $ingredients) {
        try {
            $body = $ingredient | ConvertTo-Json
            Invoke-WebRequest -Uri "http://localhost:8090/api/menus/$($menuIds['Filete de Salmón Grillado'])/ingredients" -Method POST -Body $body -Headers $headers -ContentType "application/json" -UseBasicParsing | Out-Null
        } catch { Write-Host "⚠️  Error agregando ingrediente" -ForegroundColor Yellow }
    }
}

# Confirmar algunos menús para generar movimientos de stock
Write-Host "`n5. Confirmando menús para generar movimientos..."
$menusToConfirm = @("Ensalada Caprese", "Pasta Carbonara Clásica")

foreach ($menuName in $menusToConfirm) {
    if ($menuIds[$menuName]) {
        try {
            Invoke-WebRequest -Uri "http://localhost:8090/api/menus/$($menuIds[$menuName])/confirmer" -Method PUT -Headers $headers -UseBasicParsing | Out-Null
            Write-Host "✅ Menú confirmado: $menuName" -ForegroundColor Green
        } catch {
            Write-Host "⚠️  Error confirmando menú $menuName" -ForegroundColor Yellow
        }
    }
}

# Crear movimientos de stock adicionales (entradas/salidas)
Write-Host "`n6. Creando movimientos de stock históricos..."
$movimientos = @(
    @{ productName = "Tomates Cherry"; quantite = 2.000; motif = "Entrega proveedor semanal" },
    @{ productName = "Aceite de Oliva Extra"; quantite = 1.000; motif = "Reposición stock" },
    @{ productName = "Queso Mozzarella"; quantite = 0.500; motif = "Compra urgente" },
    @{ productName = "Salmón Fresco"; quantite = 1.500; motif = "Entrega pescadería" }
)

foreach ($movimiento in $movimientos) {
    if ($productIds[$movimiento.productName]) {
        try {
            $body = @{
                quantite = $movimiento.quantite
                motif = $movimiento.motif
                notes = "Movimiento histórico de muestra"
            } | ConvertTo-Json
            
            Invoke-WebRequest -Uri "http://localhost:8090/api/inventory/produits/$($productIds[$movimiento.productName])/entree" -Method POST -Body $body -Headers $headers -ContentType "application/json" -UseBasicParsing | Out-Null
            Write-Host "✅ Movimiento creado para: $($movimiento.productName)" -ForegroundColor Green
        } catch {
            Write-Host "⚠️  Error creando movimiento para $($movimiento.productName)" -ForegroundColor Yellow
        }
    }
}

Write-Host "`n🎉 DATOS DE MUESTRA CREADOS EXITOSAMENTE!" -ForegroundColor Green
Write-Host "📊 Resumen:" -ForegroundColor Cyan
Write-Host "   - $($productos.Count) productos de inventario" -ForegroundColor White
Write-Host "   - $($menus.Count) menús con diferentes fechas" -ForegroundColor White
Write-Host "   - Ingredientes asignados a varios menús" -ForegroundColor White
Write-Host "   - Movimientos de stock históricos" -ForegroundColor White
Write-Host "   - Algunos productos próximos a vencer (para alertas)" -ForegroundColor White
Write-Host "Base de datos lista para probar los informes!" -ForegroundColor Green