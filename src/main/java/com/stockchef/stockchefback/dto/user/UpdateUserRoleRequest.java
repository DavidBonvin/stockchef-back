package com.stockchef.stockchefback.dto.user;

import com.stockchef.stockchefback.model.UserRole;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO pour mettre à jour le rôle d'un utilisateur
 * Accessible uniquement aux ADMIN et DEVELOPER
 * L'ID utilisateur vient du path parameter de l'endpoint
 */
public record UpdateUserRoleRequest(
        
        @NotNull(message = "Le nouveau rôle est requis")
        UserRole newRole,
        
        @Size(max = 255, message = "La raison ne peut pas dépasser 255 caractères")
        String reason
) {}