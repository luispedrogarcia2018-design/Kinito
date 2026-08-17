package com.tienda.ropa.repository;

import com.tienda.ropa.entity.EstadoGeneral;
import com.tienda.ropa.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findBySku(String sku);

    boolean existsBySku(String sku);

    /**
     * JOIN FETCH trae la categoria en la misma consulta (evita el error
     * LazyInitializationException al leer p.categoria.nombre en la vista,
     * ya que open-in-view esta desactivado a proposito).
     */
    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria WHERE p.estado = :estado ORDER BY p.nombre")
    List<Producto> findByEstado(@Param("estado") EstadoGeneral estado);

    long countByEstado(EstadoGeneral estado);

    @Query("""
            SELECT DISTINCT p FROM Producto p
            LEFT JOIN p.variantes v
            LEFT JOIN v.inventario inv
            WHERE p.estado = com.tienda.ropa.entity.EstadoGeneral.ACTIVO
              AND (:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
              AND (:categoriaId IS NULL OR p.categoria.id = :categoriaId)
              AND (:precioMin IS NULL OR p.precioVenta >= :precioMin)
              AND (:precioMax IS NULL OR p.precioVenta <= :precioMax)
              AND (:tallaId IS NULL OR v.talla.id = :tallaId)
              AND (:colorId IS NULL OR v.color.id = :colorId)
              AND (:soloOfertas = false OR (p.precioOferta IS NOT NULL AND p.precioOferta < p.precioVenta))
              AND (:soloDisponibles = false OR inv.stockActual > 0)
            """)
    List<Producto> buscarConFiltros(@Param("nombre") String nombre,
                                     @Param("categoriaId") Long categoriaId,
                                     @Param("precioMin") BigDecimal precioMin,
                                     @Param("precioMax") BigDecimal precioMax,
                                     @Param("tallaId") Long tallaId,
                                     @Param("colorId") Long colorId,
                                     @Param("soloOfertas") boolean soloOfertas,
                                     @Param("soloDisponibles") boolean soloDisponibles);
}
