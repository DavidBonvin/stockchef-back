package com.stockchef.stockchefback.dto.auth;

import com.stockchef.stockchefback.model.UserRole;

/**
 * Response DTO para autenticación exitosa
 */
public record LoginResponse(
        String token,
        String email,
        String fullName,
        UserRole role,
        Long expiresIn
) {
}