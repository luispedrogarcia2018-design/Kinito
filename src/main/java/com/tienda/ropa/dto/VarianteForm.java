package com.tienda.ropa.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Datos del formulario para crear una nueva variante (talla+color) de un producto. */
@Data
public class VarianteForm {

    @NotNull(message = "Debes seleccionar un producto")
    private Long productoId;

    @NotNull(message = "Debes seleccionar una talla")
    private Long tallaId;

    @NotNull(message = "Debes seleccionar un color")
    private Long colorId;

    @NotNull(message = "Indica el stock inicial")
    @Min(value = 0, message = "El stock inicial no puede ser negativo")
    private Integer stockInicial = 0;

    @NotNull(message = "Indica el stock mínimo")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo = 5;
}
