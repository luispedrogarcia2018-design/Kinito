package com.tienda.ropa.service;

import com.tienda.ropa.dto.AjusteStockForm;
import com.tienda.ropa.dto.VarianteForm;
import com.tienda.ropa.entity.*;
import com.tienda.ropa.exception.ReglaDeNegocioException;
import com.tienda.ropa.exception.RecursoNoEncontradoException;
import com.tienda.ropa.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Corazon del sistema de inventario. Toda operacion que toca stock pasa por
 * aqui, dentro de una transaccion, y siempre genera su movimiento en el
 * Kardex (movimientos_inventario). Ninguna otra clase debe modificar
 * Inventario.stockActual directamente.
 *
 * El bloqueo optimista (@Version en Inventario) protege contra el caso de
 * dos operaciones concurrentes tocando la misma variante: si otra
 * transaccion ya modifico la fila, Hibernate lanza
 * ObjectOptimisticLockingFailureException y la operacion se puede reintentar
 * desde el Controller/usuario.
 */
@Service
@RequiredArgsConstructor
public class InventarioService {

    private final VarianteProductoRepository varianteRepository;
    private final InventarioRepository inventarioRepository;
    private final ProductoRepository productoRepository;
    private final TallaRepository tallaRepository;
    private final ColorRepository colorRepository;
    private final MovimientoInventarioRepository movimientoRepository;

    @Transactional(readOnly = true)
    public List<Inventario> listarInventarioCompleto() {
        return inventarioRepository.findInventarioCompleto();
    }

    @Transactional(readOnly = true)
    public List<VarianteProducto> listarVariantesDeProducto(Long productoId) {
        return varianteRepository.findByProductoId(productoId);
    }

    @Transactional(readOnly = true)
    public List<MovimientoInventario> kardexDeVariante(Long varianteId) {
        return movimientoRepository.findByVarianteIdOrderByFechaHoraDesc(varianteId);
    }

    /**
     * Crea una nueva variante (producto + talla + color) con su fila de
     * inventario. Si se indica stock inicial mayor a cero, genera tambien
     * el primer movimiento de Kardex (tipo ENTRADA).
     */
    @Transactional
    public VarianteProducto crearVariante(VarianteForm form, Usuario usuario) {
        Producto producto = productoRepository.findById(form.getProductoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
        Talla talla = tallaRepository.findById(form.getTallaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Talla no encontrada"));
        Color color = colorRepository.findById(form.getColorId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Color no encontrado"));

        varianteRepository.findByProductoIdAndTallaIdAndColorId(producto.getId(), talla.getId(), color.getId())
                .ifPresent(v -> {
                    throw new ReglaDeNegocioException(
                            "Ya existe la variante " + color.getNombre() + " / " + talla.getNombre()
                                    + " para este producto");
                });

        VarianteProducto variante = new VarianteProducto();
        variante.setProducto(producto);
        variante.setTalla(talla);
        variante.setColor(color);
        variante.setSkuVariante(generarSkuVariante(producto, talla, color));
        variante.setEstado(EstadoGeneral.ACTIVO);
        variante = varianteRepository.save(variante);

        Inventario inventario = new Inventario();
        inventario.setVariante(variante);
        inventario.setStockActual(0);
        inventario.setStockMinimo(form.getStockMinimo());
        inventario = inventarioRepository.save(inventario);
        variante.setInventario(inventario);

        int stockInicial = form.getStockInicial() != null ? form.getStockInicial() : 0;
        if (stockInicial > 0) {
            aplicarMovimiento(inventario, MovimientoInventario.TipoMovimiento.ENTRADA, stockInicial,
                    "STOCK_INICIAL", null, usuario, "Stock inicial al crear la variante");
        }

        return variante;
    }

    /**
     * Ajuste manual de inventario (positivo o negativo), con motivo obligatorio.
     * Es la unica forma en que el administrador puede corregir stock a mano
     * (por ejemplo, un conteo fisico que no coincide) -- nunca editando la
     * tabla de inventario directamente.
     */
    @Transactional
    public void ajustarStock(AjusteStockForm form, Usuario usuario) {
        Inventario inventario = inventarioRepository.findByVarianteId(form.getVarianteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Inventario no encontrado para esa variante"));

        boolean esPositivo = "POSITIVO".equalsIgnoreCase(form.getTipo());
        MovimientoInventario.TipoMovimiento tipo = esPositivo
                ? MovimientoInventario.TipoMovimiento.AJUSTE_POSITIVO
                : MovimientoInventario.TipoMovimiento.AJUSTE_NEGATIVO;

        if (!esPositivo && form.getCantidad() > inventario.getStockActual()) {
            throw new ReglaDeNegocioException(
                    "No puedes descontar " + form.getCantidad() + " unidades: solo hay "
                            + inventario.getStockActual() + " en stock");
        }

        aplicarMovimiento(inventario, tipo, form.getCantidad(), "AJUSTE_MANUAL", null, usuario, form.getMotivo());
    }

    // ---------------------------------------------------------------
    // Metodos de uso interno, reutilizados mas adelante por
    // EntradaMercaderiaService (ENTRADA) y VentaService (VENTA).
    // ---------------------------------------------------------------

    /**
     * Aplica un movimiento de stock sobre un inventario ya cargado, y deja
     * el registro correspondiente en el Kardex. Es el UNICO lugar del
     * sistema donde se modifica Inventario.stockActual.
     */
    @Transactional
    public void aplicarMovimiento(Inventario inventario,
                                   MovimientoInventario.TipoMovimiento tipo,
                                   int cantidad,
                                   String referenciaTipo,
                                   Long referenciaId,
                                   Usuario usuario,
                                   String observaciones) {
        int stockAnterior = inventario.getStockActual();
        boolean suma = tipo == MovimientoInventario.TipoMovimiento.ENTRADA
                || tipo == MovimientoInventario.TipoMovimiento.AJUSTE_POSITIVO
                || tipo == MovimientoInventario.TipoMovimiento.DEVOLUCION;

        int stockPosterior = suma ? stockAnterior + cantidad : stockAnterior - cantidad;
        if (stockPosterior < 0) {
            throw new ReglaDeNegocioException("La operación dejaría el stock en negativo, no es posible continuar");
        }

        inventario.setStockActual(stockPosterior);
        inventarioRepository.save(inventario); // @Version protege contra concurrencia

        MovimientoInventario movimiento = MovimientoInventario.builder()
                .variante(inventario.getVariante())
                .tipoMovimiento(tipo)
                .cantidad(cantidad)
                .stockAnterior(stockAnterior)
                .stockPosterior(stockPosterior)
                .referenciaTipo(referenciaTipo)
                .referenciaId(referenciaId)
                .usuario(usuario)
                .observaciones(observaciones)
                .build();
        movimientoRepository.save(movimiento);
    }

    private String generarSkuVariante(Producto producto, Talla talla, Color color) {
        String base = producto.getSku() + "-" + abreviar(color.getNombre()) + "-" + talla.getNombre();
        String candidato = base;
        int contador = 1;
        while (varianteRepository.existsBySkuVariante(candidato)) {
            contador++;
            candidato = base + "-" + contador;
        }
        return candidato;
    }

    private String abreviar(String texto) {
        String limpio = texto.trim().toUpperCase();
        return limpio.length() <= 3 ? limpio : limpio.substring(0, 3);
    }
}
