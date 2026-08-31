package com.tienda.ropa.controller.publico;

import com.tienda.ropa.entity.EstadoGeneral;
import com.tienda.ropa.entity.Producto;
import com.tienda.ropa.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

// Paginas visibles para cualquier visitante, sin necesidad de iniciar sesion
// (ver SecurityConfig: "/", "/productos/**", "/producto/**" son publicas).
@Controller
@RequiredArgsConstructor
public class PublicoController {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final TallaRepository tallaRepository;
    private final ColorRepository colorRepository;
    private final VarianteProductoRepository varianteRepository;

    @GetMapping("/")
    public String inicio(Model model) {
        List<Producto> todos = productoRepository.findByEstado(EstadoGeneral.ACTIVO);

        // Destacados: los primeros 6 productos activos.
        model.addAttribute("destacados", todos.stream().limit(6).toList());
        // Ofertas: los que tienen precio de oferta valido.
        model.addAttribute("ofertas", todos.stream().filter(Producto::isEnOferta).limit(4).toList());
        model.addAttribute("categorias", categoriaRepository.findByEstado(EstadoGeneral.ACTIVO));
        return "public/index";
    }

    @GetMapping("/productos")
    public String catalogo(@RequestParam(required = false) String nombre,
                            @RequestParam(required = false) Long categoriaId,
                            @RequestParam(required = false) BigDecimal precioMin,
                            @RequestParam(required = false) BigDecimal precioMax,
                            @RequestParam(required = false) Long tallaId,
                            @RequestParam(required = false) Long colorId,
                            @RequestParam(defaultValue = "false") boolean soloOfertas,
                            @RequestParam(defaultValue = "false") boolean soloDisponibles,
                            Model model) {

        List<Producto> productos = productoRepository.buscarConFiltros(
                (nombre != null && !nombre.isBlank()) ? nombre : null,
                categoriaId, precioMin, precioMax, tallaId, colorId, soloOfertas, soloDisponibles);

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categoriaRepository.findByEstado(EstadoGeneral.ACTIVO));
        model.addAttribute("tallas", tallaRepository.findAll());
        model.addAttribute("colores", colorRepository.findAll());

        // Se regresan los filtros aplicados para que el formulario los recuerde.
        model.addAttribute("fNombre", nombre);
        model.addAttribute("fCategoriaId", categoriaId);
        model.addAttribute("fPrecioMin", precioMin);
        model.addAttribute("fPrecioMax", precioMax);
        model.addAttribute("fTallaId", tallaId);
        model.addAttribute("fColorId", colorId);
        model.addAttribute("fSoloOfertas", soloOfertas);
        model.addAttribute("fSoloDisponibles", soloDisponibles);
        return "public/catalogo";
    }

    @GetMapping("/producto/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new com.tienda.ropa.exception.RecursoNoEncontradoException("Producto no encontrado"));
        model.addAttribute("producto", producto);
        model.addAttribute("variantes", varianteRepository.findByProductoId(id));
        return "public/detalle";
    }
}