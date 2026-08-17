package com.tienda.ropa.service;

import com.tienda.ropa.dto.ProductoForm;
import com.tienda.ropa.entity.Categoria;
import com.tienda.ropa.entity.EstadoGeneral;
import com.tienda.ropa.entity.Producto;
import com.tienda.ropa.exception.ReglaDeNegocioException;
import com.tienda.ropa.exception.RecursoNoEncontradoException;
import com.tienda.ropa.repository.CategoriaRepository;
import com.tienda.ropa.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<Producto> listarActivos() {
        return productoRepository.findByEstado(EstadoGeneral.ACTIVO);
    }

    @Transactional(readOnly = true)
    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado (id=" + id + ")"));
    }

    /** Crea un producto nuevo a partir del formulario. */
    @Transactional
    public Producto crear(ProductoForm form) {
        if (productoRepository.existsBySku(form.getSku())) {
            throw new ReglaDeNegocioException("Ya existe un producto con el SKU '" + form.getSku() + "'");
        }
        validarPrecios(form);

        Categoria categoria = obtenerCategoria(form.getCategoriaId());

        Producto producto = new Producto();
        aplicarFormulario(producto, form, categoria);
        return productoRepository.save(producto);
    }

    /** Actualiza un producto existente. El SKU puede repetirse solo consigo mismo. */
    @Transactional
    public Producto actualizar(Long id, ProductoForm form) {
        Producto producto = buscarPorId(id);

        productoRepository.findBySku(form.getSku()).ifPresent(existente -> {
            if (!existente.getId().equals(id)) {
                throw new ReglaDeNegocioException("Ya existe otro producto con el SKU '" + form.getSku() + "'");
            }
        });
        validarPrecios(form);

        Categoria categoria = obtenerCategoria(form.getCategoriaId());
        aplicarFormulario(producto, form, categoria);
        return productoRepository.save(producto);
    }

    /**
     * Eliminacion logica: nunca se borra fisicamente un producto (podria tener
     * ventas o movimientos de inventario asociados). Solo se marca INACTIVO.
     */
    @Transactional
    public void desactivar(Long id) {
        Producto producto = buscarPorId(id);
        producto.setEstado(EstadoGeneral.INACTIVO);
        productoRepository.save(producto);
    }

    @Transactional
    public void reactivar(Long id) {
        Producto producto = buscarPorId(id);
        producto.setEstado(EstadoGeneral.ACTIVO);
        productoRepository.save(producto);
    }

    // ---------------------------------------------------------------

    private Categoria obtenerCategoria(Long categoriaId) {
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada (id=" + categoriaId + ")"));
    }

    private void validarPrecios(ProductoForm form) {
        if (form.getPrecioOferta() != null && form.getPrecioOferta().compareTo(form.getPrecioVenta()) >= 0) {
            throw new ReglaDeNegocioException("El precio de oferta debe ser menor que el precio de venta");
        }
    }

    private void aplicarFormulario(Producto producto, ProductoForm form, Categoria categoria) {
        producto.setSku(form.getSku().trim().toUpperCase());
        producto.setNombre(form.getNombre().trim());
        producto.setDescripcion(form.getDescripcion());
        producto.setCategoria(categoria);
        producto.setMarca(form.getMarca());
        producto.setPrecioCompra(form.getPrecioCompra());
        producto.setPrecioVenta(form.getPrecioVenta());
        producto.setPrecioOferta(form.getPrecioOferta());
    }
}
