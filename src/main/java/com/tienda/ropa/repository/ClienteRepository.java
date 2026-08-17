package com.tienda.ropa.repository;

import com.tienda.ropa.entity.Cliente;
import com.tienda.ropa.entity.EstadoGeneral;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findByEstado(EstadoGeneral estado);
    Optional<Cliente> findByNit(String nit);
}
