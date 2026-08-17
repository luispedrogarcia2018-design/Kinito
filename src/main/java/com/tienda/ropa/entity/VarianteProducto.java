package com.tienda.ropa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa la combinacion unica de Producto + Talla + Color.
 * El stock de cada variante se controla en la entidad Inventario (relacion 1-1).
 */
@Entity
@Table(name = "variantes_producto",
        uniqueConstraints = @UniqueConstraint(columnNames = {"producto_id", "talla_id", "color_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VarianteProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", nullable = false)
    @JsonIgnore
    private Producto producto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "talla_id", nullable = false)
    private Talla talla;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "color_id", nullable = false)
    private Color color;

    @Column(name = "sku_variante", nullable = false, unique = true, length = 60)
    private String skuVariante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoGeneral estado = EstadoGeneral.ACTIVO;

    @OneToOne(mappedBy = "variante", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Inventario inventario;

    @Transient
    public String getDescripcionCorta() {
        String nombreProducto = producto != null ? producto.getNombre() : "";
        String tallaNombre = talla != null ? talla.getNombre() : "";
        String colorNombre = color != null ? color.getNombre() : "";
        return nombreProducto + " - " + colorNombre + " / " + tallaNombre;
    }
}
