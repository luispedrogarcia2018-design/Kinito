package com.tienda.ropa.controller.admin;

import com.tienda.ropa.dto.ClienteForm;
import com.tienda.ropa.entity.Cliente;
import com.tienda.ropa.repository.VentaRepository;
import com.tienda.ropa.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;
    private final VentaRepository ventaRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("clientes", clienteService.listarActivos());
        return "clientes/lista";
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("clienteForm", new ClienteForm());
        return "clientes/formulario";
    }

    @GetMapping("/editar/{id}")
    public String formularioEditar(@PathVariable Long id, Model model) {
        Cliente cliente = clienteService.buscarPorId(id);

        ClienteForm form = new ClienteForm();
        form.setId(cliente.getId());
        form.setNombre(cliente.getNombre());
        form.setApellido(cliente.getApellido());
        form.setNit(cliente.getNit());
        form.setTelefono(cliente.getTelefono());
        form.setCorreo(cliente.getCorreo());
        form.setDireccion(cliente.getDireccion());

        model.addAttribute("clienteForm", form);
        return "clientes/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("clienteForm") ClienteForm form,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "clientes/formulario";
        }
        if (form.getId() == null) {
            clienteService.crear(form);
            redirectAttributes.addFlashAttribute("mensajeExito", "Cliente creado correctamente.");
        } else {
            clienteService.actualizar(form.getId(), form);
            redirectAttributes.addFlashAttribute("mensajeExito", "Cliente actualizado correctamente.");
        }
        return "redirect:/admin/clientes";
    }

    @PostMapping("/desactivar/{id}")
    public String desactivar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        clienteService.desactivar(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Cliente desactivado.");
        return "redirect:/admin/clientes";
    }

    // Historial de compras del cliente.
    @GetMapping("/{id}/compras")
    public String verCompras(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", clienteService.buscarPorId(id));
        model.addAttribute("ventas", ventaRepository.findByClienteId(id));
        return "clientes/compras";
    }
}
