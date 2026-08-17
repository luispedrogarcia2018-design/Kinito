package com.tienda.ropa.controller.admin;

import com.tienda.ropa.dto.AjusteStockForm;
import com.tienda.ropa.dto.VarianteForm;
import com.tienda.ropa.entity.Inventario;
import com.tienda.ropa.exception.ReglaDeNegocioException;
import com.tienda.ropa.exception.RecursoNoEncontradoException;
import com.tienda.ropa.repository.ColorRepository;
import com.tienda.ropa.repository.InventarioRepository;
import com.tienda.ropa.repository.TallaRepository;
import com.tienda.ropa.security.UsuarioPrincipal;
import com.tienda.ropa.service.InventarioService;
import com.tienda.ropa.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;
    private final ProductoService productoService;
    private final TallaRepository tallaRepository;
    private final ColorRepository colorRepository;
    private final InventarioRepository inventarioRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("inventarios", inventarioService.listarInventarioCompleto());
        return "inventario/lista";
    }

    @GetMapping("/nueva-variante")
    public String formularioNuevaVariante(Model model) {
        model.addAttribute("varianteForm", new VarianteForm());
        model.addAttribute("productos", productoService.listarActivos());
        model.addAttribute("tallas", tallaRepository.findAll());
        model.addAttribute("colores", colorRepository.findAll());
        return "inventario/nueva-variante";
    }

    @PostMapping("/nueva-variante")
    public String guardarVariante(@Valid @ModelAttribute("varianteForm") VarianteForm form,
                                   BindingResult bindingResult,
                                   @AuthenticationPrincipal UsuarioPrincipal principal,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("productos", productoService.listarActivos());
            model.addAttribute("tallas", tallaRepository.findAll());
            model.addAttribute("colores", colorRepository.findAll());
            return "inventario/nueva-variante";
        }
        try {
            inventarioService.crearVariante(form, principal.getUsuario());
            redirectAttributes.addFlashAttribute("mensajeExito", "Variante creada correctamente.");
            return "redirect:/admin/inventario";
        } catch (ReglaDeNegocioException | RecursoNoEncontradoException ex) {
            model.addAttribute("productos", productoService.listarActivos());
            model.addAttribute("tallas", tallaRepository.findAll());
            model.addAttribute("colores", colorRepository.findAll());
            model.addAttribute("errorNegocio", ex.getMessage());
            return "inventario/nueva-variante";
        }
    }

    @GetMapping("/ajuste/{varianteId}")
    public String formularioAjuste(@PathVariable Long varianteId, Model model) {
        Inventario inventario = inventarioRepository.findByVarianteId(varianteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Inventario no encontrado"));

        AjusteStockForm form = new AjusteStockForm();
        form.setVarianteId(varianteId);
        model.addAttribute("ajusteForm", form);
        model.addAttribute("inventario", inventario);
        return "inventario/ajuste";
    }

    @PostMapping("/ajuste")
    public String guardarAjuste(@Valid @ModelAttribute("ajusteForm") AjusteStockForm form,
                                 BindingResult bindingResult,
                                 @AuthenticationPrincipal UsuarioPrincipal principal,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            Inventario inventario = inventarioRepository.findByVarianteId(form.getVarianteId()).orElse(null);
            model.addAttribute("inventario", inventario);
            return "inventario/ajuste";
        }
        try {
            inventarioService.ajustarStock(form, principal.getUsuario());
            redirectAttributes.addFlashAttribute("mensajeExito", "Ajuste de inventario aplicado correctamente.");
            return "redirect:/admin/inventario";
        } catch (ReglaDeNegocioException | RecursoNoEncontradoException ex) {
            Inventario inventario = inventarioRepository.findByVarianteId(form.getVarianteId()).orElse(null);
            model.addAttribute("inventario", inventario);
            model.addAttribute("errorNegocio", ex.getMessage());
            return "inventario/ajuste";
        }
    }

    @GetMapping("/kardex/{varianteId}")
    public String kardex(@PathVariable Long varianteId, Model model) {
        model.addAttribute("movimientos", inventarioService.kardexDeVariante(varianteId));
        return "inventario/kardex";
    }
}
