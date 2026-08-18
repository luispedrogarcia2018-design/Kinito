-- ==========================================================
--  03_indexes.sql
--  Indices estrategicos (ademas de los que ya crean PK/UNIQUE/FK)
--  MySQL 8.0+
-- ==========================================================
USE tienda_ropa_db;

-- productos: busquedas por nombre (catalogo publico) y por categoria
-- (sku y precio_venta ya quedan cubiertos por su UNIQUE / no lo necesitan
-- como filtro de rango frecuente, pero se agrega igual para reportes de precio)
CREATE INDEX idx_productos_nombre ON productos (nombre);
CREATE INDEX idx_productos_categoria ON productos (categoria_id);
CREATE INDEX idx_productos_estado ON productos (estado);

-- variantes_producto: listar todas las variantes de un producto
CREATE INDEX idx_variantes_producto ON variantes_producto (producto_id);
CREATE INDEX idx_variantes_talla ON variantes_producto (talla_id);
CREATE INDEX idx_variantes_color ON variantes_producto (color_id);

-- ventas: reportes por fecha y por cliente son las consultas mas frecuentes
CREATE INDEX idx_ventas_fecha ON ventas (fecha_hora);
CREATE INDEX idx_ventas_cliente ON ventas (cliente_id);
CREATE INDEX idx_ventas_usuario ON ventas (usuario_id);

-- detalle_venta: "productos mas vendidos" agrupa por variante
CREATE INDEX idx_detalle_venta_variante ON detalle_venta (variante_id);

-- entradas_mercaderia: reportes de compras por fecha y por proveedor
CREATE INDEX idx_entradas_fecha ON entradas_mercaderia (fecha);
CREATE INDEX idx_entradas_proveedor ON entradas_mercaderia (proveedor_id);

-- detalle_entrada: "que se ha comprado a este proveedor" agrupa por variante
CREATE INDEX idx_detalle_entrada_variante ON detalle_entrada (variante_id);

-- movimientos_inventario: el Kardex se consulta casi siempre por variante,
-- por rango de fecha, o filtrando por tipo de movimiento
CREATE INDEX idx_movimientos_variante ON movimientos_inventario (variante_id);
CREATE INDEX idx_movimientos_fecha ON movimientos_inventario (fecha_hora);
CREATE INDEX idx_movimientos_tipo ON movimientos_inventario (tipo_movimiento);
-- indice compuesto para el caso mas comun: "kardex de esta variante ordenado por fecha"
CREATE INDEX idx_movimientos_variante_fecha ON movimientos_inventario (variante_id, fecha_hora);

-- inventario: filtrar rapido productos con stock bajo/agotado (stock_actual
-- ya es parte de comparaciones frecuentes; se indexa junto con el minimo)
CREATE INDEX idx_inventario_stock_actual ON inventario (stock_actual);

-- No se crean indices sobre columnas de baja cardinalidad y poco filtradas
-- por si solas (por ejemplo, "estado" en tablas pequenas como roles o
-- metodos_pago), para evitar indices que MySQL raramente usaria y que
-- solo agregan costo de escritura.
