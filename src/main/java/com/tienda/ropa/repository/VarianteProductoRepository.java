package com.tienda.ropa.repository;

import com.tienda.ropa.entity.VarianteProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VarianteProductoRepository extends JpaRepository<VarianteProducto, Long> {

    List<VarianteProducto> findByProductoId(Long productoId);

    Optional<VarianteProducto> findByProductoIdAndTallaIdAndColorId(Long productoId, Long tallaId, Long colorId);

    boolean existsBySkuVariante(String skuVariante);

    Optional<VarianteProducto> findBySkuVariante(String skuVariante);

    // Trae todas las variantes activas con producto/talla/color ya cargados,
    // para usar en los selectores de Entradas y Ventas sin errores de lazy loading.
    @Query("""
            SELECT v FROM VarianteProducto v
            JOIN FETCH v.producto p
            JOIN FETCH v.talla
            JOIN FETCH v.color
            WHERE v.estado = com.tienda.ropa.entity.EstadoGeneral.ACTIVO
              AND p.estado = com.tienda.ropa.entity.EstadoGeneral.ACTIVO
            ORDER BY p.nombre, v.talla.orden
            """)
    List<VarianteProducto> findTodasActivasConDatos();
}
