package com.tienda.ropa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

// Una linea del carrito de venta EN MEMORIA (guardada en la sesion HTTP,
// no en la base de datos). Solo se convierte en DetalleVenta real cuando
// el vendedor confirma la venta completa.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarritoItem implements Serializable {
    private Long varianteId;
    private String skuVariante;
    private String descripcion; // "Short Playero Tropical - Negro / M"
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
}
