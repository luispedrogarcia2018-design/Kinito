-- ==========================================================
--  02_tables.sql
--  Tienda de Ropa - Definicion de tablas
--  MySQL 8.0+  |  ENGINE=InnoDB  |  utf8mb4
-- ==========================================================
USE tienda_ropa_db;

SET FOREIGN_KEY_CHECKS = 0;

-- ---------------------------------------------------------
-- ROLES Y USUARIOS
-- ---------------------------------------------------------
DROP TABLE IF EXISTS roles;
CREATE TABLE roles (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(30) NOT NULL,
    CONSTRAINT uq_roles_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS usuarios;
CREATE TABLE usuarios (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(150) NOT NULL,
    username        VARCHAR(50)  NOT NULL,
    correo          VARCHAR(150),
    -- Hash BCrypt generado por Spring Security (60 caracteres). Nunca texto plano.
    password        VARCHAR(255) NOT NULL,
    rol_id          BIGINT NOT NULL,
    estado          ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_usuarios_username UNIQUE (username),
    CONSTRAINT uq_usuarios_correo UNIQUE (correo),
    CONSTRAINT fk_usuarios_rol FOREIGN KEY (rol_id) REFERENCES roles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------
-- CATALOGO: CATEGORIAS, TALLAS, COLORES
-- ---------------------------------------------------------
DROP TABLE IF EXISTS categorias;
CREATE TABLE categorias (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(80) NOT NULL,
    descripcion     VARCHAR(255),
    estado          ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_categorias_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS tallas;
CREATE TABLE tallas (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre  VARCHAR(10) NOT NULL,   -- XS, S, M, L, XL, XXL, ampliable
    orden   INT NOT NULL DEFAULT 0, -- para ordenar XS < S < M < L < XL < XXL en la UI
    CONSTRAINT uq_tallas_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS colores;
CREATE TABLE colores (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre              VARCHAR(40) NOT NULL,
    codigo_hexadecimal  VARCHAR(7),          -- ej. #000000
    estado              ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT uq_colores_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------
-- PRODUCTOS E IMAGENES
-- ---------------------------------------------------------
DROP TABLE IF EXISTS productos;
CREATE TABLE productos (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku                 VARCHAR(40) NOT NULL,
    nombre              VARCHAR(150) NOT NULL,
    descripcion         TEXT,
    categoria_id        BIGINT NOT NULL,
    marca               VARCHAR(80),
    precio_compra       DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    precio_venta        DECIMAL(10,2) NOT NULL,
    precio_oferta       DECIMAL(10,2) NULL,
    estado              ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion  DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    creado_por          BIGINT NULL,
    modificado_por      BIGINT NULL,
    CONSTRAINT uq_productos_sku UNIQUE (sku),
    CONSTRAINT fk_productos_categoria FOREIGN KEY (categoria_id) REFERENCES categorias(id),
    CONSTRAINT fk_productos_creado_por FOREIGN KEY (creado_por) REFERENCES usuarios(id),
    CONSTRAINT fk_productos_modificado_por FOREIGN KEY (modificado_por) REFERENCES usuarios(id),
    CONSTRAINT chk_productos_precio_venta CHECK (precio_venta > 0),
    CONSTRAINT chk_productos_precio_compra CHECK (precio_compra >= 0),
    CONSTRAINT chk_productos_precio_oferta CHECK (precio_oferta IS NULL OR precio_oferta >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS imagenes_producto;
CREATE TABLE imagenes_producto (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    producto_id         BIGINT NOT NULL,
    ruta_imagen         VARCHAR(255) NOT NULL,   -- ruta o URL, nunca BLOB
    imagen_principal    BOOLEAN NOT NULL DEFAULT FALSE,
    orden               INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_imagenes_producto FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------
-- VARIANTES (producto + talla + color) E INVENTARIO
-- ---------------------------------------------------------
DROP TABLE IF EXISTS variantes_producto;
CREATE TABLE variantes_producto (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    producto_id     BIGINT NOT NULL,
    talla_id        BIGINT NOT NULL,
    color_id        BIGINT NOT NULL,
    sku_variante    VARCHAR(60) NOT NULL,   -- SKU secundario, ej. SHORT-TROP-NEG-M
    estado          ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT uq_variante_sku UNIQUE (sku_variante),
    CONSTRAINT uq_variante_combinacion UNIQUE (producto_id, talla_id, color_id),
    CONSTRAINT fk_variante_producto FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE CASCADE,
    CONSTRAINT fk_variante_talla FOREIGN KEY (talla_id) REFERENCES tallas(id),
    CONSTRAINT fk_variante_color FOREIGN KEY (color_id) REFERENCES colores(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS inventario;
CREATE TABLE inventario (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    variante_id         BIGINT NOT NULL,
    stock_actual        INT NOT NULL DEFAULT 0,
    stock_minimo        INT NOT NULL DEFAULT 5,
    fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    -- version: usada por Hibernate (@Version) para bloqueo optimista y
    -- evitar sobreventa cuando dos ventas concurrentes tocan la misma fila.
    version             BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_inventario_variante UNIQUE (variante_id),
    CONSTRAINT fk_inventario_variante FOREIGN KEY (variante_id) REFERENCES variantes_producto(id) ON DELETE CASCADE,
    CONSTRAINT chk_inventario_stock_actual CHECK (stock_actual >= 0),
    CONSTRAINT chk_inventario_stock_minimo CHECK (stock_minimo >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------
-- PROVEEDORES Y ENTRADAS DE MERCADERIA
-- ---------------------------------------------------------
DROP TABLE IF EXISTS proveedores;
CREATE TABLE proveedores (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(120) NOT NULL,
    empresa         VARCHAR(150),
    nit             VARCHAR(20),
    telefono        VARCHAR(30),
    correo          VARCHAR(150),
    direccion       VARCHAR(255),
    estado          ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    observaciones   VARCHAR(255),
    fecha_creacion  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS entradas_mercaderia;
CREATE TABLE entradas_mercaderia (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_entrada  VARCHAR(30) NOT NULL,
    proveedor_id    BIGINT NOT NULL,
    usuario_id      BIGINT NOT NULL,
    fecha           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total           DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    observaciones   VARCHAR(255),
    estado          ENUM('ACTIVA','ANULADA') NOT NULL DEFAULT 'ACTIVA',
    CONSTRAINT uq_entrada_numero UNIQUE (numero_entrada),
    CONSTRAINT fk_entrada_proveedor FOREIGN KEY (proveedor_id) REFERENCES proveedores(id),
    CONSTRAINT fk_entrada_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CONSTRAINT chk_entrada_total CHECK (total >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS detalle_entrada;
CREATE TABLE detalle_entrada (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    entrada_id      BIGINT NOT NULL,
    variante_id     BIGINT NOT NULL,
    cantidad        INT NOT NULL,
    costo_unitario  DECIMAL(10,2) NOT NULL,
    subtotal        DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_detalle_entrada_cabecera FOREIGN KEY (entrada_id) REFERENCES entradas_mercaderia(id) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_entrada_variante FOREIGN KEY (variante_id) REFERENCES variantes_producto(id),
    CONSTRAINT chk_detalle_entrada_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_detalle_entrada_costo CHECK (costo_unitario >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------
-- CLIENTES
-- ---------------------------------------------------------
DROP TABLE IF EXISTS clientes;
CREATE TABLE clientes (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    apellido        VARCHAR(100),
    nit             VARCHAR(20) NOT NULL DEFAULT 'CF',  -- CF = Consumidor Final
    telefono        VARCHAR(30),
    correo          VARCHAR(150),
    direccion       VARCHAR(255),
    fecha_registro  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado          ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------
-- METODOS DE PAGO Y VENTAS
-- ---------------------------------------------------------
DROP TABLE IF EXISTS metodos_pago;
CREATE TABLE metodos_pago (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre  VARCHAR(40) NOT NULL,   -- Efectivo, Tarjeta, Transferencia...
    estado  ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT uq_metodo_pago_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS ventas;
CREATE TABLE ventas (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_venta    VARCHAR(30) NOT NULL,
    fecha_hora      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cliente_id      BIGINT NOT NULL,
    usuario_id      BIGINT NOT NULL,
    metodo_pago_id  BIGINT NOT NULL,
    subtotal        DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    descuento       DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total           DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    estado          ENUM('COMPLETADA','ANULADA') NOT NULL DEFAULT 'COMPLETADA',
    CONSTRAINT uq_venta_numero UNIQUE (numero_venta),
    CONSTRAINT fk_venta_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT fk_venta_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CONSTRAINT fk_venta_metodo_pago FOREIGN KEY (metodo_pago_id) REFERENCES metodos_pago(id),
    CONSTRAINT chk_venta_subtotal CHECK (subtotal >= 0),
    CONSTRAINT chk_venta_descuento CHECK (descuento >= 0),
    CONSTRAINT chk_venta_total CHECK (total >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS detalle_venta;
CREATE TABLE detalle_venta (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    venta_id          BIGINT NOT NULL,
    variante_id       BIGINT NOT NULL,
    cantidad          INT NOT NULL,
    precio_unitario   DECIMAL(10,2) NOT NULL,
    subtotal          DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_detalle_venta_cabecera FOREIGN KEY (venta_id) REFERENCES ventas(id) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_venta_variante FOREIGN KEY (variante_id) REFERENCES variantes_producto(id),
    CONSTRAINT chk_detalle_venta_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_detalle_venta_precio CHECK (precio_unitario >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------
-- KARDEX / MOVIMIENTOS DE INVENTARIO (append-only, nunca se edita)
-- ---------------------------------------------------------
DROP TABLE IF EXISTS movimientos_inventario;
CREATE TABLE movimientos_inventario (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    variante_id             BIGINT NOT NULL,
    fecha_hora              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo_movimiento         ENUM('ENTRADA','VENTA','AJUSTE_POSITIVO','AJUSTE_NEGATIVO','DEVOLUCION') NOT NULL,
    cantidad                INT NOT NULL,          -- valor absoluto de unidades movidas
    stock_anterior          INT NOT NULL,
    stock_posterior         INT NOT NULL,
    -- FK "polimorfica" a la tabla origen (ventas / entradas_mercaderia / ajustes).
    -- No es FOREIGN KEY fisica porque la tabla referenciada varia segun tipo_movimiento;
    -- la consistencia la garantiza el Service que crea el movimiento (ver seccion 5 del analisis).
    referencia_tipo         VARCHAR(30),           -- ej. 'VENTA', 'ENTRADA_MERCADERIA', 'AJUSTE_MANUAL'
    referencia_id           BIGINT,
    usuario_id              BIGINT NOT NULL,
    observaciones           VARCHAR(255),
    CONSTRAINT fk_movimiento_variante FOREIGN KEY (variante_id) REFERENCES variantes_producto(id),
    CONSTRAINT fk_movimiento_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CONSTRAINT chk_movimiento_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_movimiento_stock_anterior CHECK (stock_anterior >= 0),
    CONSTRAINT chk_movimiento_stock_posterior CHECK (stock_posterior >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
