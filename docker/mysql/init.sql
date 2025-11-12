-- Inicialización de la base de datos MySQL para StockChef
-- Este script se ejecuta automáticamente al crear el contenedor

-- Crear usuario adicional si no existe
CREATE USER IF NOT EXISTS 'stockchef_user'@'%' IDENTIFIED BY 'UserAdmin';

-- Otorgar permisos al usuario
GRANT ALL PRIVILEGES ON stockchef_db.* TO 'stockchef_user'@'%';

-- Aplicar cambios
FLUSH PRIVILEGES;

-- Verificar que la base de datos está lista
SELECT 'Base de datos MySQL inicializada correctamente para StockChef' AS status;