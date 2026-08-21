package com.tienda.ropa.config;

import com.tienda.ropa.security.UsuarioDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuracion central de Spring Security:
 *  - Rutas publicas (tienda, login, recursos estaticos) vs protegidas (/admin/**)
 *  - Login real contra la tabla `usuarios` (via UsuarioDetailsService)
 *  - Contraseñas verificadas con BCrypt
 *  - Autorizacion por rol: ADMIN accede a todo, VENDEDOR solo a ventas/consultas
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UsuarioDetailsService usuarioDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public org.springframework.security.authentication.AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Recursos estaticos y la tienda publica: visibles para cualquiera
                .requestMatchers("/", "/index", "/productos/**", "/producto/**", "/categorias/**",
                        "/carrito/**", "/contacto", "/ofertas",
                        "/css/**", "/js/**", "/images/**", "/uploads/**", "/webjars/**").permitAll()
                .requestMatchers("/login", "/error").permitAll()

                // Solo ADMIN: administracion de usuarios y datos sensibles
                .requestMatchers("/admin/usuarios/**", "/admin/configuracion/**").hasRole("ADMIN")
                .requestMatchers("/admin/productos/desactivar/**", "/admin/productos/reactivar/**").hasRole("ADMIN")
                .requestMatchers("/admin/inventario/nueva-variante", "/admin/inventario/nueva-variante/**",
                        "/admin/inventario/ajuste", "/admin/inventario/ajuste/**").hasRole("ADMIN")
                .requestMatchers("/admin/proveedores/nuevo", "/admin/proveedores/editar/**",
                        "/admin/proveedores/guardar", "/admin/proveedores/desactivar/**").hasRole("ADMIN")
                .requestMatchers("/admin/clientes/nuevo", "/admin/clientes/editar/**",
                        "/admin/clientes/guardar", "/admin/clientes/desactivar/**").hasRole("ADMIN")

                // ADMIN y VENDEDOR: el resto del panel administrativo
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "VENDEDOR")

                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/admin/dashboard", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
            )
            // CSRF activo por defecto (recomendado); los formularios Thymeleaf
            // con th:action ya incluyen el token automaticamente.
            ;

        return http.build();
    }
}
