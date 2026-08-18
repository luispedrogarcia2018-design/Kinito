package com.tienda.ropa.service;

import com.tienda.ropa.dto.CarritoItem;
import com.tienda.ropa.dto.ConfirmarVentaForm;
import com.tienda.ropa.dto.ItemVentaForm;
import com.tienda.ropa.entity.*;
import com.tienda.ropa.exception.ReglaDeNegocioException;
import com.tienda.ropa.exception.RecursoNoEncontradoException;
import com.tienda.ropa.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

// Nunca permite vender mas de lo disponible: el carrito se arma en memoria
// (sesion HTTP) y NO toca la base de datos hasta confirmarVenta(), donde
// todo pasa en UNA sola transaccion -- si una linea falla por falta de
// stock, ninguna venta ni ningun movimiento de Kardex queda registrado
// (rollback completo, tal como pide el diseño original).
@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final VarianteProductoRepository varianteRepository;
    private final InventarioRepository inventarioRepository;
    private final ClienteRepository clienteRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final InventarioService inventarioService;

    @Transactional(readOnly = true)
    public List<Venta> listar() {
        return ventaRepository.findAllConDatos();
    }

    @Transactional(readOnly = true)
    public Venta buscarConDetalle(Long id) {
        return ventaRepository.findByIdConDetalle(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Venta no encontrada (id=" + id + ")"));
    }

    // Agrega (o suma cantidad si ya estaba) un producto al carrito en sesion.
    // Valida contra el stock REAL menos lo que ya hay en el carrito, para no
    // dejar agregar mas de lo que existe incluso antes de confirmar la venta.
    @Transactional(readOnly = true)
    public void agregarAlCarrito(List<CarritoItem> carrito, ItemVentaForm form) {
        VarianteProducto variante = varianteRepository.findById(form.getVarianteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));

        Inventario inventario = inventarioRepository.findByVarianteId(variante.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Inventario no encontrado"));

        int yaEnCarrito = carrito.stream()
                .filter(i -> i.getVarianteId().equals(variante.getId()))
                .mapToInt(CarritoItem::getCantidad)
                .sum();

        if (yaEnCarrito + form.getCantidad() > inventario.getStockActual()) {
            throw new ReglaDeNegocioException(
                    "Solo hay " + inventario.getStockActual() + " unidades disponibles de "
                            + variante.getDescripcionCorta() + " (ya tienes " + yaEnCarrito + " en el carrito)");
        }

        Producto producto = variante.getProducto();
        BigDecimal precio = producto.isEnOferta() ? producto.getPrecioOferta() : producto.getPrecioVenta();

        CarritoItem existente = carrito.stream()
                .filter(i -> i.getVarianteId().equals(variante.getId()))
                .findFirst().orElse(null);

        if (existente != null) {
            existente.setCantidad(existente.getCantidad() + form.getCantidad());
            existente.setSubtotal(precio.multiply(BigDecimal.valueOf(existente.getCantidad())));
        } else {
            CarritoItem item = new CarritoItem();
            item.setVarianteId(variante.getId());
            item.setSkuVariante(variante.getSkuVariante());
            item.setDescripcion(variante.getDescripcionCorta());
            item.setCantidad(form.getCantidad());
            item.setPrecioUnitario(precio);
            item.setSubtotal(precio.multiply(BigDecimal.valueOf(form.getCantidad())));
            carrito.add(item);
        }
    }

    // Convierte el carrito en una Venta real: crea cabecera + detalle, y
    // descuenta el stock de cada variante via InventarioService (que ya
    // protege contra stock negativo y deja el Kardex). Todo en una transaccion.
    @Transactional
    public Venta confirmarVenta(List<CarritoItem> carrito, ConfirmarVentaForm form, Usuario usuario) {
        if (carrito == null || carrito.isEmpty()) {
            throw new ReglaDeNegocioException("El carrito está vacío, agrega al menos un producto");
        }

        Cliente cliente = clienteRepository.findById(form.getClienteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));
        MetodoPago metodoPago = metodoPagoRepository.findById(form.getMetodoPagoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Método de pago no encontrado"));

        BigDecimal subtotal = carrito.stream()
                .map(CarritoItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal descuento = form.getDescuento() != null ? form.getDescuento() : BigDecimal.ZERO;
        if (descuento.compareTo(subtotal) > 0) {
            throw new ReglaDeNegocioException("El descuento no puede ser mayor que el subtotal");
        }
        BigDecimal total = subtotal.subtract(descuento);

        Venta venta = new Venta();
        venta.setNumeroVenta(generarNumeroVenta());
        venta.setCliente(cliente);
        venta.setUsuario(usuario);
        venta.setMetodoPago(metodoPago);
        venta.setSubtotal(subtotal);
        venta.setDescuento(descuento);
        venta.setTotal(total);
        venta.setEstado(Venta.EstadoVenta.COMPLETADA);
        venta = ventaRepository.save(venta);

        for (CarritoItem item : carrito) {
            VarianteProducto variante = varianteRepository.findById(item.getVarianteId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Variante no encontrada"));

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setVariante(variante);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(item.getPrecioUnitario());
            detalle.setSubtotal(item.getSubtotal());
            detalleVentaRepository.save(detalle);

            // Descuenta stock y genera el Kardex. Si no hay stock suficiente
            // (por ejemplo, alguien mas vendio la ultima unidad mientras
            // armabas el carrito), esto lanza excepcion y revierte TODO.
            Inventario inventario = inventarioRepository.findByVarianteId(variante.getId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Inventario no encontrado"));
            inventarioService.aplicarMovimiento(
                    inventario,
                    MovimientoInventario.TipoMovimiento.VENTA,
                    item.getCantidad(),
                    "VENTA",
                    venta.getId(),
                    usuario,
                    "Venta " + venta.getNumeroVenta()
            );
        }

        return venta;
    }

    private String generarNumeroVenta() {
        long siguiente = ventaRepository.count() + 1;
        String candidato = String.format("VEN-%05d", siguiente);
        while (ventaRepository.existsByNumeroVenta(candidato)) {
            siguiente++;
            candidato = String.format("VEN-%05d", siguiente);
        }
        return candidato;
    }
}
