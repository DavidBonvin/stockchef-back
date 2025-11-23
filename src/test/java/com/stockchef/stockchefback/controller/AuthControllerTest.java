package com.stockchef.stockchefback.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockchef.stockchefback.dto.auth.LoginRequest;
import com.stockchef.stockchefback.model.User;
import com.stockchef.stockchefback.model.UserRole;
import com.stockchef.stockchefback.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests pour AuthController
 * 
 * Tests pour l'authentification des utilisateurs avec les endpoints REST.
 * Utilise SpringBootTest avec MockMvc pour tester avec contexte complet.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Auth Controller Integration Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        
        // Créer un utilisateur de test
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(UserRole.ROLE_CHEF);
        testUser.setIsActive(true);
        testUser.setCreatedAt(LocalDateTime.now());
        
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("Should authenticate valid user successfully")
    void shouldAuthenticateValidUserSuccessfully() throws Exception {
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password123");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.fullName").value("Test User"))
                .andExpect(jsonPath("$.role").value("ROLE_CHEF"))
                .andExpect(jsonPath("$.expiresIn").value(86400000L));
    }

    @Test
    @DisplayName("Should reject invalid credentials")
    void shouldRejectInvalidCredentials() throws Exception {
        LoginRequest loginRequest = new LoginRequest("test@example.com", "wrongpassword");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject non-existent email")
    void shouldRejectNonExistentEmail() throws Exception {
        LoginRequest loginRequest = new LoginRequest("nonexistent@example.com", "password123");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject inactive user")
    void shouldRejectInactiveUser() throws Exception {
        // Désactiver l'utilisateur de test
        testUser.setIsActive(false);
        userRepository.save(testUser);

        LoginRequest loginRequest = new LoginRequest("test@example.com", "password123");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should validate request format")
    void shouldValidateRequestFormat() throws Exception {
        // Test avec email invalide
        LoginRequest invalidRequest = new LoginRequest("invalid-email", "password");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}