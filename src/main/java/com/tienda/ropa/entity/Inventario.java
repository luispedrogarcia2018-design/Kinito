package com.tienda.ropa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "variante_id", nullable = false, unique = true)
    @JsonIgnore
    private VarianteProducto variante;

    @Column(name = "stock_actual", nullable = false)
    private int stockActual = 0;

    @Column(name = "stock_minimo", nullable = false)
    private int stockMinimo = 5;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    /** Bloqueo optimista: evita sobreventa cuando dos ventas concurrentes tocan la misma fila. */
    @Version
    @Column(nullable = false)
    private Long version;

    @PrePersist
    @PreUpdate
    public void actualizarFecha() {
        fechaActualizacion = LocalDateTime.now();
    }

    @Transient
    public EstadoStock getEstadoStock() {
        if (stockActual <= 0) return EstadoStock.AGOTADO;
        if (stockActual <= stockMinimo) return EstadoStock.STOCK_BAJO;
        return EstadoStock.DISPONIBLE;
    }

    public enum EstadoStock {
        DISPONIBLE, STOCK_BAJO, AGOTADO
    }
}
