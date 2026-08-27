package com.tienda.ropa.service;

import com.tienda.ropa.dto.DashboardDatos;
import com.tienda.ropa.entity.Venta;
import com.tienda.ropa.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Junta datos de varios repositorios en un solo objeto DashboardDatos.
// No tiene reglas de negocio propias, solo consulta y agrupa para la vista.
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final InventarioRepository inventarioRepository;
    private final EntradaMercaderiaRepository entradaRepository;
    private final DetalleVentaRepository detalleVentaRepository;

    @Transactional(readOnly = true)
    public DashboardDatos calcular() {
        DashboardDatos datos = new DashboardDatos();

        LocalDateTime inicioHoy = LocalDate.now().atStartOfDay();
        LocalDateTime finHoy = inicioHoy.plusDays(1).minusNanos(1);
        LocalDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime finMes = LocalDateTime.now();

        // ---- Tarjetas ----
        datos.setVentasHoy(ventaRepository.sumarTotalEntre(inicioHoy, finHoy));
        datos.setVentasMes(ventaRepository.sumarTotalEntre(inicioMes, finMes));
        datos.setProductosRegistrados(productoRepository.count());
        datos.setUnidadesDisponibles(inventarioRepository.totalUnidadesDisponibles());
        datos.setProductosStockBajo(inventarioRepository.findStockBajo().size());
        datos.setProductosAgotados(inventarioRepository.findAgotados().size());

        // ---- Tablas ----
        datos.setUltimasVentas(ventaRepository.ultimasCinco());
        datos.setUltimasEntradas(entradaRepository.ultimasCinco());

        // ---- Grafico: ventas por mes (ultimos 6 meses, incluyendo el actual) ----
        datos.setVentasPorMes(calcularVentasPorMes());

        // ---- Grafico: productos mas vendidos (top 5) ----
        Map<String, Long> topProductos = new LinkedHashMap<>();
        detalleVentaRepository.productosMasVendidos().stream()
                .limit(5)
                .forEach(fila -> topProductos.put((String) fila[0], (Long) fila[1]));
        datos.setProductosMasVendidos(topProductos);

        // ---- Grafico: ventas por categoria ----
        Map<String, BigDecimal> porCategoria = new LinkedHashMap<>();
        detalleVentaRepository.ventasPorCategoria()
                .forEach(fila -> porCategoria.put((String) fila[0], (BigDecimal) fila[1]));
        datos.setVentasPorCategoria(porCategoria);

        return datos;
    }

    // Suma el total vendido mes a mes, para los ultimos 6 meses. Se hace en
    // Java (no en SQL) para mantener la consulta simple y portable.
    private Map<String, BigDecimal> calcularVentasPorMes() {
        Map<String, BigDecimal> resultado = new LinkedHashMap<>();
        List<Venta> completadas = ventaRepository.findByEstado(Venta.EstadoVenta.COMPLETADA);
        DateTimeFormatter etiqueta = DateTimeFormatter.ofPattern("MMM yyyy", new Locale("es", "GT"));

        LocalDate mesActual = LocalDate.now().withDayOfMonth(1);
        for (int i = 5; i >= 0; i--) {
            LocalDate mes = mesActual.minusMonths(i);
            String clave = capitalizar(mes.format(etiqueta));
            BigDecimal total = completadas.stream()
                    .filter(v -> v.getFechaHora().getYear() == mes.getYear()
                            && v.getFechaHora().getMonthValue() == mes.getMonthValue())
                    .map(Venta::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            resultado.put(clave, total);
        }
        return resultado;
    }

    private String capitalizar(String texto) {
        return texto.isEmpty() ? texto : Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}
