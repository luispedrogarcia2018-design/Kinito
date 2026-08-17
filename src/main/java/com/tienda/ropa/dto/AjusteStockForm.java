package com.tienda.ropa.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Datos del formulario de ajuste manual de inventario (positivo o negativo). */
@Data
public class AjusteStockForm {

    @NotNull
    private Long varianteId;

    @NotNull(message = "Selecciona el tipo de ajuste")
    private String tipo; // "POSITIVO" o "NEGATIVO"

    @NotNull(message = "Indica la cantidad")
    @Min(value = 1, message = "La cantidad debe ser mayor que cero")
    private Integer cantidad;

    @NotBlank(message = "Debes indicar el motivo del ajuste")
    private String motivo;
}
