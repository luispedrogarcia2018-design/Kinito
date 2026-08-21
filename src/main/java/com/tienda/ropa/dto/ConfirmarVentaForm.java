package com.tienda.ropa.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

// Datos finales para cerrar la venta. Ya NO se elige un cliente de una lista:
// si nombreCliente viene vacio, se usa "Consumidor Final"; si el vendedor
// escribe un nombre (y opcionalmente un NIT), VentaService crea ese cliente
// automaticamente al confirmar la venta.
@Data
public class ConfirmarVentaForm {

    @NotNull(message = "Selecciona un método de pago")
    private Long metodoPagoId;

    @DecimalMin(value = "0.0", message = "El descuento no puede ser negativo")
    private BigDecimal descuento = BigDecimal.ZERO;

    // Opcional: si se deja vacio, la venta queda a nombre de "Consumidor Final".
    private String nombreCliente;

    // Opcional: NIT de la persona/empresa que compra (solo si se indico nombreCliente).
    private String nitCliente;
}
