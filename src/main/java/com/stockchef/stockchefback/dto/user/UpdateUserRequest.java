package com.stockchef.stockchefback.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO pour la mise à jour des informations personnelles d'un utilisateur
 * Permet la modification du prénom, nom et email
 */
public record UpdateUserRequest(
        @NotBlank(message = "Le prénom ne peut pas être vide")
        @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 et 50 caractères")
        String firstName,

        @NotBlank(message = "Le nom ne peut pas être vide") 
        @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caractères")
        String lastName,

        @NotBlank(message = "L'email ne peut pas être vide")
        @Email(message = "Format d'email invalide")
        @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
        String email
) {
}