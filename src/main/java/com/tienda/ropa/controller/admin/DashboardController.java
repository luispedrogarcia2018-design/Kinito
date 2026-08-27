package com.tienda.ropa.controller.admin;

import com.tienda.ropa.security.UsuarioPrincipal;
import com.tienda.ropa.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin/dashboard")
    public String dashboard(@AuthenticationPrincipal UsuarioPrincipal principal, Model model) {
        model.addAttribute("usuario", principal.getUsuario());
        model.addAttribute("datos", dashboardService.calcular());
        return "admin/dashboard";
    }
}
