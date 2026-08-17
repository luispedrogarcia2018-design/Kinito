package com.tienda.ropa.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Traduce las excepciones de negocio en mensajes legibles para el usuario,
 * en vez de dejar que Spring muestre la pantalla generica de error 500.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public String manejarNoEncontrado(RecursoNoEncontradoException ex,
                                       HttpServletRequest request,
                                       RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
        return "redirect:" + rutaBaseDeVuelta(request);
    }

    @ExceptionHandler(ReglaDeNegocioException.class)
    public String manejarReglaNegocio(ReglaDeNegocioException ex,
                                       HttpServletRequest request,
                                       RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
        return "redirect:" + rutaBaseDeVuelta(request);
    }

    /** Intenta volver al listado del modulo (ej. /admin/productos/editar/5 -> /admin/productos). */
    private String rutaBaseDeVuelta(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String[] partes = uri.split("/");
        if (partes.length >= 3) {
            return "/" + partes[1] + "/" + partes[2];
        }
        return "/admin/dashboard";
    }
}
