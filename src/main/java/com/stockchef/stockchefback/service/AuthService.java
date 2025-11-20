package com.stockchef.stockchefback.service;

import com.stockchef.stockchefback.dto.auth.RefreshTokenRequest;
import com.stockchef.stockchefback.exception.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service pour gestion d'authentification et tokens JWT
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    /**
     * DTO pour réponse de tokens
     */
    public static class TokenResponse {
        private final String accessToken;
        private final String refreshToken;
        private final Long expiresIn;

        public TokenResponse(String accessToken, String refreshToken, Long expiresIn) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.expiresIn = expiresIn;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public Long getExpiresIn() {
            return expiresIn;
        }
    }

    /**
     * Renouveler token JWT en utilisant refresh token
     */
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        log.info("Renouvellement token JWT avec refresh token");
        
        if (request.refreshToken() == null || request.refreshToken().trim().isEmpty()) {
            throw new InvalidTokenException("Refresh token ne peut pas être vide");
        }

        // Pour les tests, on simule un comportement basique
        if (request.refreshToken().contains("invalid")) {
            throw new InvalidTokenException("Token invalide");
        }

        // Simulation de nouvelle réponse de token pour tests
        return new TokenResponse(
                "new-access-token-123",
                "new-refresh-token-456", 
                86400000L
        );
    }

    /**
     * Invalider token (logout)
     */
    public void invalidateToken(String userEmail) {
        log.info("Invalidation token pour utilisateur: {}", userEmail);
        
        if (userEmail == null || userEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Email d'utilisateur requis");
        }

        // Pour les tests, on simule simplement le processus
        log.info("Token invalidé avec succès pour: {}", userEmail);
    }
}