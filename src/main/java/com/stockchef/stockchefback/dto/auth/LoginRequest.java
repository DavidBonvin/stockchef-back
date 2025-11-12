package com.stockchef.stockchefback.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO para autenticación de usuarios
 */
public record LoginRequest(
        
        @NotBlank(message = "Email es requerido")
        @Email(message = "Email debe tener formato válido")
        String email,
        
        @NotBlank(message = "Password es requerido")
        @Size(min = 6, message = "Password debe tener al menos 6 caracteres")
        String password
) {
}