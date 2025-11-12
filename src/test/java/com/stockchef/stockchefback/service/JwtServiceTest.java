package com.stockchef.stockchefback.service;

import com.stockchef.stockchefback.model.User;
import com.stockchef.stockchefback.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests TDD para JwtService
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("JWT Service Tests")
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;
    
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("developer@stockchef.com")
                .firstName("Super")
                .lastName("Developer")
                .role(UserRole.ROLE_DEVELOPER)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Debe generar token JWT válido")
    void shouldGenerateValidJwtToken() {
        // When
        String token = jwtService.generateToken(testUser);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT tiene 3 partes separadas por puntos
    }

    @Test
    @DisplayName("Debe extraer email del token")
    void shouldExtractEmailFromToken() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        String extractedEmail = jwtService.extractEmail(token);

        // Then
        assertThat(extractedEmail).isEqualTo("developer@stockchef.com");
    }

    @Test
    @DisplayName("Debe extraer rol del token")
    void shouldExtractRoleFromToken() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        String extractedRole = jwtService.extractRole(token);

        // Then
        assertThat(extractedRole).isEqualTo("ROLE_DEVELOPER");
    }

    @Test
    @DisplayName("Debe validar token correcto")
    void shouldValidateCorrectToken() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        boolean isValid = jwtService.isTokenValid(token, testUser);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Debe rechazar token inválido")
    void shouldRejectInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtService.isTokenValid(invalidToken, testUser);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Debe rechazar token para usuario diferente")
    void shouldRejectTokenForDifferentUser() {
        // Given
        String token = jwtService.generateToken(testUser);
        
        User differentUser = User.builder()
                .id(2L)
                .email("admin@stockchef.com")
                .role(UserRole.ROLE_ADMIN)
                .build();

        // When
        boolean isValid = jwtService.isTokenValid(token, differentUser);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Debe verificar que token no está expirado")
    void shouldVerifyTokenIsNotExpired() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        boolean isExpired = jwtService.isTokenExpired(token);

        // Then
        assertThat(isExpired).isFalse();
    }

    @Test
    @DisplayName("Debe generar tokens diferentes para diferentes usuarios")
    void shouldGenerateDifferentTokensForDifferentUsers() {
        // Given
        User adminUser = User.builder()
                .id(2L)
                .email("admin@stockchef.com")
                .role(UserRole.ROLE_ADMIN)
                .build();

        // When
        String tokenDeveloper = jwtService.generateToken(testUser);
        String tokenAdmin = jwtService.generateToken(adminUser);

        // Then
        assertThat(tokenDeveloper).isNotEqualTo(tokenAdmin);
        
        // Verificar que cada token contiene la información correcta
        assertThat(jwtService.extractEmail(tokenDeveloper)).isEqualTo("developer@stockchef.com");
        assertThat(jwtService.extractEmail(tokenAdmin)).isEqualTo("admin@stockchef.com");
        assertThat(jwtService.extractRole(tokenDeveloper)).isEqualTo("ROLE_DEVELOPER");
        assertThat(jwtService.extractRole(tokenAdmin)).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Debe incluir claims adicionales en el token")
    void shouldIncludeAdditionalClaimsInToken() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        String userId = jwtService.extractClaim(token, "userId");
        String fullName = jwtService.extractClaim(token, "fullName");

        // Then
        assertThat(userId).isEqualTo("1");
        assertThat(fullName).isEqualTo("Super Developer");
    }
}