package com.tienda.ropa.security;

import com.tienda.ropa.entity.EstadoGeneral;
import com.tienda.ropa.entity.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

/**
 * Adaptador entre nuestra entidad Usuario y el UserDetails que Spring
 * Security necesita para manejar la autenticacion.
 */
public class UsuarioPrincipal extends User {

    private final Usuario usuario;

    public UsuarioPrincipal(Usuario usuario) {
        super(usuario.getUsername(), usuario.getPassword(), authorities(usuario));
        this.usuario = usuario;
    }

    private static List<GrantedAuthority> authorities(Usuario usuario) {
        // Spring Security espera el prefijo "ROLE_" para usar hasRole("ADMIN") en las rutas.
        return List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombre()));
    }

    public Usuario getUsuario() {
        return usuario;
    }

    @Override
    public boolean isEnabled() {
        return usuario.getEstado() == EstadoGeneral.ACTIVO;
    }
}
