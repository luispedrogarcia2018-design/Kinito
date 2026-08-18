-- ==========================================================
--  05_seed_data.sql
--  Datos iniciales para poder probar el sistema de inmediato
--  Moneda: Quetzales (Q)
--  MySQL 8.0+
-- ==========================================================
USE tienda_ropa_db;

-- ---------------------------------------------------------
-- ROLES
-- ---------------------------------------------------------
INSERT INTO roles (nombre) VALUES ('ADMIN'), ('VENDEDOR');

-- ---------------------------------------------------------
-- USUARIOS
-- La columna password almacena un hash BCrypt real (no texto plano),
-- que es el hash de ejemplo oficial usado en la documentacion de
-- Spring Security para la contrasena en texto plano "password".
-- Cambia estas cuentas antes de usar el sistema en produccion.
-- ---------------------------------------------------------
INSERT INTO usuarios (nombre, username, correo, password, rol_id, estado)
VALUES
('Administrador General', 'admin', 'admin@tiendaropa.com',
 '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW',
 (SELECT id FROM roles WHERE nombre = 'ADMIN'), 'ACTIVO'),
('Vendedor Mostrador', 'vendedor1', 'vendedor1@tiendaropa.com',
 '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW',
 (SELECT id FROM roles WHERE nombre = 'VENDEDOR'), 'ACTIVO');

-- ---------------------------------------------------------
-- CATEGORIAS
-- ---------------------------------------------------------
INSERT INTO categorias (nombre, descripcion) VALUES
('Shorts', 'Shorts casuales de uso diario'),
('Pantalonetas', 'Pantalonetas deportivas y de playa'),
('Trajes de baño', 'Trajes de baño para hombre y mujer'),
('Camisas de playa', 'Camisas ligeras y estampadas de playa'),
('Accesorios', 'Gorras, lentes, bolsos y otros accesorios de playa');

-- ---------------------------------------------------------
-- TALLAS
-- ---------------------------------------------------------
INSERT INTO tallas (nombre, orden) VALUES
('XS', 1), ('S', 2), ('M', 3), ('L', 4), ('XL', 5), ('XXL', 6);

-- ---------------------------------------------------------
-- COLORES
-- ---------------------------------------------------------
INSERT INTO colores (nombre, codigo_hexadecimal) VALUES
('Negro', '#000000'),
('Blanco', '#FFFFFF'),
('Azul', '#1E3A8A'),
('Rojo', '#B91C1C'),
('Verde', '#166534'),
('Beige', '#D9C7A3');

-- ---------------------------------------------------------
-- METODOS DE PAGO
-- ---------------------------------------------------------
INSERT INTO metodos_pago (nombre) VALUES
('Efectivo'), ('Tarjeta'), ('Transferencia');

-- ---------------------------------------------------------
-- PROVEEDORES
-- ---------------------------------------------------------
INSERT INTO proveedores (nombre, empresa, nit, telefono, correo, direccion) VALUES
('Carlos Méndez', 'Distribuidora XYZ', '1234567-8', '5555-1111', 'ventas@distribuidoraxyz.com', 'Zona 4, Ciudad de Guatemala'),
('Ana López', 'Textiles del Pacífico', '2345678-9', '5555-2222', 'contacto@textilespacifico.com', 'Escuintla, Guatemala'),
('Roberto Díaz', 'Playa Import GT', '3456789-0', '5555-3333', 'info@playaimportgt.com', 'Zona 10, Ciudad de Guatemala');

-- ---------------------------------------------------------
-- CLIENTES (incluye Consumidor Final)
-- ---------------------------------------------------------
INSERT INTO clientes (nombre, apellido, nit, telefono, correo, direccion) VALUES
('Consumidor', 'Final', 'CF', NULL, NULL, NULL),
('María', 'Pérez', '4455667-1', '5555-4001', 'maria.perez@correo.com', 'Zona 1, Guatemala'),
('Luis', 'Ramírez', '4455667-2', '5555-4002', 'luis.ramirez@correo.com', 'Mixco, Guatemala'),
('Sofía', 'Castillo', '4455667-3', '5555-4003', 'sofia.castillo@correo.com', 'Antigua Guatemala'),
('Diego', 'Morales', '4455667-4', '5555-4004', 'diego.morales@correo.com', 'Villa Nueva, Guatemala'),
('Andrea', 'Gómez', '4455667-5', '5555-4005', 'andrea.gomez@correo.com', 'Zona 15, Guatemala');

-- ---------------------------------------------------------
-- PRODUCTOS (10)
-- ---------------------------------------------------------
INSERT INTO productos (sku, nombre, descripcion, categoria_id, marca, precio_compra, precio_venta, precio_oferta) VALUES
('SHT-001', 'Short Playero Tropical',      'Short liviano de secado rápido, estampado tropical', (SELECT id FROM categorias WHERE nombre='Shorts'), 'SunWave', 50.00, 120.00, 99.00),
('SHT-002', 'Short Deportivo Básico',      'Short de tela elástica para uso diario',              (SELECT id FROM categorias WHERE nombre='Shorts'), 'SunWave', 40.00, 95.00,  NULL),
('PTL-001', 'Pantaloneta Deportiva Pro',   'Pantaloneta con bolsillos laterales y cordón ajustable',(SELECT id FROM categorias WHERE nombre='Pantalonetas'), 'ActivePlay', 45.00, 110.00, NULL),
('PTL-002', 'Pantaloneta Running Ligera',  'Tela transpirable ideal para correr',                 (SELECT id FROM categorias WHERE nombre='Pantalonetas'), 'ActivePlay', 42.00, 105.00, 89.00),
('TDB-001', 'Traje de Baño Hombre Clásico','Traje de baño ajustado, secado rápido',               (SELECT id FROM categorias WHERE nombre='Trajes de baño'), 'AquaFit', 55.00, 135.00, NULL),
('TDB-002', 'Traje de Baño Mujer Bikini',  'Bikini dos piezas, tela con protección UV',           (SELECT id FROM categorias WHERE nombre='Trajes de baño'), 'AquaFit', 60.00, 150.00, 129.00),
('CAM-001', 'Camisa de Playa Hawaiana',    'Camisa manga corta estampada, tela fresca',           (SELECT id FROM categorias WHERE nombre='Camisas de playa'), 'IslaWear', 48.00, 115.00, NULL),
('CAM-002', 'Camisa Lino Playera',         'Camisa de lino, corte relajado',                      (SELECT id FROM categorias WHERE nombre='Camisas de playa'), 'IslaWear', 65.00, 160.00, NULL),
('ACC-001', 'Gorra Playera Ajustable',     'Gorra con visera curva y ajuste trasero',             (SELECT id FROM categorias WHERE nombre='Accesorios'), 'SunWave', 20.00, 55.00,  45.00),
('ACC-002', 'Lentes de Sol Polarizados',   'Lentes con protección UV400',                         (SELECT id FROM categorias WHERE nombre='Accesorios'), 'SunWave', 35.00, 89.00,  NULL);

-- ---------------------------------------------------------
-- VARIANTES + INVENTARIO INICIAL
-- Se generan variantes Negro/Azul en tallas S, M, L para los
-- productos de ropa (SHT, PTL, TDB, CAM). Accesorios (ACC) llevan
-- una sola variante "talla unica" (se reutiliza la talla M) en dos colores.
-- ---------------------------------------------------------

-- Short Playero Tropical (SHT-001)
INSERT INTO variantes_producto (producto_id, talla_id, color_id, sku_variante) VALUES
((SELECT id FROM productos WHERE sku='SHT-001'), (SELECT id FROM tallas WHERE nombre='S'), (SELECT id FROM colores WHERE nombre='Negro'), 'SHT-001-NEG-S'),
((SELECT id FROM productos WHERE sku='SHT-001'), (SELECT id FROM tallas WHERE nombre='M'), (SELECT id FROM colores WHERE nombre='Negro'), 'SHT-001-NEG-M'),
((SELECT id FROM productos WHERE sku='SHT-001'), (SELECT id FROM tallas WHERE nombre='L'), (SELECT id FROM colores WHERE nombre='Negro'), 'SHT-001-NEG-L'),
((SELECT id FROM productos WHERE sku='SHT-001'), (SELECT id FROM tallas WHERE nombre='S'), (SELECT id FROM colores WHERE nombre='Azul'),  'SHT-001-AZU-S'),
((SELECT id FROM productos WHERE sku='SHT-001'), (SELECT id FROM tallas WHERE nombre='M'), (SELECT id FROM colores WHERE nombre='Azul'),  'SHT-001-AZU-M'),
((SELECT id FROM productos WHERE sku='SHT-001'), (SELECT id FROM tallas WHERE nombre='L'), (SELECT id FROM colores WHERE nombre='Azul'),  'SHT-001-AZU-L');

-- Short Deportivo Básico (SHT-002)
INSERT INTO variantes_producto (producto_id, talla_id, color_id, sku_variante) VALUES
((SELECT id FROM productos WHERE sku='SHT-002'), (SELECT id FROM tallas WHERE nombre='S'), (SELECT id FROM colores WHERE nombre='Negro'), 'SHT-002-NEG-S'),
((SELECT id FROM productos WHERE sku='SHT-002'), (SELECT id FROM tallas WHERE nombre='M'), (SELECT id FROM colores WHERE nombre='Negro'), 'SHT-002-NEG-M'),
((SELECT id FROM productos WHERE sku='SHT-002'), (SELECT id FROM tallas WHERE nombre='L'), (SELECT id FROM colores WHERE nombre='Blanco'),'SHT-002-BLA-L');

-- Pantaloneta Deportiva Pro (PTL-001)
INSERT INTO variantes_producto (producto_id, talla_id, color_id, sku_variante) VALUES
((SELECT id FROM productos WHERE sku='PTL-001'), (SELECT id FROM tallas WHERE nombre='M'), (SELECT id FROM colores WHERE nombre='Rojo'),  'PTL-001-ROJ-M'),
((SELECT id FROM productos WHERE sku='PTL-001'), (SELECT id FROM tallas WHERE nombre='L'), (SELECT id FROM colores WHERE nombre='Rojo'),  'PTL-001-ROJ-L'),
((SELECT id FROM productos WHERE sku='PTL-001'), (SELECT id FROM tallas WHERE nombre='M'), (SELECT id FROM colores WHERE nombre='Verde'), 'PTL-001-VER-M');

-- Pantaloneta Running Ligera (PTL-002)
INSERT INTO variantes_producto (producto_id, talla_id, color_id, sku_variante) VALUES
((SELECT id FROM productos WHERE sku='PTL-002'), (SELECT id FROM tallas WHERE nombre='S'), (SELECT id FROM colores WHERE nombre='Negro'), 'PTL-002-NEG-S'),
((SELECT id FROM productos WHERE sku='PTL-002'), (SELECT id FROM tallas WHERE nombre='M'), (SELECT id FROM colores WHERE nombre='Negro'), 'PTL-002-NEG-M');

-- Traje de Baño Hombre Clásico (TDB-001)
INSERT INTO variantes_producto (producto_id, talla_id, color_id, sku_variante) VALUES
((SELECT id FROM productos WHERE sku='TDB-001'), (SELECT id FROM tallas WHERE nombre='M'), (SELECT id FROM colores WHERE nombre='Azul'),  'TDB-001-AZU-M'),
((SELECT id FROM productos WHERE sku='TDB-001'), (SELECT id FROM tallas WHERE nombre='L'), (SELECT id FROM colores WHERE nombre='Azul'),  'TDB-001-AZU-L'),
((SELECT id FROM productos WHERE sku='TDB-001'), (SELECT id FROM tallas WHERE nombre='M'), (SELECT id FROM colores WHERE nombre='Negro'), 'TDB-001-NEG-M');

-- Traje de Baño Mujer Bikini (TDB-002)
INSERT INTO variantes_producto (producto_id, talla_id, color_id, sku_variante) VALUES
((SELECT id FROM productos WHERE sku='TDB-002'), (SELECT id FROM tallas WHERE nombre='S'), (SELECT id FROM colores WHERE nombre='Beige'), 'TDB-002-BEI-S'),
((SELECT id FROM productos WHERE sku='TDB-002'), (SELECT id FROM tallas WHERE nombre='M'), (SELECT id FROM colores WHERE nombre='Beige'), 'TDB-002-BEI-M'),
((SELECT id FROM productos WHERE sku='TDB-002'), (SELECT id FROM tallas WHERE nombre='S'), (SELECT id FROM colores WHERE nombre='Rojo'),  'TDB-002-ROJ-S');

-- Camisa de Playa Hawaiana (CAM-001)
INSERT INTO variantes_producto (producto_id, talla_id, color_id, sku_variante) VALUES
((SELECT id FROM productos WHERE sku='CAM-001'), (SELECT id FROM tallas WHERE nombre='M'), (SELECT id FROM colores WHERE nombre='Blanco'),'CAM-001-BLA-M'),
((SELECT id FROM productos WHERE sku='CAM-001'), (SELECT id FROM tallas WHERE nombre='L'), (SELECT id FROM colores WHERE nombre='Blanco'),'CAM-001-BLA-L'),
((SELECT id FROM productos WHERE sku='CAM-001'), (SELECT id FROM tallas WHERE nombre='XL'),(SELECT id FROM colores WHERE nombre='Verde'), 'CAM-001-VER-XL');

-- Camisa Lino Playera (CAM-002)
INSERT INTO variantes_producto (producto_id, talla_id, color_id, sku_variante) VALUES
((SELECT id FROM productos WHERE sku='CAM-002'), (SELECT id FROM tallas WHERE nombre='M'), (SELECT id FROM colores WHERE nombre='Beige'), 'CAM-002-BEI-M'),
((SELECT id FROM productos WHERE sku='CAM-002'), (SELECT id FROM tallas WHERE nombre='L'), (SELECT id FROM colores WHERE nombre='Beige'), 'CAM-002-BEI-L');

-- Gorra Playera Ajustable (ACC-001) - talla unica (se usa 'M' como talla generica)
INSERT INTO variantes_producto (producto_id, talla_id, color_id, sku_variante) VALUES
((SELECT id FROM productos WHERE sku='ACC-001'), (SELECT id FROM tallas WHERE nombre='M'), (SELECT id FROM colores WHERE nombre='Negro'), 'ACC-001-NEG-U'),
((SELECT id FROM productos WHERE sku='ACC-001'), (SELECT id FROM tallas WHERE nombre='M'), (SELECT id FROM colores WHERE nombre='Beige'), 'ACC-001-BEI-U');

-- Lentes de Sol Polarizados (ACC-002) - talla unica
INSERT INTO variantes_producto (producto_id, talla_id, color_id, sku_variante) VALUES
((SELECT id FROM productos WHERE sku='ACC-002'), (SELECT id FROM tallas WHERE nombre='M'), (SELECT id FROM colores WHERE nombre='Negro'), 'ACC-002-NEG-U');

-- Inventario inicial: una fila por cada variante creada arriba
INSERT INTO inventario (variante_id, stock_actual, stock_minimo)
SELECT id,
       FLOOR(5 + (RAND() * 20)),  -- stock inicial simulado entre 5 y 25 unidades
       5
FROM variantes_producto;

-- ---------------------------------------------------------
-- ENTRADA DE MERCADERIA DE EJEMPLO
-- ---------------------------------------------------------
INSERT INTO entradas_mercaderia (numero_entrada, proveedor_id, usuario_id, observaciones, total)
VALUES ('ENT-00001',
        (SELECT id FROM proveedores WHERE empresa = 'Distribuidora XYZ'),
        (SELECT id FROM usuarios WHERE username = 'admin'),
        'Reposición inicial de shorts tropicales',
        1250.00);

INSERT INTO detalle_entrada (entrada_id, variante_id, cantidad, costo_unitario, subtotal)
VALUES (
    (SELECT id FROM entradas_mercaderia WHERE numero_entrada = 'ENT-00001'),
    (SELECT id FROM variantes_producto WHERE sku_variante = 'SHT-001-NEG-M'),
    25, 50.00, 1250.00
);

-- Refleja la entrada en el stock y en el Kardex (tal como lo haria la app)
UPDATE inventario
SET stock_actual = stock_actual + 25
WHERE variante_id = (SELECT id FROM variantes_producto WHERE sku_variante = 'SHT-001-NEG-M');

INSERT INTO movimientos_inventario
    (variante_id, tipo_movimiento, cantidad, stock_anterior, stock_posterior, referencia_tipo, referencia_id, usuario_id, observaciones)
SELECT
    (SELECT id FROM variantes_producto WHERE sku_variante = 'SHT-001-NEG-M'),
    'ENTRADA', 25,
    inv.stock_actual - 25, inv.stock_actual,
    'ENTRADA_MERCADERIA',
    (SELECT id FROM entradas_mercaderia WHERE numero_entrada = 'ENT-00001'),
    (SELECT id FROM usuarios WHERE username = 'admin'),
    'Ingreso inicial de mercadería'
FROM inventario inv
WHERE inv.variante_id = (SELECT id FROM variantes_producto WHERE sku_variante = 'SHT-001-NEG-M');

-- ---------------------------------------------------------
-- VENTA DE EJEMPLO
-- ---------------------------------------------------------
INSERT INTO ventas (numero_venta, cliente_id, usuario_id, metodo_pago_id, subtotal, descuento, total)
VALUES (
    'VEN-00001',
    (SELECT id FROM clientes WHERE nit = 'CF'),
    (SELECT id FROM usuarios WHERE username = 'vendedor1'),
    (SELECT id FROM metodos_pago WHERE nombre = 'Efectivo'),
    198.00, 0.00, 198.00
);

INSERT INTO detalle_venta (venta_id, variante_id, cantidad, precio_unitario, subtotal)
VALUES (
    (SELECT id FROM ventas WHERE numero_venta = 'VEN-00001'),
    (SELECT id FROM variantes_producto WHERE sku_variante = 'SHT-001-NEG-M'),
    2, 99.00, 198.00
);

-- Descuenta el stock vendido y registra el movimiento en el Kardex
UPDATE inventario
SET stock_actual = stock_actual - 2
WHERE variante_id = (SELECT id FROM variantes_producto WHERE sku_variante = 'SHT-001-NEG-M');

INSERT INTO movimientos_inventario
    (variante_id, tipo_movimiento, cantidad, stock_anterior, stock_posterior, referencia_tipo, referencia_id, usuario_id, observaciones)
SELECT
    (SELECT id FROM variantes_producto WHERE sku_variante = 'SHT-001-NEG-M'),
    'VENTA', 2,
    inv.stock_actual + 2, inv.stock_actual,
    'VENTA',
    (SELECT id FROM ventas WHERE numero_venta = 'VEN-00001'),
    (SELECT id FROM usuarios WHERE username = 'vendedor1'),
    'Venta de mostrador'
FROM inventario inv
WHERE inv.variante_id = (SELECT id FROM variantes_producto WHERE sku_variante = 'SHT-001-NEG-M');
