package com.tienda.ropa.controller.publico;

import com.tienda.ropa.dto.CarritoItem;
import com.tienda.ropa.dto.ItemVentaForm;
import com.tienda.ropa.exception.ReglaDeNegocioException;
import com.tienda.ropa.exception.RecursoNoEncontradoException;
import com.tienda.ropa.service.VentaService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// Carrito del cliente en la tienda publica. Usa una clave de sesion distinta
// ("carritoPublico") a la del POS de administracion ("carritoVenta"), para
// que un vendedor logueado y un cliente navegando no se pisen el carrito.
// La logica de "agregar y validar stock" se REUTILIZA de VentaService,
// para no duplicar las reglas de negocio.
@Controller
@RequiredArgsConstructor
public class CarritoPublicoController {

    private static final String CARRITO_SESION = "carritoPublico";

    private final VentaService ventaService;

    @GetMapping("/carrito")
    public String verCarrito(HttpSession session, Model model) {
        List<CarritoItem> carrito = obtenerCarrito(session);
        BigDecimal total = carrito.stream().map(CarritoItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("carrito", carrito);
        model.addAttribute("total", total);
        return "public/carrito";
    }

    @PostMapping("/carrito/agregar")
    public String agregar(@Valid @ModelAttribute ItemVentaForm itemForm,
                           BindingResult bindingResult,
                           HttpSession session,
                           org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        List<CarritoItem> carrito = obtenerCarrito(session);
        if (!bindingResult.hasErrors()) {
            try {
                ventaService.agregarAlCarrito(carrito, itemForm);
                redirectAttributes.addFlashAttribute("mensajeExito", "Producto agregado al carrito.");
            } catch (ReglaDeNegocioException | RecursoNoEncontradoException ex) {
                redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
            }
        }
        return "redirect:/carrito";
    }

    @PostMapping("/carrito/quitar/{varianteId}")
    public String quitar(@PathVariable Long varianteId, HttpSession session) {
        obtenerCarrito(session).removeIf(i -> i.getVarianteId().equals(varianteId));
        return "redirect:/carrito";
    }

    @PostMapping("/carrito/vaciar")
    public String vaciar(HttpSession session) {
        session.removeAttribute(CARRITO_SESION);
        return "redirect:/carrito";
    }

    @SuppressWarnings("unchecked")
    private List<CarritoItem> obtenerCarrito(HttpSession session) {
        List<CarritoItem> carrito = (List<CarritoItem>) session.getAttribute(CARRITO_SESION);
        if (carrito == null) {
            carrito = new ArrayList<>();
            session.setAttribute(CARRITO_SESION, carrito);
        }
        return carrito;
    }
}
