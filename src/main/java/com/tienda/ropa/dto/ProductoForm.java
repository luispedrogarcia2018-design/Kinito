package com.tienda.ropa.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Datos que vienen del formulario HTML de producto. Se usa un DTO
 * en vez de la entidad directamente para no exponer relaciones JPA
 * (categoria, imagenes, variantes) en el formulario de forma cruda.
 */
@Data
public class ProductoForm {

    private Long id; // null = creando, con valor = editando

    @NotBlank(message = "El SKU es obligatorio")
    private String sku;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    @NotNull(message = "Debes seleccionar una categoría")
    private Long categoriaId;

    private String marca;

    @NotNull(message = "El precio de compra es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio de compra no puede ser negativo")
    private BigDecimal precioCompra;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio de venta debe ser mayor que cero")
    private BigDecimal precioVenta;

    private BigDecimal precioOferta;
}
