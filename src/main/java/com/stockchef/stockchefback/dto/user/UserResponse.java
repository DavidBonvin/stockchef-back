package com.stockchef.stockchefback.dto.user;

import com.stockchef.stockchefback.model.UserRole;

import java.time.LocalDateTime;

/**
 * DTO pour la réponse d'informations utilisateur
 * Utilisé pour retourner les données utilisateur sans informations sensibles
 * Utilise UUID pour identifiants sécurisés
 */
public record UserResponse(
        String id,
        String email,
        String firstName,
        String lastName,
        String fullName,
        UserRole role,
        UserRole effectiveRole,  // Rôle effectif (peut être différent si inactif)
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime lastLoginAt,
        String createdBy
) {
    
    /**
     * Constructeur de convenance pour créer une réponse utilisateur
     * Le nom complet est automatiquement calculé
     */
    public UserResponse(String id, String email, String firstName, String lastName, 
                       UserRole role, UserRole effectiveRole, Boolean isActive, 
                       LocalDateTime createdAt, LocalDateTime lastLoginAt, String createdBy) {
        this(id, email, firstName, lastName, 
             firstName + " " + lastName, // fullName calculé
             role, effectiveRole, isActive, createdAt, lastLoginAt, createdBy);
    }
}