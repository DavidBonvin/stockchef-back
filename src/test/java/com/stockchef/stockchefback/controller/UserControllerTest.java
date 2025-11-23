package com.stockchef.stockchefback.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockchef.stockchefback.dto.user.RegisterRequest;
import com.stockchef.stockchefback.dto.user.UserResponse;
import com.stockchef.stockchefback.model.UserRole;
import com.stockchef.stockchefback.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests pour UserController - Gestion des utilisateurs
 * Utilise SpringBootTest avec MockMvc pour tester avec contexte complet.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("User Controller Integration Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private RegisterRequest validRegisterRequest;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        
        // Données de test valides
        validRegisterRequest = new RegisterRequest(
                "nouveau@stockchef.com",
                "password123",
                "Jean",
                "Dupont"
        );
    }

    @Test
    @DisplayName("Should register new user when valid request")
    void shouldRegisterNewUser_WhenValidRequest() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(post("/users/register")
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
                .andExpect(jsonPath("$.id").exists())
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
    @DisplayName("Should return conflict when email already exists")
    void shouldReturnConflict_WhenEmailAlreadyExists() throws Exception {
        // Given - crear usuario primero
        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isCreated());

        // When & Then - intentar crear usuario con mismo email
        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should return bad request when invalid data")
    void shouldReturnBadRequest_WhenInvalidData() throws Exception {
        // Given - Email invalide
        RegisterRequest invalidRequest = new RegisterRequest(
                "email-invalide",
                "123", // trop court
                "", // prénom vide
                "Dupont"
        );

        // When & Then
        mockMvc.perform(post("/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
