package com.tienda.ropa.controller.admin;

import com.tienda.ropa.dto.CambiarPasswordForm;
import com.tienda.ropa.exception.ReglaDeNegocioException;
import com.tienda.ropa.security.UsuarioPrincipal;
import com.tienda.ropa.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// A diferencia de UsuarioController (solo ADMIN), esta pantalla la puede usar
// CUALQUIER usuario logueado (ADMIN o VENDEDOR) para cambiar su propia
// contraseña -- por eso no vive bajo /admin/usuarios/**.
@Controller
@RequiredArgsConstructor
public class PerfilController {

    private final UsuarioService usuarioService;

    @GetMapping("/admin/perfil/cambiar-password")
    public String formulario(@AuthenticationPrincipal UsuarioPrincipal principal, Model model) {
        CambiarPasswordForm form = new CambiarPasswordForm();
        form.setUsuarioId(principal.getUsuario().getId());
        model.addAttribute("passwordForm", form);
        model.addAttribute("usuario", principal.getUsuario());
        model.addAttribute("volverA", "/admin/dashboard");
        model.addAttribute("esPropia", true);
        return "usuarios/cambiar-password";
    }

    @PostMapping("/admin/perfil/cambiar-password")
    public String guardar(@Valid @ModelAttribute("passwordForm") CambiarPasswordForm form,
                           BindingResult bindingResult,
                           @AuthenticationPrincipal UsuarioPrincipal principal,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        // Se ignora cualquier usuarioId que venga manipulado del formulario:
        // siempre se cambia la contraseña del usuario que tiene la sesion abierta.
        form.setUsuarioId(principal.getUsuario().getId());

        if (bindingResult.hasErrors()) {
            model.addAttribute("usuario", principal.getUsuario());
            model.addAttribute("volverA", "/admin/dashboard");
            model.addAttribute("esPropia", true);
            return "usuarios/cambiar-password";
        }
        try {
            usuarioService.cambiarPassword(form);
            redirectAttributes.addFlashAttribute("mensajeExito", "Tu contraseña se actualizó correctamente.");
            return "redirect:/admin/dashboard";
        } catch (ReglaDeNegocioException ex) {
            model.addAttribute("usuario", principal.getUsuario());
            model.addAttribute("volverA", "/admin/dashboard");
            model.addAttribute("esPropia", true);
            model.addAttribute("errorNegocio", ex.getMessage());
            return "usuarios/cambiar-password";
        }
    }
}
