package com.stockchef.stockchefback.controller;

import com.stockchef.stockchefback.dto.user.UserResponse;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test TDD básico para el endpoint GET /users/me
 * Verifica que el endpoint devuelve la información correcta del usuario autenticado
 * 
 * ENFOQUE TDD:
 * 1. RED: Escribir test que falla
 * 2. GREEN: Implementar mínimo código para que pase
 * 3. REFACTOR: Mejorar el código manteniendo los tests
 */
@SpringBootTest
@AutoConfigureWebMvc
@DisplayName("TDD Básico: UserController - Endpoint /users/me")
class UserBasicTDDTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private UserResponse expectedUserResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        
        expectedUserResponse = new UserResponse(
                TestUuidHelper.DEVELOPER_UUID,
                "developer@stockchef.com",
                "Super",
                "Developer",
                UserRole.ROLE_DEVELOPER,
                UserRole.ROLE_DEVELOPER,
                true,
                LocalDateTime.parse("2025-11-19T19:54:04.123456"),
                null,
                "system"
        );
    }

    // ==================== RED TESTS (Fallan inicialmente) ====================
    
    @Test
    @DisplayName("TDD RED: Debe devolver 403 cuando no hay autenticación")
    void test_step1_red_noAuthentication_returns401() throws Exception {
        // GIVEN: Sin autenticación
        
        // WHEN: Se llama al endpoint /users/me sin autenticación
        // THEN: Debe devolver 403 Forbidden (comportamiento de Spring Security)
        mockMvc.perform(get("/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());

        // Verificar que el servicio no fue llamado
        verifyNoInteractions(userService);
    }

    // ==================== GREEN TESTS (Pasan con implementación mínima) ====================
    
    @Test
    @WithMockUser(username = "developer@stockchef.com", roles = {"DEVELOPER"})
    @DisplayName("TDD GREEN: Debe devolver información del usuario cuando está autenticado")
    void test_step2_green_authenticated_returnsUserInfo() throws Exception {
        // GIVEN: Usuario autenticado y existente en la base de datos
        when(userService.getUserByEmail("developer@stockchef.com"))
                .thenReturn(expectedUserResponse);

        // WHEN: Se llama al endpoint /users/me con usuario autenticado
        // THEN: Debe devolver 200 con la información del usuario
        mockMvc.perform(get("/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(TestUuidHelper.DEVELOPER_UUID))
                .andExpect(jsonPath("$.email").value("developer@stockchef.com"))
                .andExpect(jsonPath("$.firstName").value("Super"))
                .andExpect(jsonPath("$.lastName").value("Developer"))
                .andExpect(jsonPath("$.role").value("ROLE_DEVELOPER"))
                .andExpect(jsonPath("$.isActive").value(true));

        // Verificar que el servicio fue llamado correctamente
        verify(userService, times(1)).getUserByEmail("developer@stockchef.com");
    }

    // ==================== REFACTOR TESTS (Casos adicionales) ====================
    
    @Test
    @WithMockUser(username = "noexiste@stockchef.com", roles = {"EMPLOYEE"})
    @DisplayName("TDD REFACTOR: Debe devolver 404 cuando usuario no existe en BD")
    void test_step3_refactor_userNotFound_returns404() throws Exception {
        // GIVEN: Usuario autenticado pero no existe en BD
        String emailNoExistente = "noexiste@stockchef.com";
        
        when(userService.getUserByEmail(emailNoExistente))
                .thenThrow(new UserNotFoundException("Usuario no encontrado con email: " + emailNoExistente));

        // WHEN: Se llama al endpoint con usuario inexistente
        // THEN: Debe devolver 404
        mockMvc.perform(get("/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserByEmail(emailNoExistente);
    }

    @Test
    @WithMockUser(username = "admin@stockchef.com", roles = {"ADMIN"})
    @DisplayName("TDD REFACTOR: Debe funcionar para diferentes roles de usuario")
    void test_step4_refactor_differentRoles_worksCorrectly() throws Exception {
        // GIVEN: Usuario ADMIN
        UserResponse adminResponse = new UserResponse(
                TestUuidHelper.ADMIN_UUID,
                "admin@stockchef.com",
                "System",
                "Administrator",
                UserRole.ROLE_ADMIN,
                UserRole.ROLE_ADMIN,
                true,
                LocalDateTime.parse("2025-11-19T19:54:04.123456"),
                LocalDateTime.parse("2025-11-19T20:00:00.000000"),
                "system"
        );

        when(userService.getUserByEmail("admin@stockchef.com"))
                .thenReturn(adminResponse);

        // WHEN: Se llama con usuario ADMIN autenticado
        // THEN: Debe devolver información correcta del ADMIN
        mockMvc.perform(get("/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TestUuidHelper.ADMIN_UUID))
                .andExpect(jsonPath("$.email").value("admin@stockchef.com"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));

        verify(userService, times(1)).getUserByEmail("admin@stockchef.com");
    }
}