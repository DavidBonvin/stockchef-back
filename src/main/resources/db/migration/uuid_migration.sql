-- Migration script: Migrar ID de Long a UUID
-- Para StockChef Backend - Base de datos PostgreSQL

-- Paso 1: Crear tabla temporal para preservar datos
CREATE TABLE users_backup AS 
SELECT * FROM users;

-- Paso 2: Agregar nueva columna UUID
ALTER TABLE users ADD COLUMN new_id VARCHAR(36);

-- Paso 3: Generar UUIDs únicos para registros existentes
UPDATE users SET new_id = gen_random_uuid()::text WHERE new_id IS NULL;

-- Paso 4: Eliminar restricciones de la columna ID actual
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_pkey;

-- Paso 5: Eliminar la columna ID antigua
ALTER TABLE users DROP COLUMN id;

-- Paso 6: Renombrar la nueva columna
ALTER TABLE users RENAME COLUMN new_id TO id;

-- Paso 7: Configurar la nueva columna como PRIMARY KEY
ALTER TABLE users ALTER COLUMN id SET NOT NULL;
ALTER TABLE users ADD CONSTRAINT users_pkey PRIMARY KEY (id);

-- Paso 8: Verificar la migración
SELECT 
    id, 
    email, 
    role,
    is_active,
    created_at
FROM users 
ORDER BY created_at;

-- Mensaje de confirmación
SELECT 'Migration UUID completed successfully' as status;