package com.stockchef.stockchefback.service;

import com.stockchef.stockchefback.model.User;
import com.stockchef.stockchefback.model.UserRole;
import com.stockchef.stockchefback.testutil.TestUuidHelper;
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
                .id(TestUuidHelper.USER_1_UUID)
                .email("developer@stockchef.com")
                .firstName("Super")
                .lastName("Developer")
                .role(UserRole.ROLE_DEVELOPER)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Doit générer token JWT valide")
    void shouldGenerateValidJwtToken() {
        // When
        String token = jwtService.generateToken(testUser);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT tiene 3 partes separadas por puntos
    }

    @Test
    @DisplayName("Doit extraire email du token")
    void shouldExtractEmailFromToken() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        String extractedEmail = jwtService.extractEmail(token);

        // Then
        assertThat(extractedEmail).isEqualTo("developer@stockchef.com");
    }

    @Test
    @DisplayName("Doit extraire rôle du token")
    void shouldExtractRoleFromToken() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        String extractedRole = jwtService.extractRole(token);

        // Then
        assertThat(extractedRole).isEqualTo("ROLE_DEVELOPER");
    }

    @Test
    @DisplayName("Doit valider token correct")
    void shouldValidateCorrectToken() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        boolean isValid = jwtService.isTokenValid(token, testUser);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Doit rejeter token invalide")
    void shouldRejectInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtService.isTokenValid(invalidToken, testUser);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Doit rejeter token pour utilisateur différent")
    void shouldRejectTokenForDifferentUser() {
        // Given
        String token = jwtService.generateToken(testUser);
        
        User differentUser = User.builder()
                .id(TestUuidHelper.USER_2_UUID)
                .email("admin@stockchef.com")
                .role(UserRole.ROLE_ADMIN)
                .build();

        // When
        boolean isValid = jwtService.isTokenValid(token, differentUser);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Doit vérifier que token n'est pas expiré")
    void shouldVerifyTokenIsNotExpired() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        boolean isExpired = jwtService.isTokenExpired(token);

        // Then
        assertThat(isExpired).isFalse();
    }

    @Test
    @DisplayName("Doit générer tokens différents pour utilisateurs différents")
    void shouldGenerateDifferentTokensForDifferentUsers() {
        // Given
        User adminUser = User.builder()
                .id(TestUuidHelper.USER_2_UUID)
                .email("admin@stockchef.com")
                .role(UserRole.ROLE_ADMIN)
                .build();

        // When
        String tokenDeveloper = jwtService.generateToken(testUser);
        String tokenAdmin = jwtService.generateToken(adminUser);

        // Then
        assertThat(tokenDeveloper).isNotEqualTo(tokenAdmin);
        
        // Vérifier que chaque token contient l'information correcte
        assertThat(jwtService.extractEmail(tokenDeveloper)).isEqualTo("developer@stockchef.com");
        assertThat(jwtService.extractEmail(tokenAdmin)).isEqualTo("admin@stockchef.com");
        assertThat(jwtService.extractRole(tokenDeveloper)).isEqualTo("ROLE_DEVELOPER");
        assertThat(jwtService.extractRole(tokenAdmin)).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Doit inclure claims additionnels dans le token")
    void shouldIncludeAdditionalClaimsInToken() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        String userId = jwtService.extractClaim(token, "userId");
        String fullName = jwtService.extractClaim(token, "fullName");

        // Then
        assertThat(userId).isEqualTo(TestUuidHelper.USER_1_UUID);
        assertThat(fullName).isEqualTo("Super Developer");
    }
}