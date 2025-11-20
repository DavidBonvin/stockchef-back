package com.stockchef.stockchefback.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockchef.stockchefback.dto.user.UserResponse;
import com.stockchef.stockchefback.exception.InsufficientPermissionsException;
import com.stockchef.stockchefback.exception.UserNotFoundException;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test TDD para CRUD completo de usuarios y gestión administrativa
 * Cubre los endpoints:
 * - GET /users (listar con filtros)
 * - GET /users/{id} (obtener por ID)
 * - DELETE /users/{id} (eliminar/desactivar)
 * - PUT /users/{id}/role (cambiar rol)
 * - PUT /users/{id}/status (cambiar estado)
 */
@SpringBootTest
@AutoConfigureWebMvc
@DisplayName("TDD: UserController - CRUD Completo y Gestión Administrativa")
class UserCrudTDDTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private List<UserResponse> allUsersList;
    private UserResponse employeeUser;
    private UserResponse adminUser;
    private UserResponse developerUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        
        // Datos de prueba
        employeeUser = new UserResponse(
                TestUuidHelper.EMPLOYEE_UUID,
                "employee@stockchef.com",
                "Employee",
                "User",
                UserRole.ROLE_EMPLOYEE,
                UserRole.ROLE_EMPLOYEE,
                true,
                LocalDateTime.parse("2025-11-19T19:54:04.123456"),
                null,
                "system"
        );

        adminUser = new UserResponse(
                TestUuidHelper.ADMIN_UUID,
                "admin@stockchef.com",
                "Admin",
                "User",
                UserRole.ROLE_ADMIN,
                UserRole.ROLE_ADMIN,
                true,
                LocalDateTime.parse("2025-11-19T19:54:04.123456"),
                null,
                "system"
        );

        developerUser = new UserResponse(
                TestUuidHelper.DEVELOPER_UUID,
                "developer@stockchef.com",
                "Developer",
                "User",
                UserRole.ROLE_DEVELOPER,
                UserRole.ROLE_DEVELOPER,
                true,
                LocalDateTime.parse("2025-11-19T19:54:04.123456"),
                null,
                "system"
        );

        allUsersList = List.of(adminUser, developerUser, employeeUser);
    }

    // ==================== GET /users Tests ====================

    @Test
    @DisplayName("TDD RED: Sin autenticación debe devolver 403 en GET /users")
    void getAllUsers_WhenNotAuthenticated_ShouldReturn403() throws Exception {
        // GIVEN: Sin autenticación

        // WHEN: Se llama GET /users sin autenticación
        // THEN: Debe devolver 403 Forbidden
        mockMvc.perform(get("/users"))
                .andDo(print())
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(username = "employee@stockchef.com", roles = {"EMPLOYEE"})
    @DisplayName("TDD RED: EMPLOYEE no puede listar usuarios")
    void getAllUsers_WhenEmployee_ShouldReturn403() throws Exception {
        // GIVEN: Usuario EMPLOYEE intentando listar usuarios
        when(userService.getAllUsers(eq("employee@stockchef.com"), any(), any()))
                .thenThrow(new InsufficientPermissionsException("No tienes permisos para listar usuarios"));

        // WHEN: EMPLOYEE intenta listar usuarios
        // THEN: Debe devolver 403 Forbidden
        mockMvc.perform(get("/users"))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(userService, times(1))
                .getAllUsers(eq("employee@stockchef.com"), any(), any());
    }

    @Test
    @WithMockUser(username = "chef@stockchef.com", roles = {"CHEF"})
    @DisplayName("TDD RED: CHEF no puede listar usuarios")
    void getAllUsers_WhenChef_ShouldReturn403() throws Exception {
        // GIVEN: Usuario CHEF intentando listar usuarios
        when(userService.getAllUsers(eq("chef@stockchef.com"), any(), any()))
                .thenThrow(new InsufficientPermissionsException("No tienes permisos para listar usuarios"));

        // WHEN: CHEF intenta listar usuarios
        // THEN: Debe devolver 403 Forbidden
        mockMvc.perform(get("/users"))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(userService, times(1))
                .getAllUsers(eq("chef@stockchef.com"), any(), any());
    }

    @Test
    @WithMockUser(username = "admin@stockchef.com", roles = {"ADMIN"})
    @DisplayName("TDD GREEN: ADMIN puede listar todos los usuarios")
    void getAllUsers_WhenAdmin_ShouldReturnAllUsers() throws Exception {
        // GIVEN: Usuario ADMIN solicitando lista de usuarios
        when(userService.getAllUsers(eq("admin@stockchef.com"), eq(null), eq(null)))
                .thenReturn(allUsersList);

        // WHEN: ADMIN solicita lista de usuarios
        // THEN: Debe devolver 200 con lista completa
        mockMvc.perform(get("/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].email").value("admin@stockchef.com"))
                .andExpect(jsonPath("$[0].role").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$[1].email").value("developer@stockchef.com"))
                .andExpect(jsonPath("$[1].role").value("ROLE_DEVELOPER"))
                .andExpect(jsonPath("$[2].email").value("employee@stockchef.com"))
                .andExpect(jsonPath("$[2].role").value("ROLE_EMPLOYEE"));

        verify(userService, times(1))
                .getAllUsers(eq("admin@stockchef.com"), eq(null), eq(null));
    }

    @Test
    @WithMockUser(username = "developer@stockchef.com", roles = {"DEVELOPER"})
    @DisplayName("TDD GREEN: DEVELOPER puede listar todos los usuarios")
    void getAllUsers_WhenDeveloper_ShouldReturnAllUsers() throws Exception {
        // GIVEN: Usuario DEVELOPER solicitando lista de usuarios
        when(userService.getAllUsers(eq("developer@stockchef.com"), eq(null), eq(null)))
                .thenReturn(allUsersList);

        // WHEN: DEVELOPER solicita lista de usuarios
        // THEN: Debe devolver 200 con lista completa
        mockMvc.perform(get("/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].role").exists())
                .andExpect(jsonPath("$[1].role").exists())
                .andExpect(jsonPath("$[2].role").exists());

        verify(userService, times(1))
                .getAllUsers(eq("developer@stockchef.com"), eq(null), eq(null));
    }

    @Test
    @WithMockUser(username = "admin@stockchef.com", roles = {"ADMIN"})
    @DisplayName("TDD GREEN: Filtrar usuarios por rol EMPLOYEE")
    void getAllUsers_FilterByRole_ShouldReturnFilteredUsers() throws Exception {
        // GIVEN: Filtro por rol EMPLOYEE
        List<UserResponse> employeeUsers = List.of(employeeUser);
        when(userService.getAllUsers(eq("admin@stockchef.com"), eq("EMPLOYEE"), eq(null)))
                .thenReturn(employeeUsers);

        // WHEN: Se solicita filtro por rol EMPLOYEE
        // THEN: Debe devolver solo usuarios EMPLOYEE
        mockMvc.perform(get("/users")
                        .param("role", "EMPLOYEE"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].role").value("ROLE_EMPLOYEE"));

        verify(userService, times(1))
                .getAllUsers(eq("admin@stockchef.com"), eq("EMPLOYEE"), eq(null));
    }

    @Test
    @WithMockUser(username = "admin@stockchef.com", roles = {"ADMIN"})
    @DisplayName("TDD GREEN: Filtrar usuarios por estado activo")
    void getAllUsers_FilterByStatus_ShouldReturnActiveUsers() throws Exception {
        // GIVEN: Filtro por estado activo
        when(userService.getAllUsers(eq("admin@stockchef.com"), eq(null), eq(true)))
                .thenReturn(allUsersList);

        // WHEN: Se solicita filtro por usuarios activos
        // THEN: Debe devolver solo usuarios activos
        mockMvc.perform(get("/users")
                        .param("active", "true"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));

        verify(userService, times(1))
                .getAllUsers(eq("admin@stockchef.com"), eq(null), eq(true));
    }

    // ==================== GET /users/{id} Tests ====================

    @Test
    @DisplayName("TDD RED: Sin autenticación debe devolver 403 en GET /users/{id}")
    void getUserById_WhenNotAuthenticated_ShouldReturn403() throws Exception {
        // GIVEN: Sin autenticación

        // WHEN: Se llama GET /users/{id} sin autenticación
        // THEN: Debe devolver 403 Forbidden
        mockMvc.perform(get("/users/{id}", TestUuidHelper.EMPLOYEE_UUID))
                .andDo(print())
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(username = "employee@stockchef.com", roles = {"EMPLOYEE"})
    @DisplayName("TDD GREEN: Usuario puede ver su propio perfil por ID")
    void getUserById_WhenOwnProfile_ShouldReturnUser() throws Exception {
        // GIVEN: Usuario viendo su propio perfil
        when(userService.getUserById(eq(TestUuidHelper.EMPLOYEE_UUID), eq("employee@stockchef.com")))
                .thenReturn(employeeUser);

        // WHEN: Usuario solicita su propio perfil por ID
        // THEN: Debe devolver 200 con sus datos
        mockMvc.perform(get("/users/{id}", TestUuidHelper.EMPLOYEE_UUID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TestUuidHelper.EMPLOYEE_UUID))
                .andExpect(jsonPath("$.email").value("employee@stockchef.com"))
                .andExpect(jsonPath("$.role").value("ROLE_EMPLOYEE"));

        verify(userService, times(1))
                .getUserById(eq(TestUuidHelper.EMPLOYEE_UUID), eq("employee@stockchef.com"));
    }

    @Test
    @WithMockUser(username = "employee@stockchef.com", roles = {"EMPLOYEE"})
    @DisplayName("TDD RED: EMPLOYEE no puede ver perfil de otros usuarios")
    void getUserById_WhenEmployeeViewingOthers_ShouldReturn403() throws Exception {
        // GIVEN: EMPLOYEE intentando ver perfil de ADMIN
        when(userService.getUserById(eq(TestUuidHelper.ADMIN_UUID), eq("employee@stockchef.com")))
                .thenThrow(new InsufficientPermissionsException("No tienes permisos para ver este usuario"));

        // WHEN: EMPLOYEE intenta ver perfil de ADMIN
        // THEN: Debe devolver 403 Forbidden
        mockMvc.perform(get("/users/{id}", TestUuidHelper.ADMIN_UUID))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(userService, times(1))
                .getUserById(eq(TestUuidHelper.ADMIN_UUID), eq("employee@stockchef.com"));
    }

    @Test
    @WithMockUser(username = "admin@stockchef.com", roles = {"ADMIN"})
    @DisplayName("TDD GREEN: ADMIN puede ver cualquier perfil por ID")
    void getUserById_WhenAdmin_ShouldReturnAnyUser() throws Exception {
        // GIVEN: ADMIN viendo perfil de EMPLOYEE
        when(userService.getUserById(eq(TestUuidHelper.EMPLOYEE_UUID), eq("admin@stockchef.com")))
                .thenReturn(employeeUser);

        // WHEN: ADMIN solicita perfil de EMPLOYEE por ID
        // THEN: Debe devolver 200 con datos del usuario
        mockMvc.perform(get("/users/{id}", TestUuidHelper.EMPLOYEE_UUID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TestUuidHelper.EMPLOYEE_UUID))
                .andExpect(jsonPath("$.email").value("employee@stockchef.com"));

        verify(userService, times(1))
                .getUserById(eq(TestUuidHelper.EMPLOYEE_UUID), eq("admin@stockchef.com"));
    }

    // ==================== DELETE /users/{id} Tests ====================

    @Test
    @DisplayName("TDD RED: Sin autenticación debe devolver 403 en DELETE /users/{id}")
    void deleteUser_WhenNotAuthenticated_ShouldReturn403() throws Exception {
        // GIVEN: Sin autenticación

        // WHEN: Se llama DELETE /users/{id} sin autenticación
        // THEN: Debe devolver 403 Forbidden
        mockMvc.perform(delete("/users/{id}", TestUuidHelper.EMPLOYEE_UUID))
                .andDo(print())
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(username = "employee@stockchef.com", roles = {"EMPLOYEE"})
    @DisplayName("TDD RED: EMPLOYEE no puede eliminar usuarios")
    void deleteUser_WhenEmployee_ShouldReturn403() throws Exception {
        // GIVEN: EMPLOYEE intentando eliminar usuario
        doThrow(new InsufficientPermissionsException("No tienes permisos para eliminar usuarios"))
                .when(userService).deleteUser(eq(TestUuidHelper.ADMIN_UUID), eq("employee@stockchef.com"));

        // WHEN: EMPLOYEE intenta eliminar usuario
        // THEN: Debe devolver 403 Forbidden
        mockMvc.perform(delete("/users/{id}", TestUuidHelper.ADMIN_UUID))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(userService, times(1))
                .deleteUser(eq(TestUuidHelper.ADMIN_UUID), eq("employee@stockchef.com"));
    }

    @Test
    @WithMockUser(username = "developer@stockchef.com", roles = {"DEVELOPER"})
    @DisplayName("TDD RED: DEVELOPER no puede eliminar usuarios")
    void deleteUser_WhenDeveloper_ShouldReturn403() throws Exception {
        // GIVEN: DEVELOPER intentando eliminar usuario
        doThrow(new InsufficientPermissionsException("Solo ADMIN puede eliminar usuarios"))
                .when(userService).deleteUser(eq(TestUuidHelper.EMPLOYEE_UUID), eq("developer@stockchef.com"));

        // WHEN: DEVELOPER intenta eliminar usuario
        // THEN: Debe devolver 403 Forbidden
        mockMvc.perform(delete("/users/{id}", TestUuidHelper.EMPLOYEE_UUID))
                .andDo(print())
                .andExpect(status().isForbidden());

        verify(userService, times(1))
                .deleteUser(eq(TestUuidHelper.EMPLOYEE_UUID), eq("developer@stockchef.com"));
    }

    @Test
    @WithMockUser(username = "admin@stockchef.com", roles = {"ADMIN"})
    @DisplayName("TDD GREEN: ADMIN puede eliminar usuarios")
    void deleteUser_WhenAdmin_ShouldReturn204() throws Exception {
        // GIVEN: ADMIN eliminando usuario EMPLOYEE
        doNothing().when(userService)
                .deleteUser(eq(TestUuidHelper.EMPLOYEE_UUID), eq("admin@stockchef.com"));

        // WHEN: ADMIN elimina usuario
        // THEN: Debe devolver 204 No Content
        mockMvc.perform(delete("/users/{id}", TestUuidHelper.EMPLOYEE_UUID))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(userService, times(1))
                .deleteUser(eq(TestUuidHelper.EMPLOYEE_UUID), eq("admin@stockchef.com"));
    }

    @Test
    @WithMockUser(username = "admin@stockchef.com", roles = {"ADMIN"})
    @DisplayName("TDD RED: No se puede eliminar usuario inexistente")
    void deleteUser_WhenUserNotFound_ShouldReturn404() throws Exception {
        // GIVEN: Usuario inexistente
        String nonExistentUserId = "non-existent-uuid";
        doThrow(new UserNotFoundException("Usuario no encontrado"))
                .when(userService)
                .deleteUser(eq(nonExistentUserId), eq("admin@stockchef.com"));

        // WHEN: Se intenta eliminar usuario inexistente
        // THEN: Debe devolver 404 Not Found
        mockMvc.perform(delete("/users/{id}", nonExistentUserId))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(userService, times(1))
                .deleteUser(eq(nonExistentUserId), eq("admin@stockchef.com"));
    }
}