package com.tienda.ropa.repository;

import com.tienda.ropa.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    Optional<Inventario> findByVarianteId(Long varianteId);

    /**
     * Trae el inventario completo con producto, categoria, talla y color ya
     * cargados en la misma consulta (evita LazyInitializationException en
     * la vista, ya que open-in-view esta desactivado).
     */
    @Query("""
            SELECT i FROM Inventario i
            JOIN FETCH i.variante v
            JOIN FETCH v.producto p
            JOIN FETCH p.categoria
            JOIN FETCH v.talla
            JOIN FETCH v.color
            WHERE p.estado = com.tienda.ropa.entity.EstadoGeneral.ACTIVO
            ORDER BY p.nombre, v.talla.orden
            """)
    List<Inventario> findInventarioCompleto();

    @Query("SELECT i FROM Inventario i WHERE i.stockActual <= i.stockMinimo AND i.stockActual > 0")
    List<Inventario> findStockBajo();

    @Query("SELECT i FROM Inventario i WHERE i.stockActual = 0")
    List<Inventario> findAgotados();

    @Query("SELECT COALESCE(SUM(i.stockActual), 0) FROM Inventario i")
    long totalUnidadesDisponibles();
}
