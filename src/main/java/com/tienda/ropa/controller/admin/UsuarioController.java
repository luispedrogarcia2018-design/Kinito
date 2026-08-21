package com.tienda.ropa.controller.admin;

import com.tienda.ropa.dto.CambiarPasswordForm;
import com.tienda.ropa.dto.UsuarioForm;
import com.tienda.ropa.entity.Usuario;
import com.tienda.ropa.exception.ReglaDeNegocioException;
import com.tienda.ropa.exception.RecursoNoEncontradoException;
import com.tienda.ropa.repository.RolRepository;
import com.tienda.ropa.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final RolRepository rolRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("usuarioForm", new UsuarioForm());
        model.addAttribute("roles", rolRepository.findAll());
        return "usuarios/formulario";
    }

    @GetMapping("/editar/{id}")
    public String formularioEditar(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id);

        UsuarioForm form = new UsuarioForm();
        form.setId(usuario.getId());
        form.setNombre(usuario.getNombre());
        form.setUsername(usuario.getUsername());
        form.setCorreo(usuario.getCorreo());
        form.setRolId(usuario.getRol().getId());

        model.addAttribute("usuarioForm", form);
        model.addAttribute("roles", rolRepository.findAll());
        return "usuarios/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("usuarioForm") UsuarioForm form,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", rolRepository.findAll());
            return "usuarios/formulario";
        }
        try {
            if (form.getId() == null) {
                usuarioService.crear(form);
                redirectAttributes.addFlashAttribute("mensajeExito", "Usuario creado correctamente.");
            } else {
                usuarioService.actualizar(form.getId(), form);
                redirectAttributes.addFlashAttribute("mensajeExito", "Usuario actualizado correctamente.");
            }
            return "redirect:/admin/usuarios";
        } catch (ReglaDeNegocioException | RecursoNoEncontradoException ex) {
            model.addAttribute("roles", rolRepository.findAll());
            model.addAttribute("errorNegocio", ex.getMessage());
            return "usuarios/formulario";
        }
    }

    @PostMapping("/desactivar/{id}")
    public String desactivar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        usuarioService.desactivar(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Usuario desactivado.");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/reactivar/{id}")
    public String reactivar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        usuarioService.reactivar(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Usuario reactivado.");
        return "redirect:/admin/usuarios";
    }

    // El admin cambia la contraseña de OTRO usuario.
    @GetMapping("/cambiar-password/{id}")
    public String formularioCambiarPassword(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id);
        CambiarPasswordForm form = new CambiarPasswordForm();
        form.setUsuarioId(id);
        model.addAttribute("passwordForm", form);
        model.addAttribute("usuario", usuario);
        model.addAttribute("volverA", "/admin/usuarios");
        return "usuarios/cambiar-password";
    }

    @PostMapping("/cambiar-password")
    public String cambiarPassword(@Valid @ModelAttribute("passwordForm") CambiarPasswordForm form,
                                   BindingResult bindingResult,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("usuario", usuarioService.buscarPorId(form.getUsuarioId()));
            model.addAttribute("volverA", "/admin/usuarios");
            return "usuarios/cambiar-password";
        }
        try {
            usuarioService.cambiarPassword(form);
            redirectAttributes.addFlashAttribute("mensajeExito", "Contraseña actualizada correctamente.");
            return "redirect:/admin/usuarios";
        } catch (ReglaDeNegocioException ex) {
            model.addAttribute("usuario", usuarioService.buscarPorId(form.getUsuarioId()));
            model.addAttribute("volverA", "/admin/usuarios");
            model.addAttribute("errorNegocio", ex.getMessage());
            return "usuarios/cambiar-password";
        }
    }
}
