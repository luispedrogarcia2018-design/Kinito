package com.tienda.ropa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

// Datos para el reporte de finanzas basicas por rango de fechas.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteFinanzasDatos {
    private LocalDate desde;
    private LocalDate hasta;
    private BigDecimal totalVendido;
    private BigDecimal costoEstimado;
    private BigDecimal gananciaBruta;
}
