package com.stockchef.stockchefback.dto.auth;

import com.stockchef.stockchefback.model.UserRole;

/**
 * Response DTO pour authentification réussie
 */
public record LoginResponse(
        String token,
        String email,
        String fullName,
        UserRole role,
        Long expiresIn
) {
}