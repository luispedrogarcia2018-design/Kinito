-- ==========================================================
--  06_test_queries.sql
--  Consultas de prueba y consultas para reportes
--  MySQL 8.0+
-- ==========================================================
USE tienda_ropa_db;

-- ==========================================================
-- 13. CONSULTAS DE PRUEBA (verificar que el seed cargó bien)
-- ==========================================================

-- Ver el catálogo completo con su categoría
SELECT p.sku, p.nombre, c.nombre AS categoria, p.precio_venta, p.precio_oferta, p.estado
FROM productos p
JOIN categorias c ON c.id = p.categoria_id
ORDER BY c.nombre, p.nombre;

-- Ver todas las variantes de un producto especifico con su stock
SELECT p.nombre, t.nombre AS talla, col.nombre AS color, vp.sku_variante, inv.stock_actual, inv.stock_minimo
FROM variantes_producto vp
JOIN productos p ON p.id = vp.producto_id
JOIN tallas t ON t.id = vp.talla_id
JOIN colores col ON col.id = vp.color_id
JOIN inventario inv ON inv.variante_id = vp.id
WHERE p.sku = 'SHT-001'
ORDER BY col.nombre, t.orden;

-- Verificar que el Kardex de la variante vendida cuadra: 25 (entrada) - 2 (venta)
SELECT mi.fecha_hora, mi.tipo_movimiento, mi.cantidad, mi.stock_anterior, mi.stock_posterior, u.nombre AS usuario
FROM movimientos_inventario mi
JOIN usuarios u ON u.id = mi.usuario_id
WHERE mi.variante_id = (SELECT id FROM variantes_producto WHERE sku_variante = 'SHT-001-NEG-M')
ORDER BY mi.fecha_hora;

-- Detalle completo de una venta
SELECT v.numero_venta, v.fecha_hora, cl.nombre AS cliente, p.nombre AS producto,
       t.nombre AS talla, col.nombre AS color, dv.cantidad, dv.precio_unitario, dv.subtotal, v.total
FROM ventas v
JOIN clientes cl ON cl.id = v.cliente_id
JOIN detalle_venta dv ON dv.venta_id = v.id
JOIN variantes_producto vp ON vp.id = dv.variante_id
JOIN productos p ON p.id = vp.producto_id
JOIN tallas t ON t.id = vp.talla_id
JOIN colores col ON col.id = vp.color_id
WHERE v.numero_venta = 'VEN-00001';

-- ==========================================================
-- 14. CONSULTAS PARA REPORTES
-- ==========================================================

-- ---- Inventario actual (usando la vista) ----
SELECT * FROM vw_inventario_actual ORDER BY producto_nombre, talla;

-- ---- Productos con stock bajo ----
SELECT * FROM vw_productos_stock_bajo;

-- ---- Productos agotados ----
SELECT * FROM vw_inventario_actual WHERE estado_stock = 'AGOTADO';

-- ---- Productos con mayor existencia (top 10) ----
SELECT producto_nombre, talla, color, stock_actual
FROM vw_inventario_actual
ORDER BY stock_actual DESC
LIMIT 10;

-- ---- Ventas diarias (hoy) ----
SELECT COUNT(*) AS num_ventas, COALESCE(SUM(total), 0) AS total_vendido
FROM ventas
WHERE estado = 'COMPLETADA' AND DATE(fecha_hora) = CURDATE();

-- ---- Ventas por rango de fechas ----
SELECT DATE(fecha_hora) AS fecha, COUNT(*) AS num_ventas, SUM(total) AS total_dia
FROM ventas
WHERE estado = 'COMPLETADA'
  AND fecha_hora BETWEEN '2026-01-01 00:00:00' AND '2026-12-31 23:59:59'
GROUP BY DATE(fecha_hora)
ORDER BY fecha;

-- ---- Ventas mensuales (agrupadas por año-mes) ----
SELECT DATE_FORMAT(fecha_hora, '%Y-%m') AS mes, COUNT(*) AS num_ventas, SUM(total) AS total_mes
FROM ventas
WHERE estado = 'COMPLETADA'
GROUP BY DATE_FORMAT(fecha_hora, '%Y-%m')
ORDER BY mes;

-- ---- Productos más vendidos (usando la vista) ----
SELECT * FROM vw_productos_mas_vendidos ORDER BY unidades_vendidas DESC LIMIT 10;

-- ---- Productos menos vendidos (con al menos una venta) ----
SELECT * FROM vw_productos_mas_vendidos ORDER BY unidades_vendidas ASC LIMIT 10;

-- ---- Ventas por categoría ----
SELECT c.nombre AS categoria, SUM(dv.cantidad) AS unidades, SUM(dv.subtotal) AS total_vendido
FROM detalle_venta dv
JOIN ventas v ON v.id = dv.venta_id AND v.estado = 'COMPLETADA'
JOIN variantes_producto vp ON vp.id = dv.variante_id
JOIN productos p ON p.id = vp.producto_id
JOIN categorias c ON c.id = p.categoria_id
GROUP BY c.nombre
ORDER BY total_vendido DESC;

-- ---- Compras por proveedor ----
SELECT pr.empresa, COUNT(DISTINCT em.id) AS num_entradas, SUM(de.subtotal) AS total_comprado
FROM detalle_entrada de
JOIN entradas_mercaderia em ON em.id = de.entrada_id AND em.estado = 'ACTIVA'
JOIN proveedores pr ON pr.id = em.proveedor_id
GROUP BY pr.empresa
ORDER BY total_comprado DESC;

-- ---- Historial Kardex completo de una variante ----
SELECT mi.fecha_hora, mi.tipo_movimiento, mi.cantidad, mi.stock_anterior, mi.stock_posterior,
       mi.referencia_tipo, mi.referencia_id, u.nombre AS usuario, mi.observaciones
FROM movimientos_inventario mi
JOIN usuarios u ON u.id = mi.usuario_id
WHERE mi.variante_id = (SELECT id FROM variantes_producto WHERE sku_variante = 'SHT-001-NEG-M')
ORDER BY mi.fecha_hora;

-- ---- Ganancia bruta estimada (total vendido - costo estimado de mercadería vendida) ----
-- Costo estimado = precio_compra del producto al momento de la consulta (aproximacion,
-- ya que precio_compra puede variar en el tiempo; para exactitud historica se
-- recomendaria guardar el costo_unitario tambien en detalle_venta a futuro).
SELECT
    SUM(dv.subtotal)                              AS total_vendido,
    SUM(dv.cantidad * p.precio_compra)            AS costo_estimado_vendido,
    SUM(dv.subtotal) - SUM(dv.cantidad * p.precio_compra) AS ganancia_bruta_estimada
FROM detalle_venta dv
JOIN ventas v ON v.id = dv.venta_id AND v.estado = 'COMPLETADA'
JOIN variantes_producto vp ON vp.id = dv.variante_id
JOIN productos p ON p.id = vp.producto_id;
