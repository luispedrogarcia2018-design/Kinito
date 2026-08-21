package com.tienda.ropa.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// Datos del formulario de cliente (crear/editar). Mismo patron que ProveedorForm.
@Data
public class ClienteForm {

    private Long id; // null = creando, con valor = editando

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String apellido;
    private String nit;
    private String telefono;
    private String correo;
    private String direccion;
}
