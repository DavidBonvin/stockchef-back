package com.stockchef.stockchefback.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO pour authentification des utilisateurs
 */
public record LoginRequest(
        
        @NotBlank(message = "L'email est requis")
        @Email(message = "L'email doit avoir un format valide")
        String email,
        
        @NotBlank(message = "Le mot de passe est requis")
        @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
        String password
) {
}