package com.tienda.ropa.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Formulario para agregar UN producto al carrito de venta (todavia no se
// guarda en la base de datos, solo se agrega a la lista en sesion).
@Data
public class ItemVentaForm {

    @NotNull(message = "Selecciona un producto")
    private Long varianteId;

    @NotNull(message = "Indica la cantidad")
    @Min(value = 1, message = "La cantidad debe ser mayor que cero")
    private Integer cantidad;
}
