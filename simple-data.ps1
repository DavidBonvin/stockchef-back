# Script simple para crear datos de muestra
Write-Host "Creando datos de muestra para StockChef..."

# Obtener token
$loginResponse = Invoke-WebRequest -Uri "http://localhost:8090/api/auth/login" -Method POST -Body '{"email": "chef@stockchef.com", "password": "chefpass123"}' -ContentType "application/json" -UseBasicParsing
$loginData = $loginResponse.Content | ConvertFrom-Json
$token = $loginData.token
$headers = @{ Authorization = "Bearer $token" }
Write-Host "Token obtenido exitosamente"

# Crear productos
Write-Host "Creando productos..."
$productos = @(
    @{ nom = "Tomates Cherry"; quantiteInitiale = 5.500; unite = "KILOGRAMME"; prixUnitaire = 6.80; seuilAlerte = 1.000; datePeremption = "2025-12-08" },
    @{ nom = "Aceite Oliva"; quantiteInitiale = 2.000; unite = "LITRE"; prixUnitaire = 12.50; seuilAlerte = 0.500; datePeremption = "2026-06-15" },
    @{ nom = "Queso Mozzarella"; quantiteInitiale = 1.800; unite = "KILOGRAMME"; prixUnitaire = 15.00; seuilAlerte = 0.300; datePeremption = "2025-12-10" },
    @{ nom = "Pasta Penne"; quantiteInitiale = 8.000; unite = "KILOGRAMME"; prixUnitaire = 3.20; seuilAlerte = 2.000; datePeremption = "2026-03-20" },
    @{ nom = "Carne Res"; quantiteInitiale = 3.500; unite = "KILOGRAMME"; prixUnitaire = 28.00; seuilAlerte = 1.000; datePeremption = "2025-12-07" },
    @{ nom = "Pollo Entero"; quantiteInitiale = 6.000; unite = "KILOGRAMME"; prixUnitaire = 8.50; seuilAlerte = 2.000; datePeremption = "2025-12-09" },
    @{ nom = "Salmon Fresco"; quantiteInitiale = 0.200; unite = "KILOGRAMME"; prixUnitaire = 35.00; seuilAlerte = 0.500; datePeremption = "2025-12-05" },
    @{ nom = "Huevos"; quantiteInitiale = 120.000; unite = "UNITE"; prixUnitaire = 0.25; seuilAlerte = 24.000; datePeremption = "2025-12-14" },
    @{ nom = "Albahaca"; quantiteInitiale = 0.050; unite = "KILOGRAMME"; prixUnitaire = 25.00; seuilAlerte = 0.100; datePeremption = "2025-12-06" },
    @{ nom = "Limones"; quantiteInitiale = 2.000; unite = "KILOGRAMME"; prixUnitaire = 3.80; seuilAlerte = 0.500; datePeremption = "2025-12-11" }
)

$productIds = @{}
foreach ($producto in $productos) {
    try {
        $body = $producto | ConvertTo-Json
        $response = Invoke-WebRequest -Uri "http://localhost:8090/api/inventory/produits" -Method POST -Body $body -Headers $headers -ContentType "application/json" -UseBasicParsing
        $productData = $response.Content | ConvertFrom-Json
        $productIds[$producto.nom] = $productData.id
        Write-Host "Producto creado: $($producto.nom) (ID: $($productData.id))"
    } catch {
        Write-Host "Error creando producto $($producto.nom)"
    }
}

# Crear menus
Write-Host "Creando menus..."
$menus = @(
    @{ nom = "Pasta Carbonara"; description = "Pasta con salsa carbonara tradicional"; dateService = "2025-11-28"; prixVente = 16.50 },
    @{ nom = "Ensalada Caprese"; description = "Tomates mozzarella y albahaca"; dateService = "2025-11-29"; prixVente = 12.00 },
    @{ nom = "Salmon Grillado"; description = "Salmon fresco con hierbas"; dateService = "2025-11-30"; prixVente = 24.00 },
    @{ nom = "Pollo al Limon"; description = "Pollo con salsa de limon"; dateService = "2025-12-01"; prixVente = 18.50 }
)

$menuIds = @{}
foreach ($menu in $menus) {
    try {
        $body = $menu | ConvertTo-Json
        Start-Sleep -Milliseconds 500
        $response = Invoke-WebRequest -Uri "http://localhost:8090/api/menus" -Method POST -Body $body -Headers $headers -ContentType "application/json" -UseBasicParsing
        $menuData = $response.Content | ConvertFrom-Json
        $menuIds[$menu.nom] = $menuData.id
        Write-Host "Menu creado: $($menu.nom) (ID: $($menuData.id))"
    } catch {
        Write-Host "Error creando menu $($menu.nom): $($_.Exception.Message)"
    }
}

Write-Host "Datos de muestra creados exitosamente!"
Write-Host "Productos creados: $($productIds.Count)"
Write-Host "Menus creados: $($menuIds.Count)"