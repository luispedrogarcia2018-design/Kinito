package com.tienda.ropa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

// Formulario dedicado SOLO a cambiar contrasena (tanto para que el admin
// cambie la de otro usuario, como para que cualquiera cambie la suya propia).
@Data
public class CambiarPasswordForm {

    @NotNull
    private Long usuarioId;

    @NotBlank(message = "Escribe la nueva contraseña")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String passwordNueva;

    @NotBlank(message = "Confirma la nueva contraseña")
    private String confirmarPassword;
}
