package com.stockchef.stockchefback.service;

import com.stockchef.stockchefback.dto.auth.ChangePasswordRequest;
import com.stockchef.stockchefback.dto.auth.ForgotPasswordRequest;
import com.stockchef.stockchefback.dto.auth.ResetPasswordRequest;
import com.stockchef.stockchefback.dto.user.RegisterRequest;
import com.stockchef.stockchefback.dto.user.UpdateUserRequest;
import com.stockchef.stockchefback.dto.user.UserResponse;
import com.stockchef.stockchefback.model.User;
import com.stockchef.stockchefback.model.UserRole;
import com.stockchef.stockchefback.service.user.UserAuthorizationService;
import com.stockchef.stockchefback.service.user.UserManagementService;
import com.stockchef.stockchefback.service.user.UserPasswordService;
import com.stockchef.stockchefback.service.user.UserRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio principal de usuarios - Orquestador
 * 
 * Este servicio actúa como un facade que delega las operaciones
 * a los servicios especializados correspondientes.
 * 
 * Organización modular:
 * - UserRegistrationService: Registro y creación de usuarios
 * - UserPasswordService: Gestión de contraseñas
 * - UserManagementService: CRUD y gestión general
 * - UserAuthorizationService: Autorización y roles
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    // Servicios especializados
    private final UserRegistrationService registrationService;
    private final UserPasswordService passwordService;
    private final UserManagementService managementService;
    private final UserAuthorizationService authorizationService;

    // =================
    // REGISTRO DE USUARIOS
    // =================

    /**
     * Registra un nuevo usuario con el rol EMPLOYEE por defecto
     * Accesible públicamente
     */
    public UserResponse registerNewUser(RegisterRequest request) {
        return registrationService.registerNewUser(request);
    }

    // =================
    // GESTIÓN DE USUARIOS
    // =================

    /**
     * Obtiene todos los usuarios
     */
    public List<UserResponse> getAllUsers() {
        return managementService.getAllUsers();
    }

    /**
     * Obtiene todos los usuarios con filtros
     */
    public List<UserResponse> getAllUsers(String currentUserEmail, String roleFilter, Boolean activeFilter) {
        return managementService.getAllUsers(currentUserEmail, roleFilter, activeFilter);
    }

    /**
     * Obtiene un usuario por email
     */
    public UserResponse getUserByEmail(String email) {
        return managementService.getUserByEmail(email);
    }

    /**
     * Obtiene un usuario por ID con verificación de permisos
     */
    public UserResponse getUserById(String userId, String currentUserEmail) {
        return managementService.getUserById(userId, currentUserEmail);
    }

    /**
     * Actualiza la información de un usuario
     */
    public UserResponse updateUser(String userId, UpdateUserRequest request, String currentUserEmail) {
        return managementService.updateUser(userId, request, currentUserEmail);
    }

    /**
     * Elimina un usuario
     */
    public void deleteUser(String userId, String currentUserEmail) {
        managementService.deleteUser(userId, currentUserEmail);
    }

    // =================
    // GESTIÓN DE ROLES
    // =================

    /**
     * Actualiza el rol de un usuario
     */
    public UserResponse updateUserRole(String userId, UserRole newRole, String reason, User requester) {
        return managementService.updateUserRole(userId, newRole, reason, requester);
    }

    /**
     * Actualiza el rol de un usuario (método legacy)
     */
    public UserResponse updateUserRole(String userId, UserRole newRole, String reason) {
        return managementService.updateUserRole(userId, newRole, reason);
    }

    /**
     * Actualiza el estado activo/inactivo de un usuario
     */
    public UserResponse updateUserStatus(String userId, Boolean active, String reason) {
        return managementService.updateUserStatus(userId, active, reason);
    }

    /**
     * Obtiene el rol efectivo del usuario
     */
    public UserRole getEffectiveRole(User user) {
        return authorizationService.getEffectiveRole(user);
    }

    // =================
    // GESTIÓN DE CONTRASEÑAS
    // =================

    /**
     * Cambia la contraseña de un usuario (requiere contraseña actual)
     * PUT /users/{id}/password
     */
    public void changeUserPassword(String userId, ChangePasswordRequest request, String currentUserEmail) {
        passwordService.changeUserPassword(userId, request, currentUserEmail);
    }

    /**
     * Reset de contraseña por administrador (sin necesidad de contraseña actual)
     * POST /users/{id}/reset-password
     */
    public void resetUserPassword(String userId, ResetPasswordRequest request, String currentUserEmail) {
        passwordService.resetUserPassword(userId, request, currentUserEmail);
    }

    /**
     * Cambio de contraseña personal del usuario autenticado
     * POST /users/change-password
     */
    public void changePersonalPassword(String currentUserEmail, ChangePasswordRequest request) {
        passwordService.changePersonalPassword(currentUserEmail, request);
    }

    /**
     * Solicita un reset de contraseña por email (endpoint público)
     * POST /users/forgot-password
     */
    public void requestPasswordReset(String email) {
        passwordService.requestPasswordReset(email);
    }

    // =================
    // AUTENTICACIÓN Y AUTORIZACIÓN
    // =================

    /**
     * Actualiza el último login del usuario
     */
    public void updateLastLogin(User user) {
        authorizationService.updateLastLogin(user);
    }

    // =================
    // MÉTODOS DE UTILIDAD
    // =================

    /**
     * Verifica que el usuario actual tenga rol de ADMIN
     */
    public void requireAdminRole(String currentUserEmail) {
        authorizationService.requireAdminRole(currentUserEmail);
    }

    /**
     * Obtiene el usuario actual por email
     */
    public User getCurrentUser(String currentUserEmail) {
        return authorizationService.getCurrentUser(currentUserEmail);
    }
}