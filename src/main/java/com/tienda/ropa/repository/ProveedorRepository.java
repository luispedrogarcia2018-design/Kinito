package com.tienda.ropa.repository;

import com.tienda.ropa.entity.EstadoGeneral;
import com.tienda.ropa.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    List<Proveedor> findByEstado(EstadoGeneral estado);
}
