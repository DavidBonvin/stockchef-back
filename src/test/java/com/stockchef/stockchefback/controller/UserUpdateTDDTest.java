package com.stockchef.stockchefback.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockchef.stockchefback.dto.user.UpdateUserRequest;
import com.stockchef.stockchefback.dto.user.UserResponse;
import com.stockchef.stockchefback.exception.UserNotFoundException;
import com.stockchef.stockchefback.exception.EmailAlreadyExistsException;
import com.stockchef.stockchefback.exception.UnauthorizedUserException;
import com.stockchef.stockchefback.model.UserRole;
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

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test TDD pour la modification d'information d'utilisateurs
 * Verifica las reglas de autorización:
 * - Tous les rôles peuvent modifier leur propre information
 * - DEVELOPER et ADMIN peuvent modifier information d'autres utilisateurs
 * - EMPLOYEE et CHEF peuvent seulement modifier leur propre information
 */
@SpringBootTest
@AutoConfigureWebMvc
@DisplayName("TDD: UserController - Endpoint PUT /users/{id}")
class UserUpdateTDDTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UpdateUserRequest validUpdateRequest;
    private UserResponse updatedUserResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        
        validUpdateRequest = new UpdateUserRequest(
                "Nuevo",
                "Nombre", 
                "nuevo.email@stockchef.com"
        );

        updatedUserResponse = new UserResponse(
                TestUuidHelper.EMPLOYEE_UUID,
                "nuevo.email@stockchef.com",
                "Nuevo",
                "Nombre",
                UserRole.ROLE_EMPLOYEE,
                UserRole.ROLE_EMPLOYEE,
                true,
                LocalDateTime.parse("2025-11-19T19:54:04.123456"),
                null,
                "system"
        );
    }

    // ==================== RED TESTS (Casos de error) ====================

    @Test
    @DisplayName("TDD RED: Doit retourner 403 quand il n'y a pas d'authentification")
    void shouldReturn403_WhenNotAuthenticated() throws Exception {
        // GIVEN: Sin autenticación
        
        // WHEN: On essaie de mettre à jour un utilisateur sans authentification
        // THEN: Doit retourner 403 Forbidden (comportement de Spring Security)
        mockMvc.perform(put("/users/{id}", TestUuidHelper.EMPLOYEE_UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andDo(print())
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(username = "employee@stockchef.com", roles = {"EMPLOYEE"})
    @DisplayName("TDD RED: EMPLOYEE ne peut pas modifier information d'autres utilisateurs")
    void shouldReturn403_WhenEmployeeTryToUpdateOtherUser() throws Exception {
        // GIVEN: Utilisateur EMPLOYEE essayant modifier autre utilisateur
        when(userService.updateUser(eq(TestUuidHelper.ADMIN_UUID), any(UpdateUserRequest.class), eq("employee@stockchef.com")))
                .thenThrow(new UnauthorizedUserException("Vous n'avez pas les permissions pour modifier cet utilisateur"));

        // WHEN: EMPLOYEE essaie modifier information d'ADMIN
        // THEN: Debe devolver 403 Forbidden
        mockMvc.perform(put("/users/{id}", TestUuidHelper.ADMIN_UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(userService, times(1))
                .updateUser(eq(TestUuidHelper.ADMIN_UUID), any(UpdateUserRequest.class), eq("employee@stockchef.com"));
    }

    @Test
    @WithMockUser(username = "chef@stockchef.com", roles = {"CHEF"})
    @DisplayName("TDD RED: CHEF ne peut pas modifier information d'autres utilisateurs")
    void shouldReturn403_WhenChefTryToUpdateOtherUser() throws Exception {
        // GIVEN: Utilisateur CHEF essayant modifier autre utilisateur
        when(userService.updateUser(eq(TestUuidHelper.DEVELOPER_UUID), any(UpdateUserRequest.class), eq("chef@stockchef.com")))
                .thenThrow(new UnauthorizedUserException("Vous n'avez pas les permissions pour modifier cet utilisateur"));

        // WHEN: CHEF essaie modifier information de DEVELOPER
        // THEN: Debe devolver 403 Forbidden
        mockMvc.perform(put("/users/{id}", TestUuidHelper.DEVELOPER_UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(userService, times(1))
                .updateUser(eq(TestUuidHelper.DEVELOPER_UUID), any(UpdateUserRequest.class), eq("chef@stockchef.com"));
    }

    @Test
    @WithMockUser(username = "employee@stockchef.com", roles = {"EMPLOYEE"})
    @DisplayName("TDD RED: Doit retourner 404 quand utilisateur n'existe pas")
    void shouldReturn404_WhenUserNotFound() throws Exception {
        // GIVEN: Utilisateur qui n'existe pas
        String nonExistentUserId = "non-existent-uuid";
        when(userService.updateUser(eq(nonExistentUserId), any(UpdateUserRequest.class), eq("employee@stockchef.com")))
                .thenThrow(new UserNotFoundException("Utilisateur non trouvé"));

        // WHEN: On essaie de mettre à jour utilisateur inexistant
        // THEN: Doit retourner 404
        mockMvc.perform(put("/users/{id}", nonExistentUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(userService, times(1))
                .updateUser(eq(nonExistentUserId), any(UpdateUserRequest.class), eq("employee@stockchef.com"));
    }

    @Test
    @WithMockUser(username = "employee@stockchef.com", roles = {"EMPLOYEE"})
    @DisplayName("TDD RED: Doit retourner 409 quand email existe déjà")
    void shouldReturn409_WhenEmailAlreadyExists() throws Exception {
        // GIVEN: Email que ya está en uso
        UpdateUserRequest duplicateEmailRequest = new UpdateUserRequest(
                "Nuevo",
                "Nombre",
                "admin@stockchef.com" // Email que ya existe
        );

        when(userService.updateUser(eq(TestUuidHelper.EMPLOYEE_UUID), any(UpdateUserRequest.class), eq("employee@stockchef.com")))
                .thenThrow(new EmailAlreadyExistsException("El email ya está en uso"));

        // WHEN: On essaie de mettre à jour avec email dupliqué
        // THEN: Doit retourner 409 Conflict
        mockMvc.perform(put("/users/{id}", TestUuidHelper.EMPLOYEE_UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateEmailRequest)))
                .andDo(print())
                .andExpect(status().isConflict());

        verify(userService, times(1))
                .updateUser(eq(TestUuidHelper.EMPLOYEE_UUID), any(UpdateUserRequest.class), eq("employee@stockchef.com"));
    }

    @Test
    @WithMockUser(username = "employee@stockchef.com", roles = {"EMPLOYEE"})
    @DisplayName("TDD RED: Doit retourner 400 quand données sont invalides")
    void shouldReturn400_WhenInvalidData() throws Exception {
        // GIVEN: Datos inválidos (email mal formateado)
        UpdateUserRequest invalidRequest = new UpdateUserRequest(
                "", // firstName vacío
                "N", // lastName muy corto
                "email-invalido" // Email mal formateado
        );

        // WHEN: Se envían datos inválidos
        // THEN: Doit retourner 400 Bad Request
        mockMvc.perform(put("/users/{id}", TestUuidHelper.EMPLOYEE_UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    // ==================== GREEN TESTS (Casos exitosos) ====================

    @Test
    @WithMockUser(username = "employee@stockchef.com", roles = {"EMPLOYEE"})
    @DisplayName("TDD GREEN: EMPLOYEE peut modifier sa propre information")
    void shouldReturn200_WhenEmployeeUpdatesOwnInfo() throws Exception {
        // GIVEN: Utilisateur EMPLOYEE modifiant sa propre information
        when(userService.updateUser(eq(TestUuidHelper.EMPLOYEE_UUID), any(UpdateUserRequest.class), eq("employee@stockchef.com")))
                .thenReturn(updatedUserResponse);

        // WHEN: EMPLOYEE met à jour sa propre information
        // THEN: Doit retourner 200 avec information mise à jour
        mockMvc.perform(put("/users/{id}", TestUuidHelper.EMPLOYEE_UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(TestUuidHelper.EMPLOYEE_UUID))
                .andExpect(jsonPath("$.email").value("nuevo.email@stockchef.com"))
                .andExpect(jsonPath("$.firstName").value("Nuevo"))
                .andExpect(jsonPath("$.lastName").value("Nombre"))
                .andExpect(jsonPath("$.role").value("ROLE_EMPLOYEE"));

        verify(userService, times(1))
                .updateUser(eq(TestUuidHelper.EMPLOYEE_UUID), any(UpdateUserRequest.class), eq("employee@stockchef.com"));
    }

    @Test
    @WithMockUser(username = "chef@stockchef.com", roles = {"CHEF"})
    @DisplayName("TDD GREEN: CHEF peut modifier sa propre information")
    void shouldReturn200_WhenChefUpdatesOwnInfo() throws Exception {
        // GIVEN: Usuario CHEF modificando su propia información
        UserResponse chefUpdatedResponse = new UserResponse(
                TestUuidHelper.CHEF_UUID,
                "nuevo.chef@stockchef.com",
                "Chef",
                "Actualizado",
                UserRole.ROLE_CHEF,
                UserRole.ROLE_CHEF,
                true,
                LocalDateTime.parse("2025-11-19T19:54:04.123456"),
                null,
                "system"
        );

        when(userService.updateUser(eq(TestUuidHelper.CHEF_UUID), any(UpdateUserRequest.class), eq("chef@stockchef.com")))
                .thenReturn(chefUpdatedResponse);

        UpdateUserRequest chefUpdateRequest = new UpdateUserRequest(
                "Chef",
                "Actualizado",
                "nuevo.chef@stockchef.com"
        );

        // WHEN: CHEF met à jour sa propre information
        // THEN: Doit retourner 200 avec information mise à jour
        mockMvc.perform(put("/users/{id}", TestUuidHelper.CHEF_UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chefUpdateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TestUuidHelper.CHEF_UUID))
                .andExpect(jsonPath("$.email").value("nuevo.chef@stockchef.com"))
                .andExpect(jsonPath("$.role").value("ROLE_CHEF"));

        verify(userService, times(1))
                .updateUser(eq(TestUuidHelper.CHEF_UUID), any(UpdateUserRequest.class), eq("chef@stockchef.com"));
    }

    @Test
    @WithMockUser(username = "developer@stockchef.com", roles = {"DEVELOPER"})
    @DisplayName("TDD GREEN: DEVELOPER peut modifier information d'autres utilisateurs")
    void shouldReturn200_WhenDeveloperUpdatesOtherUser() throws Exception {
        // GIVEN: Usuario DEVELOPER modificando información de EMPLOYEE
        when(userService.updateUser(eq(TestUuidHelper.EMPLOYEE_UUID), any(UpdateUserRequest.class), eq("developer@stockchef.com")))
                .thenReturn(updatedUserResponse);

        // WHEN: DEVELOPER met à jour information d'EMPLOYEE
        // THEN: Doit retourner 200 avec information mise à jour
        mockMvc.perform(put("/users/{id}", TestUuidHelper.EMPLOYEE_UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TestUuidHelper.EMPLOYEE_UUID))
                .andExpect(jsonPath("$.email").value("nuevo.email@stockchef.com"))
                .andExpect(jsonPath("$.firstName").value("Nuevo"))
                .andExpect(jsonPath("$.lastName").value("Nombre"));

        verify(userService, times(1))
                .updateUser(eq(TestUuidHelper.EMPLOYEE_UUID), any(UpdateUserRequest.class), eq("developer@stockchef.com"));
    }

    @Test
    @WithMockUser(username = "admin@stockchef.com", roles = {"ADMIN"})
    @DisplayName("TDD GREEN: ADMIN peut modifier information de n'importe quel utilisateur")
    void shouldReturn200_WhenAdminUpdatesAnyUser() throws Exception {
        // GIVEN: Usuario ADMIN modificando información de DEVELOPER
        UserResponse developerUpdatedResponse = new UserResponse(
                TestUuidHelper.DEVELOPER_UUID,
                "developer.updated@stockchef.com",
                "Developer",
                "Actualizado",
                UserRole.ROLE_DEVELOPER,
                UserRole.ROLE_DEVELOPER,
                true,
                LocalDateTime.parse("2025-11-19T19:54:04.123456"),
                null,
                "system"
        );

        when(userService.updateUser(eq(TestUuidHelper.DEVELOPER_UUID), any(UpdateUserRequest.class), eq("admin@stockchef.com")))
                .thenReturn(developerUpdatedResponse);

        UpdateUserRequest developerUpdateRequest = new UpdateUserRequest(
                "Developer",
                "Actualizado",
                "developer.updated@stockchef.com"
        );

        // WHEN: ADMIN met à jour information de DEVELOPER
        // THEN: Doit retourner 200 avec information mise à jour
        mockMvc.perform(put("/users/{id}", TestUuidHelper.DEVELOPER_UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(developerUpdateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TestUuidHelper.DEVELOPER_UUID))
                .andExpect(jsonPath("$.email").value("developer.updated@stockchef.com"))
                .andExpect(jsonPath("$.role").value("ROLE_DEVELOPER"));

        verify(userService, times(1))
                .updateUser(eq(TestUuidHelper.DEVELOPER_UUID), any(UpdateUserRequest.class), eq("admin@stockchef.com"));
    }

    // ==================== REFACTOR TESTS (Casos edge) ====================

    @Test
    @WithMockUser(username = "developer@stockchef.com", roles = {"DEVELOPER"})
    @DisplayName("TDD REFACTOR: DEVELOPER puede modificar su propia información")
    void shouldReturn200_WhenDeveloperUpdatesOwnInfo() throws Exception {
        // GIVEN: Usuario DEVELOPER modificando su propia información
        UserResponse developerSelfUpdate = new UserResponse(
                TestUuidHelper.DEVELOPER_UUID,
                "self.update@stockchef.com",
                "Self",
                "Updated",
                UserRole.ROLE_DEVELOPER,
                UserRole.ROLE_DEVELOPER,
                true,
                LocalDateTime.parse("2025-11-19T19:54:04.123456"),
                LocalDateTime.now(),
                "system"
        );

        when(userService.updateUser(eq(TestUuidHelper.DEVELOPER_UUID), any(UpdateUserRequest.class), eq("developer@stockchef.com")))
                .thenReturn(developerSelfUpdate);

        UpdateUserRequest selfUpdateRequest = new UpdateUserRequest(
                "Self",
                "Updated", 
                "self.update@stockchef.com"
        );

        // WHEN: DEVELOPER met à jour sa propre information
        // THEN: Debe devolver 200 exitosamente
        mockMvc.perform(put("/users/{id}", TestUuidHelper.DEVELOPER_UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(selfUpdateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("self.update@stockchef.com"))
                .andExpect(jsonPath("$.firstName").value("Self"))
                .andExpect(jsonPath("$.lastName").value("Updated"));

        verify(userService, times(1))
                .updateUser(eq(TestUuidHelper.DEVELOPER_UUID), any(UpdateUserRequest.class), eq("developer@stockchef.com"));
    }
}