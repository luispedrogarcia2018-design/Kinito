package com.tienda.ropa.controller.admin;

import com.tienda.ropa.dto.ProductoForm;
import com.tienda.ropa.entity.Producto;
import com.tienda.ropa.exception.ReglaDeNegocioException;
import com.tienda.ropa.exception.RecursoNoEncontradoException;
import com.tienda.ropa.repository.CategoriaRepository;
import com.tienda.ropa.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;
    private final CategoriaRepository categoriaRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("productos", productoService.listarActivos());
        return "productos/lista";
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("productoForm", new ProductoForm());
        model.addAttribute("categorias", categoriaRepository.findAll());
        return "productos/formulario";
    }

    @GetMapping("/editar/{id}")
    public String formularioEditar(@PathVariable Long id, Model model) {
        Producto producto = productoService.buscarPorId(id);

        ProductoForm form = new ProductoForm();
        form.setId(producto.getId());
        form.setSku(producto.getSku());
        form.setNombre(producto.getNombre());
        form.setDescripcion(producto.getDescripcion());
        form.setCategoriaId(producto.getCategoria().getId());
        form.setMarca(producto.getMarca());
        form.setPrecioCompra(producto.getPrecioCompra());
        form.setPrecioVenta(producto.getPrecioVenta());
        form.setPrecioOferta(producto.getPrecioOferta());

        model.addAttribute("productoForm", form);
        model.addAttribute("categorias", categoriaRepository.findAll());
        return "productos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("productoForm") ProductoForm form,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categorias", categoriaRepository.findAll());
            return "productos/formulario";
        }

        try {
            if (form.getId() == null) {
                productoService.crear(form);
                redirectAttributes.addFlashAttribute("mensajeExito", "Producto creado correctamente.");
            } else {
                productoService.actualizar(form.getId(), form);
                redirectAttributes.addFlashAttribute("mensajeExito", "Producto actualizado correctamente.");
            }
            return "redirect:/admin/productos";
        } catch (ReglaDeNegocioException | RecursoNoEncontradoException ex) {
            model.addAttribute("categorias", categoriaRepository.findAll());
            model.addAttribute("errorNegocio", ex.getMessage());
            return "productos/formulario";
        }
    }

    @PostMapping("/desactivar/{id}")
    public String desactivar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productoService.desactivar(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Producto desactivado.");
        return "redirect:/admin/productos";
    }

    @PostMapping("/reactivar/{id}")
    public String reactivar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productoService.reactivar(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Producto reactivado.");
        return "redirect:/admin/productos";
    }
}
