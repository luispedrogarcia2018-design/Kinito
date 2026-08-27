package com.tienda.ropa.controller.admin;

import com.tienda.ropa.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping
    public String indice() {
        return "reportes/indice";
    }

    // Por defecto muestra el mes actual completo si no se indica un rango.
    @GetMapping("/ventas")
    public String ventas(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                          Model model) {
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate inicio = desde != null ? desde : fin.withDayOfMonth(1);

        model.addAttribute("reporte", reporteService.reporteVentas(inicio, fin));
        return "reportes/ventas";
    }

    @GetMapping("/inventario")
    public String inventario(Model model) {
        model.addAttribute("inventarioActual", reporteService.reporteInventarioActual());
        model.addAttribute("stockBajo", reporteService.reporteStockBajo());
        model.addAttribute("agotados", reporteService.reporteAgotados());
        return "reportes/inventario";
    }

    @GetMapping("/productos")
    public String productos(Model model) {
        model.addAttribute("masVendidos", reporteService.reporteMasVendidos());
        model.addAttribute("menosVendidos", reporteService.reporteMenosVendidos());
        model.addAttribute("porCategoria", reporteService.reporteVentasPorCategoria());
        return "reportes/productos";
    }

    @GetMapping("/finanzas")
    public String finanzas(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                            Model model) {
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate inicio = desde != null ? desde : fin.withDayOfMonth(1);

        model.addAttribute("reporte", reporteService.reporteFinanzas(inicio, fin));
        return "reportes/finanzas";
    }
}
