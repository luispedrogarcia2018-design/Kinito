package com.tienda.ropa.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// Datos del formulario de proveedor (crear/editar). Igual que con Producto,
// se usa un DTO en vez de la entidad directa para no exponer campos internos
// como fecha_creacion o estado en el formulario.
@Data
public class ProveedorForm {

    private Long id; // null = creando, con valor = editando

    @NotBlank(message = "El nombre de contacto es obligatorio")
    private String nombre;

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    private String empresa;

    private String nit;
    private String telefono;
    private String correo;
    private String direccion;
    private String observaciones;
}
