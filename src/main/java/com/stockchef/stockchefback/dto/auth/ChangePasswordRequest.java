package com.stockchef.stockchefback.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para cambio de contraseña
 */
public record ChangePasswordRequest(
        @NotBlank(message = "La contraseña actual es obligatoria")
        String currentPassword,
        
        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 8, max = 100, message = "La nueva contraseña debe tener entre 8 y 100 caracteres")
        String newPassword,
        
        @NotBlank(message = "La confirmación de contraseña es obligatoria")
        String confirmPassword
) {
    // Validation moved to service layer to avoid constructor issues in tests
}