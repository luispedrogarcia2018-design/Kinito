-- ==========================================================
--  04_views.sql
--  Vistas de apoyo para reportes frecuentes
--  MySQL 8.0+
-- ==========================================================
USE tienda_ropa_db;

-- ---------------------------------------------------------
-- vw_inventario_actual
-- Foto completa del stock por variante: producto, SKU, talla,
-- color, stock y estado calculado (DISPONIBLE / STOCK_BAJO / AGOTADO).
-- ---------------------------------------------------------
CREATE OR REPLACE VIEW vw_inventario_actual AS
SELECT
    vp.id                   AS variante_id,
    p.id                    AS producto_id,
    p.sku                   AS producto_sku,
    p.nombre                AS producto_nombre,
    c.nombre                AS categoria,
    t.nombre                AS talla,
    col.nombre               AS color,
    vp.sku_variante,
    inv.stock_actual,
    inv.stock_minimo,
    CASE
        WHEN inv.stock_actual = 0 THEN 'AGOTADO'
        WHEN inv.stock_actual <= inv.stock_minimo THEN 'STOCK_BAJO'
        ELSE 'DISPONIBLE'
    END AS estado_stock
FROM variantes_producto vp
JOIN productos p   ON p.id = vp.producto_id
JOIN categorias c  ON c.id = p.categoria_id
JOIN tallas t      ON t.id = vp.talla_id
JOIN colores col   ON col.id = vp.color_id
JOIN inventario inv ON inv.variante_id = vp.id
WHERE vp.estado = 'ACTIVO' AND p.estado = 'ACTIVO';

-- ---------------------------------------------------------
-- vw_productos_stock_bajo
-- Subconjunto de vw_inventario_actual con solo STOCK_BAJO o AGOTADO,
-- listo para alertas visuales en el dashboard.
-- ---------------------------------------------------------
CREATE OR REPLACE VIEW vw_productos_stock_bajo AS
SELECT *
FROM vw_inventario_actual
WHERE estado_stock IN ('STOCK_BAJO', 'AGOTADO');

-- ---------------------------------------------------------
-- vw_resumen_ventas
-- Una fila por venta completada, con totales, para reportes
-- diarios/semanales/mensuales por rango de fecha.
-- ---------------------------------------------------------
CREATE OR REPLACE VIEW vw_resumen_ventas AS
SELECT
    v.id             AS venta_id,
    v.numero_venta,
    v.fecha_hora,
    DATE(v.fecha_hora) AS fecha,
    cl.id            AS cliente_id,
    CONCAT(cl.nombre, ' ', COALESCE(cl.apellido, '')) AS cliente,
    u.nombre         AS vendedor,
    mp.nombre        AS metodo_pago,
    v.subtotal,
    v.descuento,
    v.total,
    v.estado
FROM ventas v
JOIN clientes cl      ON cl.id = v.cliente_id
JOIN usuarios u        ON u.id = v.usuario_id
JOIN metodos_pago mp   ON mp.id = v.metodo_pago_id
WHERE v.estado = 'COMPLETADA';

-- ---------------------------------------------------------
-- vw_productos_mas_vendidos
-- Unidades y monto total vendido por producto (agrupando todas
-- sus variantes), solo considerando ventas completadas.
-- ---------------------------------------------------------
CREATE OR REPLACE VIEW vw_productos_mas_vendidos AS
SELECT
    p.id                AS producto_id,
    p.sku,
    p.nombre            AS producto,
    c.nombre            AS categoria,
    SUM(dv.cantidad)    AS unidades_vendidas,
    SUM(dv.subtotal)    AS total_vendido
FROM detalle_venta dv
JOIN ventas v        ON v.id = dv.venta_id AND v.estado = 'COMPLETADA'
JOIN variantes_producto vp ON vp.id = dv.variante_id
JOIN productos p     ON p.id = vp.producto_id
JOIN categorias c    ON c.id = p.categoria_id
GROUP BY p.id, p.sku, p.nombre, c.nombre;
