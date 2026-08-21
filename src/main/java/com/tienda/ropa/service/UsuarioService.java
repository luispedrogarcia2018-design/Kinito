package com.tienda.ropa.service;

import com.tienda.ropa.dto.CambiarPasswordForm;
import com.tienda.ropa.dto.UsuarioForm;
import com.tienda.ropa.entity.EstadoGeneral;
import com.tienda.ropa.entity.Rol;
import com.tienda.ropa.entity.Usuario;
import com.tienda.ropa.exception.ReglaDeNegocioException;
import com.tienda.ropa.exception.RecursoNoEncontradoException;
import com.tienda.ropa.repository.RolRepository;
import com.tienda.ropa.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Las contrasenas NUNCA se guardan en texto plano: siempre pasan por
// PasswordEncoder (BCrypt) antes de llegar a la base de datos.
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAllConRol();
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado (id=" + id + ")"));
    }

    @Transactional
    public Usuario crear(UsuarioForm form) {
        if (usuarioRepository.existsByUsername(form.getUsername())) {
            throw new ReglaDeNegocioException("Ya existe un usuario con el username '" + form.getUsername() + "'");
        }
        if (form.getPasswordNueva() == null || form.getPasswordNueva().isBlank()) {
            throw new ReglaDeNegocioException("Debes indicar una contraseña para el usuario nuevo");
        }

        Rol rol = obtenerRol(form.getRolId());

        Usuario usuario = new Usuario();
        usuario.setNombre(form.getNombre().trim());
        usuario.setUsername(form.getUsername().trim());
        usuario.setCorreo(form.getCorreo());
        usuario.setRol(rol);
        usuario.setEstado(EstadoGeneral.ACTIVO);
        usuario.setPassword(passwordEncoder.encode(form.getPasswordNueva()));
        return usuarioRepository.save(usuario);
    }

    // Actualiza datos basicos. La contrasena NO se toca aqui (ver cambiarPassword).
    @Transactional
    public Usuario actualizar(Long id, UsuarioForm form) {
        Usuario usuario = buscarPorId(id);

        usuarioRepository.findByUsername(form.getUsername()).ifPresent(existente -> {
            if (!existente.getId().equals(id)) {
                throw new ReglaDeNegocioException("Ya existe otro usuario con el username '" + form.getUsername() + "'");
            }
        });

        Rol rol = obtenerRol(form.getRolId());
        usuario.setNombre(form.getNombre().trim());
        usuario.setUsername(form.getUsername().trim());
        usuario.setCorreo(form.getCorreo());
        usuario.setRol(rol);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void cambiarPassword(CambiarPasswordForm form) {
        if (!form.getPasswordNueva().equals(form.getConfirmarPassword())) {
            throw new ReglaDeNegocioException("La confirmación no coincide con la contraseña nueva");
        }
        Usuario usuario = buscarPorId(form.getUsuarioId());
        usuario.setPassword(passwordEncoder.encode(form.getPasswordNueva()));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void desactivar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.setEstado(EstadoGeneral.INACTIVO);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void reactivar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuario.setEstado(EstadoGeneral.ACTIVO);
        usuarioRepository.save(usuario);
    }

    private Rol obtenerRol(Long rolId) {
        return rolRepository.findById(rolId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado"));
    }
}
