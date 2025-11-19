package com.stockchef.stockchefback.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockchef.stockchefback.dto.user.UpdateUserRoleRequest;
import com.stockchef.stockchefback.dto.user.UpdateUserStatusRequest;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests TDD pour AdminController - Gestion administrative des utilisateurs
 * RED -> GREEN -> REFACTOR
 */
@WebMvcTest(AdminController.class)
@ActiveProfiles("test")
@DisplayName("Tests TDD - AdminController")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserResponse employeeUser;
    private UserResponse chefUser;
    private UserResponse inactiveUser;

    @BeforeEach
    void setUp() {
        employeeUser = new UserResponse(
                TestUuidHelper.USER_1_UUID, "employee@test.com", "Jean", "Employee", "Jean Employee",
                UserRole.ROLE_EMPLOYEE, UserRole.ROLE_EMPLOYEE, true,
                LocalDateTime.now(), null, "system"
        );

        chefUser = new UserResponse(
                TestUuidHelper.USER_2_UUID, "chef@test.com", "Marie", "Chef", "Marie Chef",
                UserRole.ROLE_CHEF, UserRole.ROLE_CHEF, true,
                LocalDateTime.now(), null, "admin"
        );

        inactiveUser = new UserResponse(
                TestUuidHelper.USER_3_UUID, "inactive@test.com", "Pierre", "Inactive", "Pierre Inactive",
                UserRole.ROLE_CHEF, UserRole.ROLE_EMPLOYEE, false, // Inactif = effectiveRole EMPLOYEE
                LocalDateTime.now(), null, "admin"
        );
    }

    // Tests pour la liste des utilisateurs
    
    @Test
    @DisplayName("RED: GET /api/admin/users - Lister tous les utilisateurs (ADMIN)")
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldListAllUsers_WhenAdmin() throws Exception {
        // Given
        when(userService.getAllUsers()).thenReturn(List.of(employeeUser, chefUser, inactiveUser));

        // When & Then
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].email").value("employee@test.com"))
                .andExpect(jsonPath("$[1].email").value("chef@test.com"))
                .andExpect(jsonPath("$[2].email").value("inactive@test.com"))
                .andExpect(jsonPath("$[2].isActive").value(false))
                .andExpect(jsonPath("$[2].effectiveRole").value("ROLE_EMPLOYEE"));
    }

    @Test
    @DisplayName("RED: GET /api/admin/users - Lister tous les utilisateurs (DEVELOPER)")
    @WithMockUser(authorities = "ROLE_DEVELOPER")
    void shouldListAllUsers_WhenDeveloper() throws Exception {
        // Given
        when(userService.getAllUsers()).thenReturn(List.of(employeeUser, chefUser));

        // When & Then
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("RED: GET /api/admin/users - Accès refusé (EMPLOYEE)")
    @WithMockUser(authorities = "ROLE_EMPLOYEE")
    void shouldDenyAccess_WhenEmployee() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    // Tests pour la modification des rôles

    @Test
    @DisplayName("RED: PUT /api/admin/users/{id}/role - Changer rôle (ADMIN)")
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldUpdateUserRole_WhenAdmin() throws Exception {
        // Given
        UpdateUserRoleRequest request = new UpdateUserRoleRequest(
                UserRole.ROLE_CHEF, "Promotion méritée"
        );
        
        UserResponse updatedUser = new UserResponse(
                TestUuidHelper.USER_1_UUID, "employee@test.com", "Jean", "Employee", "Jean Employee",
                UserRole.ROLE_CHEF, UserRole.ROLE_CHEF, true,
                LocalDateTime.now(), null, "admin"
        );
        
        when(userService.updateUserRole(eq(TestUuidHelper.USER_1_UUID), eq(UserRole.ROLE_CHEF), eq("Promotion méritée")))
                .thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/api/admin/users/" + TestUuidHelper.USER_1_UUID + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_CHEF"))
                .andExpect(jsonPath("$.effectiveRole").value("ROLE_CHEF"));
    }

    @Test
    @DisplayName("RED: PUT /api/admin/users/{id}/role - ADMIN ne peut pas créer DEVELOPER")
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldDenyDeveloperRoleCreation_WhenAdmin() throws Exception {
        // Given
        UpdateUserRoleRequest request = new UpdateUserRoleRequest(
                UserRole.ROLE_DEVELOPER, "Tentative non autorisée"
        );
        
        when(userService.updateUserRole(eq(TestUuidHelper.USER_1_UUID), eq(UserRole.ROLE_DEVELOPER), any()))
                .thenThrow(new InsufficientPermissionsException("Un ADMIN ne peut pas créer un DEVELOPER"));

        // When & Then
        mockMvc.perform(put("/api/admin/users/" + TestUuidHelper.USER_1_UUID + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Un ADMIN ne peut pas créer un DEVELOPER"));
    }

    @Test
    @DisplayName("RED: PUT /api/admin/users/{id}/role - DEVELOPER peut créer n'importe quel rôle")
    @WithMockUser(authorities = "ROLE_DEVELOPER")
    void shouldAllowAnyRoleCreation_WhenDeveloper() throws Exception {
        // Given
        UpdateUserRoleRequest request = new UpdateUserRoleRequest(
                UserRole.ROLE_ADMIN, "Nouveau administrateur"
        );
        
        UserResponse updatedUser = new UserResponse(
                TestUuidHelper.USER_1_UUID, "employee@test.com", "Jean", "Employee", "Jean Employee",
                UserRole.ROLE_ADMIN, UserRole.ROLE_ADMIN, true,
                LocalDateTime.now(), null, "developer"
        );
        
        when(userService.updateUserRole(eq(TestUuidHelper.USER_1_UUID), eq(UserRole.ROLE_ADMIN), eq("Nouveau administrateur")))
                .thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/api/admin/users/" + TestUuidHelper.USER_1_UUID + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }

    // Tests pour la gestion du statut actif/inactif

    @Test
    @DisplayName("RED: PUT /api/admin/users/{id}/status - Désactiver utilisateur")
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldDeactivateUser_WhenAdmin() throws Exception {
        // Given
        UpdateUserStatusRequest request = new UpdateUserStatusRequest(
                false, "Suspension temporaire"
        );
        
        UserResponse deactivatedUser = new UserResponse(
                TestUuidHelper.USER_2_UUID, "chef@test.com", "Marie", "Chef", "Marie Chef",
                UserRole.ROLE_CHEF, UserRole.ROLE_EMPLOYEE, false, // Inactif = effectiveRole EMPLOYEE
                LocalDateTime.now(), null, "admin"
        );
        
        when(userService.updateUserStatus(eq(TestUuidHelper.USER_2_UUID), eq(false), eq("Suspension temporaire")))
                .thenReturn(deactivatedUser);

        // When & Then
        mockMvc.perform(put("/api/admin/users/" + TestUuidHelper.USER_2_UUID + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false))
                .andExpect(jsonPath("$.role").value("ROLE_CHEF")) // Rôle réel conservé
                .andExpect(jsonPath("$.effectiveRole").value("ROLE_EMPLOYEE")); // Rôle effectif dégradé
    }

    @Test
    @DisplayName("RED: PUT /api/admin/users/{id}/status - Réactiver utilisateur")
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldReactivateUser_WhenAdmin() throws Exception {
        // Given
        UpdateUserStatusRequest request = new UpdateUserStatusRequest(
                true, "Fin de suspension"
        );
        
        UserResponse reactivatedUser = new UserResponse(
                TestUuidHelper.USER_3_UUID, "inactive@test.com", "Pierre", "Inactive", "Pierre Inactive",
                UserRole.ROLE_CHEF, UserRole.ROLE_CHEF, true, // Actif = effectiveRole = role
                LocalDateTime.now(), null, "admin"
        );
        
        when(userService.updateUserStatus(eq(TestUuidHelper.USER_3_UUID), eq(true), eq("Fin de suspension")))
                .thenReturn(reactivatedUser);

        // When & Then
        mockMvc.perform(put("/api/admin/users/" + TestUuidHelper.USER_3_UUID + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.role").value("ROLE_CHEF"))
                .andExpect(jsonPath("$.effectiveRole").value("ROLE_CHEF")); // Rôle restauré
    }

    @Test
    @DisplayName("RED: PUT /api/admin/users/{id}/status - Accès refusé (EMPLOYEE)")
    @WithMockUser(authorities = "ROLE_EMPLOYEE")
    void shouldDenyStatusChange_WhenEmployee() throws Exception {
        // Given
        UpdateUserStatusRequest request = new UpdateUserStatusRequest(
                false, "Tentative non autorisée"
        );

        // When & Then
        mockMvc.perform(put("/api/admin/users/" + TestUuidHelper.USER_1_UUID + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}

/**
 * Exception pour permissions insuffisantes
 * (sera créée dans l'implémentation)
 */
class InsufficientPermissionsException extends RuntimeException {
    public InsufficientPermissionsException(String message) {
        super(message);
    }
}
