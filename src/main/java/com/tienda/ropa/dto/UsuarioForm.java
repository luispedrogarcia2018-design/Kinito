package com.tienda.ropa.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// Formulario para crear/editar un usuario. La contrasena solo se pide al
// CREAR (passwordNueva); para cambiarla despues se usa CambiarPasswordForm,
// nunca se edita junto con los demas datos.
@Data
public class UsuarioForm {

    private Long id; // null = creando, con valor = editando

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El usuario (username) es obligatorio")
    private String username;

    @Email(message = "Correo inválido")
    private String correo;

    @NotNull(message = "Selecciona un rol")
    private Long rolId;

    // Solo obligatoria al crear (el Service la valida segun el caso).
    private String passwordNueva;
}
