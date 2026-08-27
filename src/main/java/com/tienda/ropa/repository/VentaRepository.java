package com.tienda.ropa.repository;

import com.tienda.ropa.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VentaRepository extends JpaRepository<Venta, Long> {
    Optional<Venta> findByNumeroVenta(String numeroVenta);
    boolean existsByNumeroVenta(String numeroVenta);
    List<Venta> findByFechaHoraBetween(LocalDateTime desde, LocalDateTime hasta);
    List<Venta> findByEstado(Venta.EstadoVenta estado);

    // Ventas por rango de fechas, con cliente/usuario/metodo ya cargados, para el reporte.
    @Query("""
            SELECT v FROM Venta v
            JOIN FETCH v.cliente
            JOIN FETCH v.usuario
            JOIN FETCH v.metodoPago
            WHERE v.estado = com.tienda.ropa.entity.Venta.EstadoVenta.COMPLETADA
              AND v.fechaHora BETWEEN :desde AND :hasta
            ORDER BY v.fechaHora DESC
            """)
    List<Venta> findParaReporte(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    // Listado general con cliente/usuario/metodo de pago ya cargados
    // (evita error de lazy loading al mostrarlos en la tabla).
    @Query("""
            SELECT v FROM Venta v
            JOIN FETCH v.cliente
            JOIN FETCH v.usuario
            JOIN FETCH v.metodoPago
            ORDER BY v.fechaHora DESC
            """)
    List<Venta> findAllConDatos();

    // Detalle completo de una venta (para el recibo/comprobante).
    @Query("""
            SELECT v FROM Venta v
            JOIN FETCH v.cliente
            JOIN FETCH v.usuario
            JOIN FETCH v.metodoPago
            LEFT JOIN FETCH v.detalles d
            LEFT JOIN FETCH d.variante var
            LEFT JOIN FETCH var.producto
            LEFT JOIN FETCH var.talla
            LEFT JOIN FETCH var.color
            WHERE v.id = :id
            """)
    Optional<Venta> findByIdConDetalle(@Param("id") Long id);

    // Historial de compras de un cliente especifico, para su pantalla de detalle.
    @Query("""
            SELECT v FROM Venta v
            JOIN FETCH v.usuario
            JOIN FETCH v.metodoPago
            WHERE v.cliente.id = :clienteId
            ORDER BY v.fechaHora DESC
            """)
    List<Venta> findByClienteId(@Param("clienteId") Long clienteId);

    // ---- Consultas para el Dashboard ----

    @Query("""
            SELECT COALESCE(SUM(v.total), 0) FROM Venta v
            WHERE v.estado = com.tienda.ropa.entity.Venta.EstadoVenta.COMPLETADA
              AND v.fechaHora BETWEEN :desde AND :hasta
            """)
    java.math.BigDecimal sumarTotalEntre(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    // Ultimas N ventas para la tabla del dashboard.
    @Query("""
            SELECT v FROM Venta v
            JOIN FETCH v.cliente
            JOIN FETCH v.usuario
            ORDER BY v.fechaHora DESC
            LIMIT 5
            """)
    List<Venta> ultimasCinco();
}
