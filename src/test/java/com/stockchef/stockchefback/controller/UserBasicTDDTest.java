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
 * Vérifie que le endpoint retourne l'information correcte de l'utilisateur authentifié
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
    @DisplayName("TDD RED: Doit retourner 403 quand il n'y a pas d'authentification")
    void test_step1_red_noAuthentication_returns401() throws Exception {
        // GIVEN: Sin autenticación
        
        // WHEN: Se llama al endpoint /users/me sin autenticación
        // THEN: Doit retourner 403 Forbidden (comportement de Spring Security)
        mockMvc.perform(get("/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());

        // Vérifier que le service n'a pas été appelé
        verifyNoInteractions(userService);
    }

    // ==================== GREEN TESTS (Pasan con implementación mínima) ====================
    
    @Test
    @WithMockUser(username = "developer@stockchef.com", roles = {"DEVELOPER"})
    @DisplayName("TDD GREEN: Doit retourner information utilisateur quand authentifié")
    void test_step2_green_authenticated_returnsUserInfo() throws Exception {
        // GIVEN: Utilisateur authentifié et existant dans la base de données
        when(userService.getUserByEmail("developer@stockchef.com"))
                .thenReturn(expectedUserResponse);

        // WHEN: On appelle le endpoint /users/me avec utilisateur authentifié
        // THEN: Doit retourner 200 avec l'information de l'utilisateur
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

        // Vérifier que le service a été appelé correctement
        verify(userService, times(1)).getUserByEmail("developer@stockchef.com");
    }

    // ==================== REFACTOR TESTS (Casos adicionales) ====================
    
    @Test
    @WithMockUser(username = "noexiste@stockchef.com", roles = {"EMPLOYEE"})
    @DisplayName("TDD REFACTOR: Doit retourner 404 quand utilisateur n'existe pas en BD")
    void test_step3_refactor_userNotFound_returns404() throws Exception {
        // GIVEN: Utilisateur authentifié mais n'existe pas en BD
        String emailNoExistente = "noexiste@stockchef.com";
        
        when(userService.getUserByEmail(emailNoExistente))
                .thenThrow(new UserNotFoundException("Utilisateur non trouvé avec email: " + emailNoExistente));

        // WHEN: On appelle le endpoint avec utilisateur inexistant
        // THEN: Doit retourner 404
        mockMvc.perform(get("/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserByEmail(emailNoExistente);
    }

    @Test
    @WithMockUser(username = "admin@stockchef.com", roles = {"ADMIN"})
    @DisplayName("TDD REFACTOR: Doit fonctionner pour différents rôles d'utilisateur")
    void test_step4_refactor_differentRoles_worksCorrectly() throws Exception {
        // GIVEN: Utilisateur ADMIN
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

        // WHEN: On appelle avec utilisateur ADMIN authentifié
        // THEN: Doit retourner information correcte de l'ADMIN
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