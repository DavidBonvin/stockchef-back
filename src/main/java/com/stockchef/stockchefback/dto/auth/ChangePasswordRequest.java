package com.stockchef.stockchefback.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO pour changement de mot de passe
 */
public record ChangePasswordRequest(
        @NotBlank(message = "Le mot de passe actuel est obligatoire")
        String currentPassword,
        
        @NotBlank(message = "Le nouveau mot de passe est obligatoire")
        @Size(min = 8, max = 100, message = "Le nouveau mot de passe doit contenir entre 8 et 100 caractères")
        String newPassword,
        
        @NotBlank(message = "La confirmation de mot de passe est obligatoire")
        String confirmPassword
) {
    // Validation moved to service layer to avoid constructor issues in tests
}