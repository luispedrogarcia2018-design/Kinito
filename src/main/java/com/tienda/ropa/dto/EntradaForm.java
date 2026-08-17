package com.tienda.ropa.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Datos para crear la CABECERA de una entrada de mercaderia (sin productos todavia).
// Los productos se agregan despues, uno por uno, sobre la entrada ya creada.
@Data
public class EntradaForm {

    @NotNull(message = "Debes seleccionar un proveedor")
    private Long proveedorId;

    private String observaciones;
}
