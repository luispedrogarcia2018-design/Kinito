package com.tienda.ropa.exception;

/** Se lanza cuando se busca por id/codigo algo que no existe (producto, cliente, etc.). */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
