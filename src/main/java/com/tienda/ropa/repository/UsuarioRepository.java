package com.tienda.ropa.repository;

import com.tienda.ropa.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    boolean existsByUsername(String username);

    // Trae todos los usuarios con su rol ya cargado (evita error de lazy loading en la vista).
    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol ORDER BY u.nombre")
    List<Usuario> findAllConRol();
}
