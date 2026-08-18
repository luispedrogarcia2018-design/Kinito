package com.tienda.ropa.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

// Datos finales para cerrar la venta: a quien se le vendio y como pago.
// El carrito con los productos viaja aparte, guardado en la sesion.
@Data
public class ConfirmarVentaForm {

    @NotNull(message = "Selecciona un cliente")
    private Long clienteId;

    @NotNull(message = "Selecciona un método de pago")
    private Long metodoPagoId;

    @DecimalMin(value = "0.0", message = "El descuento no puede ser negativo")
    private BigDecimal descuento = BigDecimal.ZERO;
}
