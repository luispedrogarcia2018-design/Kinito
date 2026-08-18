package com.tienda.ropa.controller.admin;

import com.tienda.ropa.dto.CarritoItem;
import com.tienda.ropa.dto.ConfirmarVentaForm;
import com.tienda.ropa.dto.ItemVentaForm;
import com.tienda.ropa.exception.ReglaDeNegocioException;
import com.tienda.ropa.exception.RecursoNoEncontradoException;
import com.tienda.ropa.repository.ClienteRepository;
import com.tienda.ropa.repository.MetodoPagoRepository;
import com.tienda.ropa.repository.VarianteProductoRepository;
import com.tienda.ropa.security.UsuarioPrincipal;
import com.tienda.ropa.service.VentaService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/ventas")
@RequiredArgsConstructor
public class VentaController {

    private static final String CARRITO_SESION = "carritoVenta";

    private final VentaService ventaService;
    private final VarianteProductoRepository varianteRepository;
    private final ClienteRepository clienteRepository;
    private final MetodoPagoRepository metodoPagoRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("ventas", ventaService.listar());
        return "ventas/lista";
    }

    @GetMapping("/{id}")
    public String verDetalle(@PathVariable Long id, Model model) {
        model.addAttribute("venta", ventaService.buscarConDetalle(id));
        return "ventas/detalle";
    }

    // Pantalla principal del POS: muestra el carrito actual (de la sesion)
    // y los formularios para agregar productos / confirmar la venta.
    @GetMapping("/nueva")
    public String pantallaVenta(HttpSession session, Model model) {
        cargarDatosFormulario(session, model);
        return "ventas/nueva";
    }

    @PostMapping("/nueva/agregar")
    public String agregarProducto(@Valid @ModelAttribute("itemForm") ItemVentaForm form,
                                   BindingResult bindingResult,
                                   HttpSession session,
                                   Model model) {
        List<CarritoItem> carrito = obtenerCarrito(session);
        if (!bindingResult.hasErrors()) {
            try {
                ventaService.agregarAlCarrito(carrito, form);
            } catch (ReglaDeNegocioException | RecursoNoEncontradoException ex) {
                model.addAttribute("errorNegocio", ex.getMessage());
            }
        }
        cargarDatosFormulario(session, model);
        return "ventas/nueva";
    }

    @PostMapping("/nueva/quitar/{varianteId}")
    public String quitarProducto(@PathVariable Long varianteId, HttpSession session, Model model) {
        List<CarritoItem> carrito = obtenerCarrito(session);
        carrito.removeIf(i -> i.getVarianteId().equals(varianteId));
        cargarDatosFormulario(session, model);
        return "ventas/nueva";
    }

    @PostMapping("/nueva/confirmar")
    public String confirmarVenta(@Valid @ModelAttribute("confirmarForm") ConfirmarVentaForm form,
                                  BindingResult bindingResult,
                                  @AuthenticationPrincipal UsuarioPrincipal principal,
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        List<CarritoItem> carrito = obtenerCarrito(session);

        if (bindingResult.hasErrors()) {
            cargarDatosFormulario(session, model);
            return "ventas/nueva";
        }

        try {
            var venta = ventaService.confirmarVenta(carrito, form, principal.getUsuario());
            session.removeAttribute(CARRITO_SESION); // vacia el carrito solo si la venta se confirmo con exito
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Venta " + venta.getNumeroVenta() + " registrada correctamente.");
            return "redirect:/admin/ventas/" + venta.getId();
        } catch (ReglaDeNegocioException | RecursoNoEncontradoException ex) {
            model.addAttribute("errorNegocio", ex.getMessage());
            cargarDatosFormulario(session, model);
            return "ventas/nueva";
        }
    }

    @PostMapping("/nueva/cancelar")
    public String cancelarVenta(HttpSession session) {
        session.removeAttribute(CARRITO_SESION);
        return "redirect:/admin/ventas/nueva";
    }

    // ---------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private List<CarritoItem> obtenerCarrito(HttpSession session) {
        List<CarritoItem> carrito = (List<CarritoItem>) session.getAttribute(CARRITO_SESION);
        if (carrito == null) {
            carrito = new ArrayList<>();
            session.setAttribute(CARRITO_SESION, carrito);
        }
        return carrito;
    }

    private void cargarDatosFormulario(HttpSession session, Model model) {
        List<CarritoItem> carrito = obtenerCarrito(session);
        BigDecimal subtotal = carrito.stream().map(CarritoItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("carrito", carrito);
        model.addAttribute("subtotalCarrito", subtotal);
        model.addAttribute("itemForm", new ItemVentaForm());
        model.addAttribute("confirmarForm", new ConfirmarVentaForm());
        model.addAttribute("variantes", varianteRepository.findTodasActivasConDatos());
        model.addAttribute("clientes", clienteRepository.findAll());
        model.addAttribute("metodosPago", metodoPagoRepository.findAll());
    }
}
