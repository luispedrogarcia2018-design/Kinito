package com.tienda.ropa.dto;

import com.tienda.ropa.entity.Venta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Datos para el reporte de ventas por rango de fechas.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteVentasDatos {
    private LocalDate desde;
    private LocalDate hasta;
    private List<Venta> ventas;
    private long cantidadVentas;
    private BigDecimal totalVendido;
}
