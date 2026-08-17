package com.tienda.ropa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Historial permanente de todos los movimientos de stock (Kardex).
 * Tabla append-only: solo se inserta, nunca se edita ni se borra.
 * Se crea siempre desde InventarioService dentro de una transaccion.
 *
 * referencia_tipo / referencia_id funcionan como una FK "polimorfica" hacia
 * la tabla que origino el movimiento (ventas o entradas_mercaderia). No es
 * una FOREIGN KEY fisica porque la tabla referenciada varia segun el tipo
 * de movimiento; la consistencia la garantiza este mismo Service.
 */
@Entity
@Table(name = "movimientos_inventario", indexes = {
        @Index(name = "idx_movimiento_variante", columnList = "variante_id"),
        @Index(name = "idx_movimiento_fecha", columnList = "fecha_hora")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "variante_id", nullable = false)
    private VarianteProducto variante;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, length = 20)
    private TipoMovimiento tipoMovimiento;

    /** Cantidad de unidades movidas (siempre positiva; el signo lo da tipoMovimiento). */
    @Column(nullable = false)
    private int cantidad;

    @Column(name = "stock_anterior", nullable = false)
    private int stockAnterior;

    @Column(name = "stock_posterior", nullable = false)
    private int stockPosterior;

    @Column(name = "referencia_tipo", length = 30)
    private String referenciaTipo; // 'VENTA', 'ENTRADA_MERCADERIA', 'AJUSTE_MANUAL'...

    @Column(name = "referencia_id")
    private Long referenciaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(length = 255)
    private String observaciones;

    @PrePersist
    public void prePersist() {
        if (fechaHora == null) fechaHora = LocalDateTime.now();
    }

    public enum TipoMovimiento {
        ENTRADA, VENTA, AJUSTE_POSITIVO, AJUSTE_NEGATIVO, DEVOLUCION
    }
}
