package com.tienda.ropa.repository;

import com.tienda.ropa.entity.Categoria;
import com.tienda.ropa.entity.EstadoGeneral;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findByEstado(EstadoGeneral estado);
}
