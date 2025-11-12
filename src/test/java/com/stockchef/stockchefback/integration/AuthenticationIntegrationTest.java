package com.stockchef.stockchefback.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockchef.stockchefback.dto.auth.LoginRequest;
import com.stockchef.stockchefback.dto.auth.LoginResponse;
import com.stockchef.stockchefback.model.User;
import com.stockchef.stockchefback.model.UserRole;
import com.stockchef.stockchefback.repository.UserRepository;
import com.stockchef.stockchefback.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración end-to-end para el sistema de autenticación
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("🔐 Authentication End-to-End Integration Tests")
class AuthenticationIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        
        userRepository.deleteAll();
        createTestUsers();
    }

    @Test
    @DisplayName("🚀 Should complete full authentication flow for DEVELOPER")
    void shouldCompleteFullAuthenticationFlowForDeveloper() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest(
                "developer@stockchef.com", 
                "devpass123"
        );

        // When - Realizar login
        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("developer@stockchef.com"))
                .andExpect(jsonPath("$.fullName").value("Super Admin"))
                .andExpect(jsonPath("$.role").value("ROLE_DEVELOPER"))
                .andExpect(jsonPath("$.expiresIn").value(86400000))
                .andReturn();

        // Then - Verificar que el token sea válido y extraer información
        LoginResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                LoginResponse.class
        );

        // Verificar token structure
        assertThat(response.token()).isNotNull();
        assertThat(response.token().split("\\.")).hasSize(3); // JWT format

        // Verificar claims del token
        assertThat(jwtService.extractEmail(response.token())).isEqualTo("developer@stockchef.com");
        assertThat(jwtService.extractRole(response.token())).isEqualTo("ROLE_DEVELOPER");
        
        // Verificar que el token es válido para el usuario
        User user = userRepository.findByEmail("developer@stockchef.com").orElseThrow();
        assertThat(jwtService.isTokenValid(response.token(), user)).isTrue();
        assertThat(jwtService.isTokenExpired(response.token())).isFalse();

        // Verificar claims adicionales
        assertThat(jwtService.extractClaim(response.token(), "userId")).isEqualTo(user.getId().toString());
        assertThat(jwtService.extractClaim(response.token(), "fullName")).isEqualTo("Super Admin");
    }

    @Test
    @DisplayName("👤 Should authenticate all user roles successfully")
    void shouldAuthenticateAllUserRolesSuccessfully() throws Exception {
        // Test ADMIN
        testUserAuthentication("admin@stockchef.com", "adminpass123", "ROLE_ADMIN", "John Administrator");

        // Test CHEF
        testUserAuthentication("chef@stockchef.com", "chefpass123", "ROLE_CHEF", "Maria Rodriguez");

        // Test EMPLOYEE
        testUserAuthentication("employee@stockchef.com", "emppass123", "ROLE_EMPLOYEE", "Pedro Martinez");
    }

    @Test
    @DisplayName("🚫 Should reject invalid credentials")
    void shouldRejectInvalidCredentials() throws Exception {
        // Wrong password
        LoginRequest wrongPassword = new LoginRequest("developer@stockchef.com", "wrongpass");
        
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongPassword)))
                .andExpect(status().isUnauthorized());

        // Non-existent user
        LoginRequest nonExistentUser = new LoginRequest("notfound@stockchef.com", "anypass");
        
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nonExistentUser)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("⚠️ Should reject inactive user")
    void shouldRejectInactiveUser() throws Exception {
        // Create inactive user
        User inactiveUser = User.builder()
                .email("inactive@stockchef.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("Inactive")
                .lastName("User")
                .role(UserRole.ROLE_EMPLOYEE)
                .isActive(false)
                .build();
        userRepository.save(inactiveUser);

        LoginRequest loginRequest = new LoginRequest("inactive@stockchef.com", "password123");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("📋 Should validate request format")
    void shouldValidateRequestFormat() throws Exception {
        // Invalid email format
        LoginRequest invalidEmail = new LoginRequest("invalid-email", "password123");
        
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEmail)))
                .andExpect(status().isBadRequest());

        // Short password
        LoginRequest shortPassword = new LoginRequest("user@stockchef.com", "123");
        
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shortPassword)))
                .andExpect(status().isBadRequest());

        // Empty email
        LoginRequest emptyEmail = new LoginRequest("", "password123");
        
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyEmail)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("🔄 Should handle multiple consecutive authentications")
    void shouldHandleMultipleConsecutiveAuthentications() throws Exception {
        LoginRequest loginRequest = new LoginRequest("developer@stockchef.com", "devpass123");

        // Perform multiple logins
        for (int i = 0; i < 3; i++) {
            MvcResult result = mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            LoginResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    LoginResponse.class
            );

            // Each token should be unique but valid
            assertThat(response.token()).isNotNull();
            assertThat(jwtService.extractEmail(response.token())).isEqualTo("developer@stockchef.com");
        }
    }

    @Test
    @DisplayName("🔍 Should extract all custom claims correctly")
    void shouldExtractAllCustomClaimsCorrectly() throws Exception {
        // Login as developer
        LoginRequest loginRequest = new LoginRequest("developer@stockchef.com", "devpass123");
        
        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                LoginResponse.class
        );

        String token = response.token();

        // Verify all custom claims
        User user = userRepository.findByEmail("developer@stockchef.com").orElseThrow();
        assertThat(jwtService.extractClaim(token, "userId")).isEqualTo(user.getId().toString());
        assertThat(jwtService.extractClaim(token, "role")).isEqualTo("ROLE_DEVELOPER");
        assertThat(jwtService.extractClaim(token, "fullName")).isEqualTo("Super Admin");
        assertThat(jwtService.extractEmail(token)).isEqualTo("developer@stockchef.com");
        assertThat(jwtService.extractRole(token)).isEqualTo("ROLE_DEVELOPER");
    }

    // Helper methods
    private void testUserAuthentication(String email, String password, String expectedRole, String expectedFullName) throws Exception {
        LoginRequest loginRequest = new LoginRequest(email, password);

        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value(expectedRole))
                .andExpect(jsonPath("$.fullName").value(expectedFullName))
                .andReturn();

        LoginResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                LoginResponse.class
        );

        // Verify token validity
        User user = userRepository.findByEmail(email).orElseThrow();
        assertThat(jwtService.isTokenValid(response.token(), user)).isTrue();
    }

    private void createTestUsers() {
        // Developer
        userRepository.save(User.builder()
                .email("developer@stockchef.com")
                .password(passwordEncoder.encode("devpass123"))
                .firstName("Super")
                .lastName("Admin")
                .role(UserRole.ROLE_DEVELOPER)
                .isActive(true)
                .build());

        // Admin
        userRepository.save(User.builder()
                .email("admin@stockchef.com")
                .password(passwordEncoder.encode("adminpass123"))
                .firstName("John")
                .lastName("Administrator")
                .role(UserRole.ROLE_ADMIN)
                .isActive(true)
                .build());

        // Chef
        userRepository.save(User.builder()
                .email("chef@stockchef.com")
                .password(passwordEncoder.encode("chefpass123"))
                .firstName("Maria")
                .lastName("Rodriguez")
                .role(UserRole.ROLE_CHEF)
                .isActive(true)
                .build());

        // Employee
        userRepository.save(User.builder()
                .email("employee@stockchef.com")
                .password(passwordEncoder.encode("emppass123"))
                .firstName("Pedro")
                .lastName("Martinez")
                .role(UserRole.ROLE_EMPLOYEE)
                .isActive(true)
                .build());
    }
}