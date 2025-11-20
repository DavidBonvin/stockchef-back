package com.stockchef.stockchefback.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockchef.stockchefback.dto.auth.ChangePasswordRequest;
import com.stockchef.stockchefback.dto.auth.ForgotPasswordRequest;
import com.stockchef.stockchefback.dto.auth.RefreshTokenRequest;
import com.stockchef.stockchefback.dto.auth.ResetPasswordRequest;
import com.stockchef.stockchefback.exception.InvalidPasswordException;
import com.stockchef.stockchefback.exception.InsufficientPermissionsException;
import com.stockchef.stockchefback.exception.UserNotFoundException;
import com.stockchef.stockchefback.exception.InvalidTokenException;
import com.stockchef.stockchefback.service.AuthService;
import com.stockchef.stockchefback.service.AuthService.TokenResponse;
import com.stockchef.stockchefback.service.UserService;
import com.stockchef.stockchefback.testutil.TestUuidHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests TDD para endpoints de gestión de contraseñas y autenticación
 * 
 * Endpoints probados:
 * - PUT /users/{id}/password - Changement mot de passe (propre utilisateur + ADMIN)
 * - POST /users/{id}/reset-password - Reset de contraseña (solo ADMIN)
 * - POST /auth/refresh - Renovar token JWT
 * - POST /auth/logout - Invalidar token (blacklist)
 * - POST /users/change-password - Cambio de contraseña personal
 * - POST /users/forgot-password - Solicitar reset de contraseña
 */
@SpringBootTest
@AutoConfigureWebMvc
public class UserPasswordTDDTest {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthService authService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        objectMapper = new ObjectMapper();
    }

    // ==================== PUT /users/{id}/password Tests ====================

    @Test
    @WithMockUser(username = "admin@stockchef.com", roles = {"ADMIN"})
    @DisplayName("PUT /users/{id}/password - ADMIN can change any user password")
    void changeUserPassword_WhenAdmin_ShouldReturnSuccess() throws Exception {
        // GIVEN: ADMIN changing EMPLOYEE password
        ChangePasswordRequest request = new ChangePasswordRequest(
                "oldPassword123!",
                "newPassword123!",
                "newPassword123!"
        );

        doNothing().when(userService).changeUserPassword(
                eq(TestUuidHelper.EMPLOYEE_UUID), 
                eq(request), 
                eq("admin@stockchef.com")
        );

        // WHEN & THEN: ADMIN changes another user's password
        mockMvc.perform(put("/users/{id}/password", TestUuidHelper.EMPLOYEE_UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "employee@stockchef.com", roles = {"EMPLOYEE"})
    @DisplayName("PUT /users/{id}/password - User can change own password")
    void changeUserPassword_WhenOwnPassword_ShouldReturnSuccess() throws Exception {
        // GIVEN: User changing own password
        ChangePasswordRequest request = new ChangePasswordRequest(
                "oldPassword123!",
                "newPassword123!",
                "newPassword123!"
        );

        doNothing().when(userService).changeUserPassword(
                eq(TestUuidHelper.EMPLOYEE_UUID), 
                eq(request), 
                eq("employee@stockchef.com")
        );

        // WHEN & THEN: User changes own password
        mockMvc.perform(put("/users/{id}/password", TestUuidHelper.EMPLOYEE_UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "employee@stockchef.com", roles = {"EMPLOYEE"})
    @DisplayName("PUT /users/{id}/password - EMPLOYEE cannot change other user's password")
    void changeUserPassword_WhenEmployeeChangingOthers_ShouldReturn403() throws Exception {
        // GIVEN: EMPLOYEE trying to change ADMIN password
        ChangePasswordRequest request = new ChangePasswordRequest(
                "oldPassword123!",
                "newPassword123!",
                "newPassword123!"
        );

        doThrow(new InsufficientPermissionsException("No tienes permisos para cambiar esta contraseña"))
                .when(userService).changeUserPassword(
                        eq(TestUuidHelper.ADMIN_UUID), 
                        eq(request), 
                        eq("employee@stockchef.com")
                );

        // WHEN & THEN: Should return 403
        mockMvc.perform(put("/users/{id}/password", TestUuidHelper.ADMIN_UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    // ==================== POST /users/{id}/reset-password Tests ====================

    @Test
    @WithMockUser(username = "admin@stockchef.com", roles = {"ADMIN"})
    @DisplayName("POST /users/{id}/reset-password - ADMIN can reset any user password")
    void resetUserPassword_WhenAdmin_ShouldReturnSuccess() throws Exception {
        // GIVEN: ADMIN resetting EMPLOYEE password
        ResetPasswordRequest request = new ResetPasswordRequest("newPassword123!");

        doNothing().when(userService).resetUserPassword(
                eq(TestUuidHelper.EMPLOYEE_UUID), 
                eq(request), 
                eq("admin@stockchef.com")
        );

        // WHEN & THEN: Should return 200
        mockMvc.perform(post("/users/{id}/reset-password", TestUuidHelper.EMPLOYEE_UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "developer@stockchef.com", roles = {"DEVELOPER"})
    @DisplayName("POST /users/{id}/reset-password - DEVELOPER cannot reset passwords")
    void resetUserPassword_WhenDeveloper_ShouldReturn403() throws Exception {
        // GIVEN: DEVELOPER trying to reset password
        ResetPasswordRequest request = new ResetPasswordRequest("newPassword123!");

        doThrow(new InsufficientPermissionsException("Seuls les administrateurs peuvent réinitialiser les mots de passe"))
                .when(userService).resetUserPassword(
                        eq(TestUuidHelper.EMPLOYEE_UUID), 
                        eq(request), 
                        eq("developer@stockchef.com")
                );

        // WHEN & THEN: Should return 403
        mockMvc.perform(post("/users/{id}/reset-password", TestUuidHelper.EMPLOYEE_UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    // ==================== POST /auth/refresh Tests ====================

    @Test
    @DisplayName("POST /auth/refresh - Valid refresh token should return new tokens")
    void refreshToken_WhenValidRefreshToken_ShouldReturnNewTokens() throws Exception {
        // GIVEN: Valid refresh token request
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token-12345");
        
        TokenResponse mockResponse = new TokenResponse(
                "new-access-token-123", 
                "new-refresh-token-456", 
                86400000L
        );

        when(authService.refreshToken(eq(request))).thenReturn(mockResponse);

        // WHEN & THEN: Should return new tokens
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token-123"));
    }

    @Test
    @DisplayName("POST /auth/refresh - Invalid refresh token should return 401")
    void refreshToken_WhenInvalidRefreshToken_ShouldReturn401() throws Exception {
        // GIVEN: Invalid refresh token request
        RefreshTokenRequest request = new RefreshTokenRequest("invalid-refresh-token");

        when(authService.refreshToken(eq(request)))
                .thenThrow(new InvalidTokenException("Token inválido"));

        // WHEN & THEN: Should return 401
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    // ==================== POST /auth/logout Tests ====================

    @Test
    @WithMockUser(username = "employee@stockchef.com", roles = {"EMPLOYEE"})
    @DisplayName("POST /auth/logout - Authenticated user should be able to logout")
    void logout_WhenAuthenticated_ShouldInvalidateToken() throws Exception {
        // GIVEN: Authenticated user
        doNothing().when(authService).invalidateToken("employee@stockchef.com");

        // WHEN & THEN: Should logout successfully
        mockMvc.perform(post("/auth/logout"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /auth/logout - Not authenticated should return 403")
    void logout_WhenNotAuthenticated_ShouldReturn403() throws Exception {
        // WHEN & THEN: Should return 403
        mockMvc.perform(post("/auth/logout"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    // ==================== POST /users/change-password Tests ====================

    @Test
    @WithMockUser(username = "employee@stockchef.com", roles = {"EMPLOYEE"})
    @DisplayName("POST /users/change-password - Valid data should change password")
    void changePersonalPassword_WhenValidData_ShouldReturnSuccess() throws Exception {
        // GIVEN: Valid password change request
        ChangePasswordRequest request = new ChangePasswordRequest(
                "currentPassword123!",
                "newPassword123!",
                "newPassword123!"
        );

        doNothing().when(userService).changePersonalPassword("employee@stockchef.com", request);

        // WHEN & THEN: Should change password successfully
        mockMvc.perform(post("/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "employee@stockchef.com", roles = {"EMPLOYEE"})
    @DisplayName("POST /users/change-password - Password mismatch should return 400")
    void changePersonalPassword_WhenPasswordMismatch_ShouldReturn400() throws Exception {
        // GIVEN: Mismatched new passwords
        ChangePasswordRequest request = new ChangePasswordRequest(
                "currentPassword123!",
                "newPassword123!",
                "differentPassword123!"
        );

        doThrow(new InvalidPasswordException("Las contraseñas nuevas no coinciden"))
                .when(userService).changePersonalPassword("employee@stockchef.com", request);

        // WHEN & THEN: Should return 400
        mockMvc.perform(post("/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // ==================== POST /users/forgot-password Tests ====================

    @Test
    @DisplayName("POST /users/forgot-password - Valid email should process request")
    void forgotPassword_WhenValidEmail_ShouldReturnSuccess() throws Exception {
        // GIVEN: Valid email for password reset
        ForgotPasswordRequest request = new ForgotPasswordRequest("employee@stockchef.com");

        doNothing().when(userService).requestPasswordReset("employee@stockchef.com");

        // WHEN & THEN: Should return 200 (always for security)
        mockMvc.perform(post("/users/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /users/forgot-password - Non-existent email should still return 200 for security")
    void forgotPassword_WhenEmailNotFound_ShouldReturn200ForSecurity() throws Exception {
        // GIVEN: Non-existent email
        ForgotPasswordRequest request = new ForgotPasswordRequest("nonexistent@stockchef.com");

        doThrow(new UserNotFoundException("Utilisateur non trouvé"))
                .when(userService).requestPasswordReset("nonexistent@stockchef.com");

        // WHEN & THEN: Should return 200 for security reasons
        mockMvc.perform(post("/users/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());  // Note: Returns 200 for security
    }

    @Test
    @DisplayName("POST /users/forgot-password - Invalid email format should return 400")
    void forgotPassword_WhenInvalidEmail_ShouldReturn400() throws Exception {
        // GIVEN: Invalid email format
        ForgotPasswordRequest request = new ForgotPasswordRequest("invalid-email-format");

        // WHEN & THEN: Should return 400 due to validation
        mockMvc.perform(post("/users/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}