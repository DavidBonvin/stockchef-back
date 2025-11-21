package com.stockchef.stockchefback.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockchef.stockchefback.dto.auth.LoginRequest;
import com.stockchef.stockchefback.dto.auth.LoginResponse;
import com.stockchef.stockchefback.dto.user.RegisterRequest;
import com.stockchef.stockchefback.dto.user.UpdateUserRoleRequest;
import com.stockchef.stockchefback.dto.user.UpdateUserStatusRequest;
import com.stockchef.stockchefback.dto.user.UserResponse;
import com.stockchef.stockchefback.model.UserRole;
import com.stockchef.stockchefback.repository.UserRepository;
import com.stockchef.stockchefback.testutil.TestUuidHelper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration TDD pour la gestion complète des utilisateurs
 * Test du workflow complet: Registration -> Login -> Gestion roles
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Tests d'Intégration TDD - Gestion Utilisateurs")
class UserManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private String adminToken;
    private String employeeToken;

    @BeforeEach
    void setUp() throws Exception {
        // Nettoyer la base de données et obtenir les tokens nécessaires
        userRepository.deleteAll();
        
        // Les utilisateurs par défaut sont créés par DataInitConfig
        // Obtenir le token admin pour les tests
        adminToken = getAuthToken("admin@stockchef.com", "adminpass123");
        employeeToken = getAuthToken("employee@stockchef.com", "emppass123");
    }

    @Test
    @DisplayName("RED: Workflow complet - Enregistrement public -> Gestion par admin")
    void shouldCompleteUserManagementWorkflow() throws Exception {
        // ÉTAPE 1: Enregistrement public d'un nouvel utilisateur
        RegisterRequest newUserRequest = new RegisterRequest(
                "nouvel.employee@test.com",
                "password123",
                "Alex",
                "Martin"
        );

        MvcResult registerResult = mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("nouvel.employee@test.com"))
                .andExpect(jsonPath("$.role").value("ROLE_EMPLOYEE"))
                .andExpect(jsonPath("$.effectiveRole").value("ROLE_EMPLOYEE"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andReturn();

        UserResponse newUser = objectMapper.readValue(
                registerResult.getResponse().getContentAsString(), 
                UserResponse.class
        );
        
        assertThat(newUser.id()).isNotNull();
        assertThat(TestUuidHelper.isValidUuid(newUser.id())).isTrue();
        String newUserId = newUser.id();

        // ÉTAPE 2: Le nouvel utilisateur peut se connecter
        LoginRequest loginRequest = new LoginRequest(
                "nouvel.employee@test.com", 
                "password123"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role").value("ROLE_EMPLOYEE"));

        // ÉTAPE 3: Admin peut voir tous les utilisateurs
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.email == 'nouvel.employee@test.com')]").exists());

        // ÉTAPE 4: Admin peut promouvoir l'utilisateur
        UpdateUserRoleRequest roleUpdateRequest = new UpdateUserRoleRequest(
                UserRole.ROLE_CHEF,
                "Promotion méritée"
        );

        mockMvc.perform(put("/api/admin/users/" + newUserId + "/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_CHEF"))
                .andExpect(jsonPath("$.effectiveRole").value("ROLE_CHEF"));

        // ÉTAPE 5: L'utilisateur promu a maintenant les permissions de CHEF
        String promotedUserToken = getAuthToken("nouvel.employee@test.com", "password123");
        
        // Vérifier que le token contient le nouveau rôle
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + promotedUserToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_CHEF"));
    }

    @Test
    @DisplayName("RED: Workflow désactivation utilisateur - Perte permissions temporaire")
    void shouldDeactivateUser_AndDegradePermissions() throws Exception {
        // ÉTAPE 1: Enregistrer un utilisateur et le promouvoir
        RegisterRequest userRequest = new RegisterRequest(
                "chef.temporaire@test.com", "password123", "Marie", "Chef"
        );
        
        MvcResult registerResult = mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andReturn();
                
        UserResponse user = objectMapper.readValue(
                registerResult.getResponse().getContentAsString(), 
                UserResponse.class
        );
        assertThat(TestUuidHelper.isValidUuid(user.id())).isTrue();
        String userId = user.id();

        // Promouvoir à CHEF
        UpdateUserRoleRequest promoteRequest = new UpdateUserRoleRequest(
                UserRole.ROLE_CHEF, "Promotion initiale"
        );
        
        mockMvc.perform(put("/api/admin/users/" + userId + "/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(promoteRequest)))
                .andExpect(status().isOk());

        // ÉTAPE 2: Vérifier que l'utilisateur a bien les permissions CHEF
        String chefToken = getAuthToken("chef.temporaire@test.com", "password123");
        
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + chefToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.effectiveRole").value("ROLE_CHEF"));

        // ÉTAPE 3: Admin désactive l'utilisateur
        UpdateUserStatusRequest deactivateRequest = new UpdateUserStatusRequest(
                false, "Suspension disciplinaire"
        );
        
        mockMvc.perform(put("/api/admin/users/" + userId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deactivateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false))
                .andExpect(jsonPath("$.role").value("ROLE_CHEF")) // Rôle conservé
                .andExpect(jsonPath("$.effectiveRole").value("ROLE_EMPLOYEE")); // Permissions dégradées

        // ÉTAPE 4: L'utilisateur peut toujours se connecter mais avec permissions EMPLOYEE
        String degradedToken = getAuthToken("chef.temporaire@test.com", "password123");
        
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + degradedToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false))
                .andExpect(jsonPath("$.effectiveRole").value("ROLE_EMPLOYEE"));
        
        // ÉTAPE 5: Réactivation restaure les permissions
        UpdateUserStatusRequest reactivateRequest = new UpdateUserStatusRequest(
                true, "Fin de suspension"
        );
        
        mockMvc.perform(put("/api/admin/users/" + userId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reactivateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.effectiveRole").value("ROLE_CHEF")); // Permissions restaurées
    }

    @Test
    @DisplayName("RED: Sécurité - EMPLOYEE ne peut pas gérer d'autres utilisateurs")
    void shouldDenyEmployeeAccessToUserManagement() throws Exception {
        // Tenter d'accéder à la liste des utilisateurs en tant qu'EMPLOYEE
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());

        // Tenter de changer le rôle d'un autre utilisateur
        UpdateUserRoleRequest roleRequest = new UpdateUserRoleRequest(
                UserRole.ROLE_ADMIN, "Tentative non autorisée"
        );
        
        mockMvc.perform(put("/api/admin/users/" + TestUuidHelper.USER_1_UUID + "/role")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RED: Email unique - Impossible d'enregistrer le même email")
    void shouldPreventDuplicateEmailRegistration() throws Exception {
        // Premier enregistrement
        RegisterRequest firstUser = new RegisterRequest(
                "unique@test.com", "password123", "Premier", "User"
        );
        
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstUser)))
                .andExpect(status().isCreated());

        // Tentative d'enregistrement avec le même email
        RegisterRequest duplicateUser = new RegisterRequest(
                "unique@test.com", "differentPassword", "Autre", "User"
        );
        
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Un utilisateur avec cet email existe déjà"));
    }

    /**
     * Méthode utilitaire pour obtenir un token d'authentification
     */
    private String getAuthToken(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(email, password);
        
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        
        LoginResponse loginResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(), 
                LoginResponse.class
        );
        
        return loginResponse.token();
    }
}
