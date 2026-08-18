-- ==========================================================
--  01_database.sql
--  Creacion de la base de datos - Tienda de Ropa
--  MySQL 8.0+
-- ==========================================================

CREATE DATABASE IF NOT EXISTS tienda_ropa_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE tienda_ropa_db;

-- A partir de aqui, todos los scripts (02_tables.sql en adelante)
-- asumen que ya se ejecuto este archivo y que el esquema activo
-- es tienda_ropa_db.
