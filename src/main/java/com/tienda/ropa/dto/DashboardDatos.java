package com.tienda.ropa.dto;

import com.tienda.ropa.entity.EntradaMercaderia;
import com.tienda.ropa.entity.Venta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Todo lo que necesita la pantalla del dashboard, calculado una sola vez
// en DashboardService para no llenar el Controller de logica.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDatos {

    // Tarjetas de indicadores
    private BigDecimal ventasHoy;
    private BigDecimal ventasMes;
    private long productosRegistrados;
    private long unidadesDisponibles;
    private long productosStockBajo;
    private long productosAgotados;

    // Tablas
    private List<Venta> ultimasVentas;
    private List<EntradaMercaderia> ultimasEntradas;

    // Graficos (nombre -> valor, ya listos para pintar con Chart.js)
    private Map<String, BigDecimal> ventasPorMes = new LinkedHashMap<>();
    private Map<String, Long> productosMasVendidos = new LinkedHashMap<>();
    private Map<String, BigDecimal> ventasPorCategoria = new LinkedHashMap<>();
}
