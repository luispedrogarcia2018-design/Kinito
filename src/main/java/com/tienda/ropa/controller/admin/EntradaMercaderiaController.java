package com.tienda.ropa.controller.admin;

import com.tienda.ropa.dto.DetalleEntradaForm;
import com.tienda.ropa.dto.EntradaForm;
import com.tienda.ropa.exception.ReglaDeNegocioException;
import com.tienda.ropa.exception.RecursoNoEncontradoException;
import com.tienda.ropa.repository.ProveedorRepository;
import com.tienda.ropa.repository.VarianteProductoRepository;
import com.tienda.ropa.security.UsuarioPrincipal;
import com.tienda.ropa.service.EntradaMercaderiaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/entradas")
@RequiredArgsConstructor
public class EntradaMercaderiaController {

    private final EntradaMercaderiaService entradaService;
    private final ProveedorRepository proveedorRepository;
    private final VarianteProductoRepository varianteRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("entradas", entradaService.listar());
        return "entradas/lista";
    }

    @GetMapping("/nueva")
    public String formularioNueva(Model model) {
        model.addAttribute("entradaForm", new EntradaForm());
        model.addAttribute("proveedores", proveedorRepository.findAll());
        return "entradas/nueva";
    }

    @PostMapping("/nueva")
    public String crearCabecera(@Valid @ModelAttribute("entradaForm") EntradaForm form,
                                 BindingResult bindingResult,
                                 @AuthenticationPrincipal UsuarioPrincipal principal,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("proveedores", proveedorRepository.findAll());
            return "entradas/nueva";
        }
        var entrada = entradaService.crearCabecera(form, principal.getUsuario());
        // Redirige directo al detalle: ahi es donde se van agregando los productos.
        return "redirect:/admin/entradas/" + entrada.getId();
    }

    @GetMapping("/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        model.addAttribute("entrada", entradaService.buscarConDetalle(id));
        model.addAttribute("detalleForm", new DetalleEntradaForm());
        model.addAttribute("variantes", varianteRepository.findTodasActivasConDatos());
        return "entradas/detalle";
    }

    @PostMapping("/{id}/agregar")
    public String agregarDetalle(@PathVariable Long id,
                                  @Valid @ModelAttribute("detalleForm") DetalleEntradaForm form,
                                  BindingResult bindingResult,
                                  @AuthenticationPrincipal UsuarioPrincipal principal,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("entrada", entradaService.buscarConDetalle(id));
            model.addAttribute("variantes", varianteRepository.findTodasActivasConDatos());
            return "entradas/detalle";
        }
        try {
            entradaService.agregarDetalle(id, form, principal.getUsuario());
            redirectAttributes.addFlashAttribute("mensajeExito", "Producto agregado a la entrada.");
        } catch (ReglaDeNegocioException | RecursoNoEncontradoException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
        }
        return "redirect:/admin/entradas/" + id;
    }
}
