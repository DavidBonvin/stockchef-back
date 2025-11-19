package com.stockchef.stockchefback.controller;

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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
// import static org.mockito.Mockito.*; // Removed unused import
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests TDD para AuthController
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

    private User testDeveloper;
    private User testAdmin;
    private User testChef;
    private User testEmployee;

    @BeforeEach
    void setUp() {
        // Limpiar datos
        userRepository.deleteAll();

        // Crear usuarios de prueba
        testDeveloper = createAndSaveUser(
                "developer@stockchef.com", "devpass123",
                "Super", "Admin", UserRole.ROLE_DEVELOPER
        );

        testAdmin = createAndSaveUser(
                "admin@stockchef.com", "adminpass123",
                "John", "Administrator", UserRole.ROLE_ADMIN
        );

        testChef = createAndSaveUser(
                "chef@stockchef.com", "chefpass123",
                "Maria", "Rodriguez", UserRole.ROLE_CHEF
        );

        testEmployee = createAndSaveUser(
                "employee@stockchef.com", "emppass123",
                "Pedro", "Martinez", UserRole.ROLE_EMPLOYEE
        );
    }

    @Test
    @DisplayName("POST /auth/login - Debe autenticar developer exitosamente")
    void shouldAuthenticateDeveloperSuccessfully() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest(
                "developer@stockchef.com", 
                "devpass123"
        );

        // When & Then
        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("developer@stockchef.com"))
                .andExpect(jsonPath("$.fullName").value("Super Admin"))
                .andExpect(jsonPath("$.role").value("ROLE_DEVELOPER"))
                .andExpect(jsonPath("$.expiresIn").exists())
                .andReturn();

        // Verificar que el token sea válido
        LoginResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                LoginResponse.class
        );

        assertThat(jwtService.extractEmail(response.token())).isEqualTo("developer@stockchef.com");
        assertThat(jwtService.extractRole(response.token())).isEqualTo("ROLE_DEVELOPER");
        assertThat(jwtService.isTokenValid(response.token(), testDeveloper)).isTrue();
    }

    @Test
    @DisplayName("POST /auth/login - Debe autenticar admin exitosamente")
    void shouldAuthenticateAdminSuccessfully() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest(
                "admin@stockchef.com", 
                "adminpass123"
        );

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("admin@stockchef.com"))
                .andExpect(jsonPath("$.fullName").value("John Administrator"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("POST /auth/login - Debe autenticar chef exitosamente")
    void shouldAuthenticateChefSuccessfully() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest(
                "chef@stockchef.com", 
                "chefpass123"
        );

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_CHEF"));
    }

    @Test
    @DisplayName("POST /auth/login - Debe autenticar employee exitosamente")
    void shouldAuthenticateEmployeeSuccessfully() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest(
                "employee@stockchef.com", 
                "emppass123"
        );

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_EMPLOYEE"));
    }

    @Test
    @DisplayName("POST /auth/login - Debe rechazar credenciales inválidas")
    void shouldRejectInvalidCredentials() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest(
                "developer@stockchef.com", 
                "wrongpassword"
        );

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /auth/login - Debe rechazar email inexistente")
    void shouldRejectNonExistentEmail() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest(
                "nonexistent@stockchef.com", 
                "anypassword"
        );

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /auth/login - Debe validar formato de request")
    void shouldValidateRequestFormat() throws Exception {
        // Given - email inválido
        LoginRequest invalidEmailRequest = new LoginRequest(
                "invalid-email", 
                "password123"
        );

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEmailRequest)))
                .andExpect(status().isBadRequest());

        // Given - password muy corta
        LoginRequest shortPasswordRequest = new LoginRequest(
                "user@stockchef.com", 
                "123"
        );

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shortPasswordRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/login - Debe rechazar usuario inactivo")
    void shouldRejectInactiveUser() throws Exception {
        // Given - crear usuario inactivo
        User inactiveUser = User.builder()
                .email("inactive@stockchef.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("Inactive")
                .lastName("User")
                .role(UserRole.ROLE_EMPLOYEE)
                .isActive(false)
                .build();
        userRepository.save(inactiveUser);

        LoginRequest loginRequest = new LoginRequest(
                "inactive@stockchef.com", 
                "password123"
        );

        // When & Then
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    private User createAndSaveUser(String email, String password, String firstName, 
                                  String lastName, UserRole role) {
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .isActive(true)
                .build();
        return userRepository.save(user);
    }
}