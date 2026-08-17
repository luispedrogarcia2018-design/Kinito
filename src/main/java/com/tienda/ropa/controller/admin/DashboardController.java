package com.tienda.ropa.controller.admin;

import com.tienda.ropa.security.UsuarioPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/admin/dashboard")
    public String dashboard(@AuthenticationPrincipal UsuarioPrincipal principal, Model model) {
        model.addAttribute("usuario", principal.getUsuario());
        return "admin/dashboard";
    }
}
