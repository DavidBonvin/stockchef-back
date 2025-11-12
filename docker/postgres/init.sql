-- Inicialización de la base de datos PostgreSQL para StockChef
-- Este script se ejecuta automáticamente al crear el contenedor

-- Crear extensiones útiles si están disponibles
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Verificar que la base de datos está lista
SELECT 'Base de datos PostgreSQL inicializada correctamente para StockChef' AS status;