package com.stockchef.stockchefback.dto.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO pour mettre à jour le statut actif/inactif d'un utilisateur
 * Accessible uniquement aux ADMIN et DEVELOPER
 */
public record UpdateUserStatusRequest(
        
        @NotNull(message = "Le statut actif est requis")
        Boolean active,
        
        @Size(max = 255, message = "La raison ne peut pas dépasser 255 caractères")
        String reason
) {
}