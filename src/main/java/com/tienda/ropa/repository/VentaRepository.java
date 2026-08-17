package com.tienda.ropa.repository;

import com.tienda.ropa.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VentaRepository extends JpaRepository<Venta, Long> {
    Optional<Venta> findByNumeroVenta(String numeroVenta);
    boolean existsByNumeroVenta(String numeroVenta);
    List<Venta> findByFechaHoraBetween(LocalDateTime desde, LocalDateTime hasta);
    List<Venta> findByClienteId(Long clienteId);
}
