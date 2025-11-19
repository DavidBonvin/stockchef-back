package com.stockchef.stockchefback.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockchef.stockchefback.dto.user.RegisterRequest;
import com.stockchef.stockchefback.dto.user.UserResponse;
import com.stockchef.stockchefback.model.UserRole;
import com.stockchef.stockchefback.service.UserService;
import com.stockchef.stockchefback.testutil.TestUuidHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests TDD pour UserController - Gestion des utilisateurs
 * RED -> GREEN -> REFACTOR
 */
@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@DisplayName("Tests TDD - UserController")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterRequest validRegisterRequest;
    private UserResponse expectedUserResponse;

    @BeforeEach
    void setUp() {
        // Données de test valides
        validRegisterRequest = new RegisterRequest(
                "nouveau@stockchef.com",
                "password123",
                "Jean",
                "Dupont"
        );

        expectedUserResponse = new UserResponse(
                TestUuidHelper.USER_1_UUID,
                "nouveau@stockchef.com",
                "Jean",
                "Dupont",
                "Jean Dupont",
                UserRole.ROLE_EMPLOYEE,
                UserRole.ROLE_EMPLOYEE, // effectiveRole = role si actif
                true,
                LocalDateTime.now(),
                null,
                "system"
        );
    }

    @Test
    @DisplayName("RED: POST /api/users/register - Enregistrement utilisateur valide")
    void shouldRegisterNewUser_WhenValidRequest() throws Exception {
        // Given
        when(userService.registerNewUser(any(RegisterRequest.class)))
                .thenReturn(expectedUserResponse);

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("nouveau@stockchef.com"))
                .andExpect(jsonPath("$.firstName").value("Jean"))
                .andExpect(jsonPath("$.lastName").value("Dupont"))
                .andExpect(jsonPath("$.fullName").value("Jean Dupont"))
                .andExpect(jsonPath("$.role").value("ROLE_EMPLOYEE"))
                .andExpect(jsonPath("$.effectiveRole").value("ROLE_EMPLOYEE"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.id").value(TestUuidHelper.USER_1_UUID))
                .andReturn();

        // Vérifier la réponse complète
        String responseJson = result.getResponse().getContentAsString();
        UserResponse actualResponse = objectMapper.readValue(responseJson, UserResponse.class);
        
        assertThat(actualResponse.email()).isEqualTo(validRegisterRequest.email());
        assertThat(actualResponse.firstName()).isEqualTo(validRegisterRequest.firstName());
        assertThat(actualResponse.lastName()).isEqualTo(validRegisterRequest.lastName());
        assertThat(actualResponse.role()).isEqualTo(UserRole.ROLE_EMPLOYEE);
        assertThat(actualResponse.isActive()).isTrue();
    }

    @Test
    @DisplayName("RED: POST /api/users/register - Email déjà existant")
    void shouldReturnConflict_WhenEmailAlreadyExists() throws Exception {
        // Given
        when(userService.registerNewUser(any(RegisterRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("Email déjà utilisé"));

        // When & Then
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email déjà utilisé"));
    }

    @Test
    @DisplayName("RED: POST /api/users/register - Données invalides")
    void shouldReturnBadRequest_WhenInvalidData() throws Exception {
        // Given - Email invalide
        RegisterRequest invalidRequest = new RegisterRequest(
                "email-invalide",
                "123", // trop court
                "", // prénom vide
                "Dupont"
        );

        // When & Then
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[*].field").exists())
                .andExpect(jsonPath("$.errors[*].message").exists());
    }

    @Test
    @DisplayName("RED: POST /api/users/register - Mot de passe trop court")
    void shouldReturnBadRequest_WhenPasswordTooShort() throws Exception {
        // Given
        RegisterRequest shortPasswordRequest = new RegisterRequest(
                "test@stockchef.com",
                "12345", // 5 caractères, minimum requis: 6
                "Jean",
                "Dupont"
        );

        // When & Then
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shortPasswordRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'password')]").exists());
    }

    @Test
    @DisplayName("RED: POST /api/users/register - Tous les champs requis")
    void shouldReturnBadRequest_WhenRequiredFieldsAreMissing() throws Exception {
        // Given - Tous les champs vides
        RegisterRequest emptyRequest = new RegisterRequest("", "", "", "");

        // When & Then
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    @DisplayName("RED: POST /api/users/register - Nouvel utilisateur a rôle EMPLOYEE par défaut")
    void shouldAssignEmployeeRole_WhenRegisteringNewUser() throws Exception {
        // Given
        when(userService.registerNewUser(any(RegisterRequest.class)))
                .thenReturn(expectedUserResponse);

        // When & Then
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ROLE_EMPLOYEE"))
                .andExpect(jsonPath("$.effectiveRole").value("ROLE_EMPLOYEE"));
    }

    @Test
    @DisplayName("RED: POST /api/users/register - Utilisateur créé est actif par défaut")
    void shouldCreateActiveUser_ByDefault() throws Exception {
        // Given
        when(userService.registerNewUser(any(RegisterRequest.class)))
                .thenReturn(expectedUserResponse);

        // When & Then
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isActive").value(true));
    }
}

/**
 * Exception personnalisée pour email déjà existant
 * (sera créée dans l'implémentation)
 */
class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
