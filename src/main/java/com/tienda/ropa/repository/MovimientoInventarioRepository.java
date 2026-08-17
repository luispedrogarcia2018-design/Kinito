package com.tienda.ropa.repository;

import com.tienda.ropa.entity.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    @Query("""
            SELECT m FROM MovimientoInventario m
            JOIN FETCH m.variante v
            JOIN FETCH v.producto
            JOIN FETCH v.talla
            JOIN FETCH v.color
            JOIN FETCH m.usuario
            WHERE v.id = :varianteId
            ORDER BY m.fechaHora DESC
            """)
    List<MovimientoInventario> findByVarianteIdOrderByFechaHoraDesc(@Param("varianteId") Long varianteId);
}
