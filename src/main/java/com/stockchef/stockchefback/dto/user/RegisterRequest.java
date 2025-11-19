package com.stockchef.stockchefback.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO pour l'enregistrement d'un nouvel utilisateur
 * Accessible publiquement (tous peuvent s'enregistrer)
 */
public record RegisterRequest(
        
        @NotBlank(message = "L'email est requis")
        @Email(message = "L'email doit avoir un format valide")
        String email,
        
        @NotBlank(message = "Le mot de passe est requis")
        @Size(min = 6, max = 50, message = "Le mot de passe doit contenir entre 6 et 50 caractères")
        String password,
        
        @NotBlank(message = "Le prénom est requis")
        @Size(max = 50, message = "Le prénom ne peut pas dépasser 50 caractères")
        String firstName,
        
        @NotBlank(message = "Le nom de famille est requis")
        @Size(max = 50, message = "Le nom de famille ne peut pas dépasser 50 caractères")
        String lastName
) {
}