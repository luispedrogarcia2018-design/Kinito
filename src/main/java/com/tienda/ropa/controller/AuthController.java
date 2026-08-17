package com.tienda.ropa.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    /**
     * Muestra el formulario de login. Spring Security intercepta el POST
     * a esta misma URL automaticamente (configurado en SecurityConfig),
     * no hace falta un metodo aparte para procesar el login.
     */
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }
}
