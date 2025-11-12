-- =============================================================================
-- SCRIPT DE CONFIGURACIÓN DE BASE DE DATOS POSTGRESQL PARA STOCKCHEF
-- =============================================================================

-- Eliminar base de datos si existe (para desarrollo)
DROP DATABASE IF EXISTS stockchef_db;
DROP USER IF EXISTS stockchef_user;

-- Crear usuario específico para la aplicación
CREATE USER stockchef_user WITH PASSWORD 'stockchef_pass';

-- Crear base de datos
CREATE DATABASE stockchef_db 
    OWNER stockchef_user 
    ENCODING 'UTF8' 
    LC_COLLATE = 'en_US.utf8' 
    LC_CTYPE = 'en_US.utf8';

-- Conectar a la base de datos stockchef_db
\c stockchef_db;

-- Otorgar permisos al usuario
GRANT ALL PRIVILEGES ON DATABASE stockchef_db TO stockchef_user;
GRANT ALL ON SCHEMA public TO stockchef_user;

-- Habilitar extensiones útiles
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Mensaje de confirmación
\echo 'Base de datos stockchef_db configurada correctamente'
\echo 'Usuario: stockchef_user'
\echo 'Contraseña: stockchef_pass'
\echo 'Base de datos: stockchef_db'
\echo ''
\echo 'Para conectar desde Spring Boot usar:'
\echo 'spring.profiles.active=postgres'