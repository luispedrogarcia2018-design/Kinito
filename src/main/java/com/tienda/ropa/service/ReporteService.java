package com.tienda.ropa.service;

import com.tienda.ropa.dto.ReporteFinanzasDatos;
import com.tienda.ropa.dto.ReporteVentasDatos;
import com.tienda.ropa.entity.Inventario;
import com.tienda.ropa.entity.Venta;
import com.tienda.ropa.repository.DetalleVentaRepository;
import com.tienda.ropa.repository.InventarioRepository;
import com.tienda.ropa.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// No tiene reglas de negocio propias: solo arma reportes de solo-lectura a
// partir de las mismas consultas que ya usan Inventario, Ventas y el Dashboard.
@Service
@RequiredArgsConstructor
public class ReporteService {

    private final VentaRepository ventaRepository;
    private final InventarioRepository inventarioRepository;
    private final DetalleVentaRepository detalleVentaRepository;

    @Transactional(readOnly = true)
    public ReporteVentasDatos reporteVentas(LocalDate desde, LocalDate hasta) {
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.atTime(23, 59, 59);

        List<Venta> ventas = ventaRepository.findParaReporte(inicio, fin);
        BigDecimal total = ventas.stream().map(Venta::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ReporteVentasDatos(desde, hasta, ventas, ventas.size(), total);
    }

    @Transactional(readOnly = true)
    public List<Inventario> reporteInventarioActual() {
        return inventarioRepository.findInventarioCompleto();
    }

    @Transactional(readOnly = true)
    public List<Inventario> reporteStockBajo() {
        return inventarioRepository.findStockBajo();
    }

    @Transactional(readOnly = true)
    public List<Inventario> reporteAgotados() {
        return inventarioRepository.findAgotados();
    }

    // Object[] -> [nombre, unidades], ya ordenado por la consulta.
    @Transactional(readOnly = true)
    public Map<String, Long> reporteMasVendidos() {
        return convertirALong(detalleVentaRepository.productosMasVendidos());
    }

    @Transactional(readOnly = true)
    public Map<String, Long> reporteMenosVendidos() {
        return convertirALong(detalleVentaRepository.productosMenosVendidos());
    }

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> reporteVentasPorCategoria() {
        Map<String, BigDecimal> resultado = new LinkedHashMap<>();
        detalleVentaRepository.ventasPorCategoria()
                .forEach(fila -> resultado.put((String) fila[0], (BigDecimal) fila[1]));
        return resultado;
    }

    @Transactional(readOnly = true)
    public ReporteFinanzasDatos reporteFinanzas(LocalDate desde, LocalDate hasta) {
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.atTime(23, 59, 59);

        List<Object[]> filas = detalleVentaRepository.totalesFinancieros(inicio, fin);
        BigDecimal totalVendido = BigDecimal.ZERO;
        BigDecimal costoEstimado = BigDecimal.ZERO;
        if (!filas.isEmpty()) {
            Object[] fila = filas.get(0);
            totalVendido = (BigDecimal) fila[0];
            costoEstimado = (BigDecimal) fila[1];
        }
        BigDecimal gananciaBruta = totalVendido.subtract(costoEstimado);

        return new ReporteFinanzasDatos(desde, hasta, totalVendido, costoEstimado, gananciaBruta);
    }

    private Map<String, Long> convertirALong(List<Object[]> filas) {
        Map<String, Long> resultado = new LinkedHashMap<>();
        filas.forEach(fila -> resultado.put((String) fila[0], (Long) fila[1]));
        return resultado;
    }
}
