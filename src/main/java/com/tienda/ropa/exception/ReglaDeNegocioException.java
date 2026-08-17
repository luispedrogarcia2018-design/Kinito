package com.tienda.ropa.exception;

/**
 * Se lanza cuando una operacion viola una regla de negocio explicita
 * (SKU duplicado, stock insuficiente, venta con cantidad invalida, etc.).
 * El mensaje de esta excepcion esta pensado para mostrarse directamente
 * al usuario en pantalla.
 */
public class ReglaDeNegocioException extends RuntimeException {
    public ReglaDeNegocioException(String mensaje) {
        super(mensaje);
    }
}
