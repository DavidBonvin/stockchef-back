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
 * Test TDD para la modificación de información de usuarios
 * Verifica las reglas de autorización:
 * - Todos los roles pueden modificar su propia información
 * - DEVELOPER y ADMIN pueden modificar información de otros usuarios
 * - EMPLOYEE y CHEF solo pueden modificar su propia información
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
    @DisplayName("TDD RED: Debe devolver 403 cuando no hay autenticación")
    void shouldReturn403_WhenNotAuthenticated() throws Exception {
        // GIVEN: Sin autenticación
        
        // WHEN: Se intenta actualizar un usuario sin autenticación
        // THEN: Debe devolver 403 Forbidden (comportamiento de Spring Security)
        mockMvc.perform(put("/users/{id}", TestUuidHelper.EMPLOYEE_UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andDo(print())
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(username = "employee@stockchef.com", roles = {"EMPLOYEE"})
    @DisplayName("TDD RED: EMPLOYEE no puede modificar información de otros usuarios")
    void shouldReturn403_WhenEmployeeTryToUpdateOtherUser() throws Exception {
        // GIVEN: Usuario EMPLOYEE intentando modificar otro usuario
        when(userService.updateUser(eq(TestUuidHelper.ADMIN_UUID), any(UpdateUserRequest.class), eq("employee@stockchef.com")))
                .thenThrow(new UnauthorizedUserException("No tienes permisos para modificar este usuario"));

        // WHEN: EMPLOYEE intenta modificar información de ADMIN
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
    @DisplayName("TDD RED: CHEF no puede modificar información de otros usuarios")
    void shouldReturn403_WhenChefTryToUpdateOtherUser() throws Exception {
        // GIVEN: Usuario CHEF intentando modificar otro usuario
        when(userService.updateUser(eq(TestUuidHelper.DEVELOPER_UUID), any(UpdateUserRequest.class), eq("chef@stockchef.com")))
                .thenThrow(new UnauthorizedUserException("No tienes permisos para modificar este usuario"));

        // WHEN: CHEF intenta modificar información de DEVELOPER
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
    @DisplayName("TDD RED: Debe devolver 404 cuando usuario no existe")
    void shouldReturn404_WhenUserNotFound() throws Exception {
        // GIVEN: Usuario que no existe
        String nonExistentUserId = "non-existent-uuid";
        when(userService.updateUser(eq(nonExistentUserId), any(UpdateUserRequest.class), eq("employee@stockchef.com")))
                .thenThrow(new UserNotFoundException("Usuario no encontrado"));

        // WHEN: Se intenta actualizar usuario inexistente
        // THEN: Debe devolver 404
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
    @DisplayName("TDD RED: Debe devolver 409 cuando email ya existe")
    void shouldReturn409_WhenEmailAlreadyExists() throws Exception {
        // GIVEN: Email que ya está en uso
        UpdateUserRequest duplicateEmailRequest = new UpdateUserRequest(
                "Nuevo",
                "Nombre",
                "admin@stockchef.com" // Email que ya existe
        );

        when(userService.updateUser(eq(TestUuidHelper.EMPLOYEE_UUID), any(UpdateUserRequest.class), eq("employee@stockchef.com")))
                .thenThrow(new EmailAlreadyExistsException("El email ya está en uso"));

        // WHEN: Se intenta actualizar con email duplicado
        // THEN: Debe devolver 409 Conflict
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
    @DisplayName("TDD RED: Debe devolver 400 cuando datos son inválidos")
    void shouldReturn400_WhenInvalidData() throws Exception {
        // GIVEN: Datos inválidos (email mal formateado)
        UpdateUserRequest invalidRequest = new UpdateUserRequest(
                "", // firstName vacío
                "N", // lastName muy corto
                "email-invalido" // Email mal formateado
        );

        // WHEN: Se envían datos inválidos
        // THEN: Debe devolver 400 Bad Request
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
    @DisplayName("TDD GREEN: EMPLOYEE puede modificar su propia información")
    void shouldReturn200_WhenEmployeeUpdatesOwnInfo() throws Exception {
        // GIVEN: Usuario EMPLOYEE modificando su propia información
        when(userService.updateUser(eq(TestUuidHelper.EMPLOYEE_UUID), any(UpdateUserRequest.class), eq("employee@stockchef.com")))
                .thenReturn(updatedUserResponse);

        // WHEN: EMPLOYEE actualiza su propia información
        // THEN: Debe devolver 200 con información actualizada
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
    @DisplayName("TDD GREEN: CHEF puede modificar su propia información")
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

        // WHEN: CHEF actualiza su propia información
        // THEN: Debe devolver 200 con información actualizada
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
    @DisplayName("TDD GREEN: DEVELOPER puede modificar información de otros usuarios")
    void shouldReturn200_WhenDeveloperUpdatesOtherUser() throws Exception {
        // GIVEN: Usuario DEVELOPER modificando información de EMPLOYEE
        when(userService.updateUser(eq(TestUuidHelper.EMPLOYEE_UUID), any(UpdateUserRequest.class), eq("developer@stockchef.com")))
                .thenReturn(updatedUserResponse);

        // WHEN: DEVELOPER actualiza información de EMPLOYEE
        // THEN: Debe devolver 200 con información actualizada
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
    @DisplayName("TDD GREEN: ADMIN puede modificar información de cualquier usuario")
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

        // WHEN: ADMIN actualiza información de DEVELOPER
        // THEN: Debe devolver 200 con información actualizada
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

        // WHEN: DEVELOPER actualiza su propia información
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