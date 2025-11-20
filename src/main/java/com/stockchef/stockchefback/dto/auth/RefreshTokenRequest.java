package com.stockchef.stockchefback.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO pour demande de renouvellement de token JWT
 */
public record RefreshTokenRequest(
        @NotBlank(message = "Le refresh token est obligatoire")
        String refreshToken
) {
}