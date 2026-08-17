package com.tienda.ropa.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

// Datos para agregar UNA linea (un producto/variante) a una entrada ya creada.
@Data
public class DetalleEntradaForm {

    @NotNull(message = "Selecciona una variante")
    private Long varianteId;

    @NotNull(message = "Indica la cantidad")
    @Min(value = 1, message = "La cantidad debe ser mayor que cero")
    private Integer cantidad;

    @NotNull(message = "Indica el costo unitario")
    @DecimalMin(value = "0.0", message = "El costo unitario no puede ser negativo")
    private BigDecimal costoUnitario;
}
