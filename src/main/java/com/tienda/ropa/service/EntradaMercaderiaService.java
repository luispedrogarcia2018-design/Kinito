package com.tienda.ropa.service;

import com.tienda.ropa.dto.DetalleEntradaForm;
import com.tienda.ropa.dto.EntradaForm;
import com.tienda.ropa.entity.*;
import com.tienda.ropa.exception.RecursoNoEncontradoException;
import com.tienda.ropa.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

// Registra mercaderia nueva. Cada linea que se agrega llama a
// InventarioService.aplicarMovimiento(...) para sumar el stock de esa
// variante y dejar su registro en el Kardex -- este Service NUNCA toca
// Inventario.stockActual directamente, siempre pasa por InventarioService.
@Service
@RequiredArgsConstructor
public class EntradaMercaderiaService {

    private final EntradaMercaderiaRepository entradaRepository;
    private final DetalleEntradaRepository detalleRepository;
    private final ProveedorRepository proveedorRepository;
    private final VarianteProductoRepository varianteRepository;
    private final InventarioRepository inventarioRepository;
    private final InventarioService inventarioService;

    @Transactional(readOnly = true)
    public List<EntradaMercaderia> listar() {
        return entradaRepository.findAllConProveedorYUsuario();
    }

    @Transactional(readOnly = true)
    public EntradaMercaderia buscarConDetalle(Long id) {
        return entradaRepository.findByIdConDetalle(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Entrada no encontrada (id=" + id + ")"));
    }

    // Crea solo la cabecera (numero, proveedor, fecha). Todavia sin productos ni total.
    @Transactional
    public EntradaMercaderia crearCabecera(EntradaForm form, Usuario usuario) {
        Proveedor proveedor = proveedorRepository.findById(form.getProveedorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor no encontrado"));

        EntradaMercaderia entrada = new EntradaMercaderia();
        entrada.setNumeroEntrada(generarNumeroEntrada());
        entrada.setProveedor(proveedor);
        entrada.setUsuario(usuario);
        entrada.setObservaciones(form.getObservaciones());
        entrada.setTotal(BigDecimal.ZERO);
        return entradaRepository.save(entrada);
    }

    // Agrega una linea (un producto/variante) a una entrada existente:
    // 1) crea el DetalleEntrada, 2) suma el subtotal al total de la cabecera,
    // 3) llama a InventarioService para sumar el stock y generar el Kardex.
    // Todo dentro de una sola transaccion: si algo falla, no queda nada a medias.
    @Transactional
    public void agregarDetalle(Long entradaId, DetalleEntradaForm form, Usuario usuario) {
        EntradaMercaderia entrada = entradaRepository.findById(entradaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Entrada no encontrada"));
        VarianteProducto variante = varianteRepository.findById(form.getVarianteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Variante no encontrada"));

        BigDecimal subtotal = form.getCostoUnitario().multiply(BigDecimal.valueOf(form.getCantidad()));

        DetalleEntrada detalle = new DetalleEntrada();
        detalle.setEntrada(entrada);
        detalle.setVariante(variante);
        detalle.setCantidad(form.getCantidad());
        detalle.setCostoUnitario(form.getCostoUnitario());
        detalle.setSubtotal(subtotal);
        detalleRepository.save(detalle);

        entrada.setTotal(entrada.getTotal().add(subtotal));
        entradaRepository.save(entrada);

        // Aqui es donde realmente se actualiza el stock y se escribe el Kardex.
        Inventario inventario = inventarioRepository.findByVarianteId(variante.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Inventario no encontrado para esa variante"));
        inventarioService.aplicarMovimiento(
                inventario,
                MovimientoInventario.TipoMovimiento.ENTRADA,
                form.getCantidad(),
                "ENTRADA_MERCADERIA",
                entrada.getId(),
                usuario,
                "Entrada " + entrada.getNumeroEntrada()
        );
    }

    // Genera un numero correlativo tipo ENT-00001, ENT-00002...
    private String generarNumeroEntrada() {
        long siguiente = entradaRepository.count() + 1;
        String candidato = String.format("ENT-%05d", siguiente);
        while (entradaRepository.existsByNumeroEntrada(candidato)) {
            siguiente++;
            candidato = String.format("ENT-%05d", siguiente);
        }
        return candidato;
    }
}
