package com.stockchef.stockchefback.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para solicitud de renovación de token JWT
 */
public record RefreshTokenRequest(
        @NotBlank(message = "El refresh token es obligatorio")
        String refreshToken
) {
}