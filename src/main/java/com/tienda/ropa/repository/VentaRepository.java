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
}
