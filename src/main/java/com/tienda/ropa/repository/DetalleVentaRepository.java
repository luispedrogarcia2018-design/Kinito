package com.tienda.ropa.repository;

import com.tienda.ropa.entity.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    // Cada fila: [nombreProducto, unidadesVendidas]. Solo cuenta ventas COMPLETADA.
    @Query("""
            SELECT p.nombre, SUM(d.cantidad)
            FROM DetalleVenta d
            JOIN d.venta ve
            JOIN d.variante v
            JOIN v.producto p
            WHERE ve.estado = com.tienda.ropa.entity.Venta.EstadoVenta.COMPLETADA
            GROUP BY p.nombre
            ORDER BY SUM(d.cantidad) DESC
            """)
    List<Object[]> productosMasVendidos();

    // Cada fila: [nombreCategoria, totalVendido]. Solo cuenta ventas COMPLETADA.
    @Query("""
            SELECT c.nombre, SUM(d.subtotal)
            FROM DetalleVenta d
            JOIN d.venta ve
            JOIN d.variante v
            JOIN v.producto p
            JOIN p.categoria c
            WHERE ve.estado = com.tienda.ropa.entity.Venta.EstadoVenta.COMPLETADA
            GROUP BY c.nombre
            ORDER BY SUM(d.subtotal) DESC
            """)
    List<Object[]> ventasPorCategoria();

    // Igual que productosMasVendidos() pero ordenado ascendente (para "menos vendidos").
    @Query("""
            SELECT p.nombre, SUM(d.cantidad)
            FROM DetalleVenta d
            JOIN d.venta ve
            JOIN d.variante v
            JOIN v.producto p
            WHERE ve.estado = com.tienda.ropa.entity.Venta.EstadoVenta.COMPLETADA
            GROUP BY p.nombre
            ORDER BY SUM(d.cantidad) ASC
            """)
    List<Object[]> productosMenosVendidos();

    // Fila unica: [totalVendido, costoEstimadoDeLoVendido] en el rango de fechas dado.
    @Query("""
            SELECT COALESCE(SUM(d.subtotal), 0), COALESCE(SUM(d.cantidad * p.precioCompra), 0)
            FROM DetalleVenta d
            JOIN d.venta ve
            JOIN d.variante v
            JOIN v.producto p
            WHERE ve.estado = com.tienda.ropa.entity.Venta.EstadoVenta.COMPLETADA
              AND ve.fechaHora BETWEEN :desde AND :hasta
            """)
    List<Object[]> totalesFinancieros(@org.springframework.data.repository.query.Param("desde") java.time.LocalDateTime desde,
                                       @org.springframework.data.repository.query.Param("hasta") java.time.LocalDateTime hasta);
}
