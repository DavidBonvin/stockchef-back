package com.stockchef.stockchefback.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockchef.stockchefback.dto.auth.LoginRequest;
import com.stockchef.stockchefback.model.UserRole;
import com.stockchef.stockchefback.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.stockchef.stockchefback.repository.UserRepository;
import com.stockchef.stockchefback.service.AuthService;
import com.stockchef.stockchefback.model.User;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitarios simples para AuthController
 */
@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
@DisplayName("Auth Controller Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AuthService authService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("developer@stockchef.com");
        mockUser.setPassword("$2a$10$encodedPassword");
        mockUser.setFirstName("Super");
        mockUser.setLastName("Admin");
        mockUser.setRole(UserRole.ROLE_DEVELOPER);
        mockUser.setIsActive(true);
        mockUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("POST /auth/login - Debe autenticar usuario válido exitosamente")
    void shouldAuthenticateValidUserSuccessfully() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("developer@stockchef.com", "devpass123");
        
        when(userRepository.findByEmail("developer@stockchef.com"))
                .thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("devpass123", mockUser.getPassword()))
                .thenReturn(true);
        when(jwtService.generateToken(mockUser))
                .thenReturn("mock-jwt-token");

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("developer@stockchef.com"))
                .andExpect(jsonPath("$.fullName").value("Super Admin"))
                .andExpect(jsonPath("$.role").value("ROLE_DEVELOPER"))
                .andExpect(jsonPath("$.token").value("mock-jwt-token"));
    }

    @Test
    @DisplayName("POST /auth/login - Debe rechazar credenciales inválidas")
    void shouldRejectInvalidCredentials() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("developer@stockchef.com", "wrongpass");
        
        when(userRepository.findByEmail("developer@stockchef.com"))
                .thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongpass", mockUser.getPassword()))
                .thenReturn(false);

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /auth/login - Debe rechazar email no existente")
    void shouldRejectNonExistentEmail() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("nonexistent@stockchef.com", "anypass");
        
        when(userRepository.findByEmail("nonexistent@stockchef.com"))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /auth/login - Debe rechazar usuario inactivo")
    void shouldRejectInactiveUser() throws Exception {
        // Given
        mockUser.setIsActive(false);
        LoginRequest loginRequest = new LoginRequest("developer@stockchef.com", "devpass123");
        
        when(userRepository.findByEmail("developer@stockchef.com"))
                .thenReturn(Optional.of(mockUser));

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /auth/login - Debe validar formato de requête")
    void shouldValidateRequestFormat() throws Exception {
        // Given - invalid request with missing password
        String invalidRequest = """
                {
                    "email": "test@example.com"
                }
                """;

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }
}