package com.tienda.ropa.repository;

import com.tienda.ropa.entity.EntradaMercaderia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EntradaMercaderiaRepository extends JpaRepository<EntradaMercaderia, Long> {
    Optional<EntradaMercaderia> findByNumeroEntrada(String numeroEntrada);
    boolean existsByNumeroEntrada(String numeroEntrada);

    // Trae la entrada con proveedor, usuario y el detalle (con sus variantes) ya
    // cargados, para evitar errores de lazy loading al mostrar la pantalla de detalle.
    @Query("""
            SELECT e FROM EntradaMercaderia e
            JOIN FETCH e.proveedor
            JOIN FETCH e.usuario
            LEFT JOIN FETCH e.detalles d
            LEFT JOIN FETCH d.variante v
            LEFT JOIN FETCH v.producto
            LEFT JOIN FETCH v.talla
            LEFT JOIN FETCH v.color
            WHERE e.id = :id
            """)
    Optional<EntradaMercaderia> findByIdConDetalle(@Param("id") Long id);

    // Listado general (sin detalle) para la pantalla principal de Entradas.
    @Query("""
            SELECT e FROM EntradaMercaderia e
            JOIN FETCH e.proveedor
            JOIN FETCH e.usuario
            ORDER BY e.fecha DESC
            """)
    List<EntradaMercaderia> findAllConProveedorYUsuario();

    // Entradas de un proveedor especifico, para su pantalla de detalle.
    @Query("""
            SELECT e FROM EntradaMercaderia e
            JOIN FETCH e.usuario
            WHERE e.proveedor.id = :proveedorId
            ORDER BY e.fecha DESC
            """)
    List<EntradaMercaderia> findByProveedorId(@Param("proveedorId") Long proveedorId);
}
