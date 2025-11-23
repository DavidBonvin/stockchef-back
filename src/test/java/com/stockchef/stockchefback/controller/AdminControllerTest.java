package com.stockchef.stockchefback.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockchef.stockchefback.dto.user.UpdateUserRoleRequest;
import com.stockchef.stockchefback.dto.user.UpdateUserStatusRequest;
import com.stockchef.stockchefback.model.User;
import com.stockchef.stockchefback.model.UserRole;
import com.stockchef.stockchefback.repository.UserRepository;
import com.stockchef.stockchefback.service.UserService;
import com.stockchef.stockchefback.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests pour AdminController - Gestion administrative des utilisateurs  
 * Utilise SpringBootTest avec MockMvc pour tester avec contexte complet.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Admin Controller Integration Tests")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    private User testEmployee;
    private User testChef;
    private User testAdmin;
    private User testAdminUser;  // Usuario admin para tests
    private User testDeveloperUser; // Usuario developer para tests

    private String adminToken;
    private String developerToken;
    private String employeeToken;

    @BeforeEach
    void setUp() {
        // Limpiar datos previos
        userRepository.deleteAll();
        
        // Crear usuarios de prueba directamente en la BD
        testEmployee = User.builder()
                .email("employee.test@stockchef.com")
                .firstName("Test")
                .lastName("Employee")
                .password("$2a$10$hashedPassword")
                .role(UserRole.ROLE_EMPLOYEE)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy("system")
                .build();
        testEmployee = userRepository.save(testEmployee);

        testChef = User.builder()
                .email("chef.test@stockchef.com")
                .firstName("Test")
                .lastName("Chef")
                .password("$2a$10$hashedPassword")
                .role(UserRole.ROLE_CHEF)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy("system")
                .build();
        testChef = userRepository.save(testChef);
        
        testAdmin = User.builder()
                .email("admin.test@stockchef.com")
                .firstName("Test")
                .lastName("Admin")
                .password("$2a$10$hashedPassword")
                .role(UserRole.ROLE_ADMIN)
                .isActive(false) // Inicialmente inactivo para probar reactivación
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy("system")
                .build();
        testAdmin = userRepository.save(testAdmin);

        // Usuario admin para hacer las peticiones
        testAdminUser = User.builder()
                .email("admin.requester@stockchef.com")
                .firstName("Admin")
                .lastName("Requester")
                .password("$2a$10$hashedPassword")
                .role(UserRole.ROLE_ADMIN)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy("system")
                .build();
        testAdminUser = userRepository.save(testAdminUser);

        // Usuario developer para hacer las peticiones
        testDeveloperUser = User.builder()
                .email("developer.requester@stockchef.com")
                .firstName("Developer")
                .lastName("Requester")
                .password("$2a$10$hashedPassword")
                .role(UserRole.ROLE_DEVELOPER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy("system")
                .build();
        testDeveloperUser = userRepository.save(testDeveloperUser);
        
        // Generar tokens JWT para los tests
        adminToken = jwtService.generateToken(testAdminUser);
        developerToken = jwtService.generateToken(testDeveloperUser);
        employeeToken = jwtService.generateToken(testEmployee);
    }

    // Tests pour la liste des utilisateurs
    
    @Test
    @DisplayName("Should list all users when admin")
    void shouldListAllUsers_WhenAdmin() throws Exception {
        // When & Then
        mockMvc.perform(get("/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(5))  // 5 usuarios total
                .andExpect(jsonPath("$[0].email").exists())
                .andExpect(jsonPath("$[1].email").exists())
                .andExpect(jsonPath("$[2].email").exists());
    }

    @Test
    @DisplayName("Should list all users when developer")
    void shouldListAllUsers_WhenDeveloper() throws Exception {
        // When & Then
        mockMvc.perform(get("/admin/users")
                        .header("Authorization", "Bearer " + developerToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should deny access when employee")
    void shouldDenyAccess_WhenEmployee() throws Exception {
        // When & Then
        mockMvc.perform(get("/admin/users")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }

    // Tests pour la modification des rôles

    @Test
    @DisplayName("Should update user role when admin")
    void shouldUpdateUserRole_WhenAdmin() throws Exception {
        // Given
        UpdateUserRoleRequest request = new UpdateUserRoleRequest(
                UserRole.ROLE_CHEF, "Promotion méritée"
        );

        // When & Then
        mockMvc.perform(put("/admin/users/" + testEmployee.getId() + "/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.role").value("ROLE_CHEF"))
                .andExpect(jsonPath("$.effectiveRole").value("ROLE_CHEF"));
    }

    @Test
    @DisplayName("Should allow any role creation when developer")
    void shouldAllowAnyRoleCreation_WhenDeveloper() throws Exception {
        // Given
        UpdateUserRoleRequest request = new UpdateUserRoleRequest(
                UserRole.ROLE_ADMIN, "Nuevo administrateur"
        );

        // When & Then
        mockMvc.perform(put("/admin/users/" + testEmployee.getId() + "/role")
                        .header("Authorization", "Bearer " + developerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }

    @Test 
    @DisplayName("Should deny developer role creation when admin")
    void shouldDenyDeveloperRoleCreation_WhenAdmin() throws Exception {
        // Given
        UpdateUserRoleRequest request = new UpdateUserRoleRequest(
                UserRole.ROLE_DEVELOPER, "Tentative non autorisée"
        );

        // When & Then
        mockMvc.perform(put("/admin/users/" + testEmployee.getId() + "/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // Tests pour la gestion du statut actif/inactif

    @Test
    @DisplayName("Should deactivate user when admin")
    void shouldDeactivateUser_WhenAdmin() throws Exception {
        // Given
        UpdateUserStatusRequest request = new UpdateUserStatusRequest(
                false, "Suspension temporaire"
        );

        // When & Then
        mockMvc.perform(put("/admin/users/" + testChef.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isActive").value(false))
                .andExpect(jsonPath("$.role").value("ROLE_CHEF"))
                .andExpect(jsonPath("$.effectiveRole").value("ROLE_EMPLOYEE"));
    }

    @Test
    @DisplayName("Should reactivate user when admin")
    void shouldReactivateUser_WhenAdmin() throws Exception {
        // Given
        UpdateUserStatusRequest request = new UpdateUserStatusRequest(
                true, "Fin de suspension"
        );

        // When & Then
        mockMvc.perform(put("/admin/users/" + testAdmin.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.effectiveRole").value("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("Should deny status change when employee")
    void shouldDenyStatusChange_WhenEmployee() throws Exception {
        // Given
        UpdateUserStatusRequest request = new UpdateUserStatusRequest(
                false, "Tentative non autorisée"
        );

        // When & Then
        mockMvc.perform(put("/admin/users/" + testEmployee.getId() + "/status")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}