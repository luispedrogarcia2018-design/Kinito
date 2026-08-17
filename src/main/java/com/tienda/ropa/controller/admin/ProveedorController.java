package com.tienda.ropa.controller.admin;

import com.tienda.ropa.dto.ProveedorForm;
import com.tienda.ropa.entity.Proveedor;
import com.tienda.ropa.repository.EntradaMercaderiaRepository;
import com.tienda.ropa.service.ProveedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorService proveedorService;
    private final EntradaMercaderiaRepository entradaMercaderiaRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("proveedores", proveedorService.listarActivos());
        return "proveedores/lista";
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("proveedorForm", new ProveedorForm());
        return "proveedores/formulario";
    }

    @GetMapping("/editar/{id}")
    public String formularioEditar(@PathVariable Long id, Model model) {
        Proveedor proveedor = proveedorService.buscarPorId(id);

        ProveedorForm form = new ProveedorForm();
        form.setId(proveedor.getId());
        form.setNombre(proveedor.getNombre());
        form.setEmpresa(proveedor.getEmpresa());
        form.setNit(proveedor.getNit());
        form.setTelefono(proveedor.getTelefono());
        form.setCorreo(proveedor.getCorreo());
        form.setDireccion(proveedor.getDireccion());
        form.setObservaciones(proveedor.getObservaciones());

        model.addAttribute("proveedorForm", form);
        return "proveedores/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("proveedorForm") ProveedorForm form,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "proveedores/formulario";
        }
        if (form.getId() == null) {
            proveedorService.crear(form);
            redirectAttributes.addFlashAttribute("mensajeExito", "Proveedor creado correctamente.");
        } else {
            proveedorService.actualizar(form.getId(), form);
            redirectAttributes.addFlashAttribute("mensajeExito", "Proveedor actualizado correctamente.");
        }
        return "redirect:/admin/proveedores";
    }

    @PostMapping("/desactivar/{id}")
    public String desactivar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        proveedorService.desactivar(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Proveedor desactivado.");
        return "redirect:/admin/proveedores";
    }

    // Historial de entradas de mercaderia asociadas a este proveedor.
    @GetMapping("/{id}/entradas")
    public String verEntradas(@PathVariable Long id, Model model) {
        model.addAttribute("proveedor", proveedorService.buscarPorId(id));
        model.addAttribute("entradas", entradaMercaderiaRepository.findByProveedorId(id));
        return "proveedores/entradas";
    }
}
