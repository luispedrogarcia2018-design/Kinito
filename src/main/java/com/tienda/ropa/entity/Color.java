package com.tienda.ropa.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "colores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Color {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 40)
    private String nombre;

    @Column(name = "codigo_hexadecimal", length = 7)
    private String codigoHexadecimal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoGeneral estado = EstadoGeneral.ACTIVO;
}
