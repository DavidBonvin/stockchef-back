package com.stockchef.stockchefback.service.user;

import com.stockchef.stockchefback.dto.user.UpdateUserRequest;
import com.stockchef.stockchefback.dto.user.UserResponse;
import com.stockchef.stockchefback.exception.UnauthorizedUserException;
import com.stockchef.stockchefback.exception.UserNotFoundException;
import com.stockchef.stockchefback.model.User;
import com.stockchef.stockchefback.model.UserRole;
import com.stockchef.stockchefback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio especializado para la gestión y operaciones CRUD de usuarios
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserManagementService {

    private final UserRepository userRepository;
    private final UserAuthorizationService authorizationService;

    /**
     * Obtiene todos los usuarios (con filtros opcionales)
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers(String currentUserEmail, String roleFilter, Boolean activeFilter) {
        log.info("Solicitud de lista de usuarios por: {}, filtros - rol: {}, activo: {}", 
                currentUserEmail, roleFilter, activeFilter);

        // Verificar permisos - solo admins pueden ver listas filtradas
        if (!authorizationService.canViewFilteredLists(currentUserEmail)) {
            throw new UnauthorizedUserException("No tienes permisos para ver la lista completa de usuarios");
        }

        List<User> users = userRepository.findAll();

        // Aplicar filtros si se proporcionan
        if (roleFilter != null && !roleFilter.isEmpty()) {
            try {
                UserRole role = UserRole.valueOf("ROLE_" + roleFilter.toUpperCase());
                users = users.stream()
                        .filter(user -> user.getRole() == role)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                log.warn("Rol inválido proporcionado: {}", roleFilter);
            }
        }

        if (activeFilter != null) {
            users = users.stream()
                    .filter(user -> user.getIsActive().equals(activeFilter))
                    .collect(Collectors.toList());
        }

        return users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los usuarios (versión simple sin filtros)
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.info("Obteniendo lista simple de todos los usuarios");
        
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un usuario por ID con verificación de permisos
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(String userId, String currentUserEmail) {
        log.info("Solicitud de usuario ID: {} por usuario: {}", userId, currentUserEmail);

        // Verificar permisos de acceso
        authorizationService.requireOwnershipOrAdmin(userId, currentUserEmail);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));

        log.info("Usuario encontrado: {}", user.getEmail());
        return convertToUserResponse(user);
    }

    /**
     * Obtiene un usuario por email
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        log.info("Buscando usuario por email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con email: " + email));
        
        log.info("Usuario encontrado por email: {}", email);
        return convertToUserResponse(user);
    }

    /**
     * Actualiza la información de un usuario
     */
    public UserResponse updateUser(String userId, UpdateUserRequest request, String currentUserEmail) {
        log.info("Actualización de usuario ID: {} por usuario: {}", userId, currentUserEmail);

        // Verificar permisos de modificación
        authorizationService.requireModificationRights(userId, currentUserEmail);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));

        // Actualizar campos proporcionados
        if (request.firstName() != null && !request.firstName().trim().isEmpty()) {
            user.setFirstName(request.firstName().trim());
        }
        
        if (request.lastName() != null && !request.lastName().trim().isEmpty()) {
            user.setLastName(request.lastName().trim());
        }

        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);

        log.info("Usuario actualizado exitosamente: {}", updatedUser.getEmail());
        return convertToUserResponse(updatedUser);
    }

    /**
     * Actualiza el rol de un usuario
     */
    public UserResponse updateUserRole(String userId, UserRole newRole, String reason, User requester) {
        log.info("Actualizando rol de usuario ID: {} a {} por usuario: {}, razón: {}", 
                userId, newRole, requester.getEmail(), reason);

        // Solo admins pueden cambiar roles
        if (requester.getRole() != UserRole.ROLE_ADMIN) {
            throw new UnauthorizedUserException("Solo los administradores pueden cambiar roles de usuario");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));

        UserRole previousRole = user.getRole();
        user.setRole(newRole);
        user.setUpdatedAt(LocalDateTime.now());
        
        User updatedUser = userRepository.save(user);
        
        log.info("Rol de usuario {} actualizado de {} a {} por {}, razón: {}", 
                user.getEmail(), previousRole, newRole, requester.getEmail(), reason);
        
        return convertToUserResponse(updatedUser);
    }

    /**
     * Actualiza el rol de un usuario (versión con email de requester)
     */
    public UserResponse updateUserRole(String userId, UserRole newRole, String reason) {
        // Obtener el usuario actual del contexto de seguridad
        // Por ahora usaremos un método que requiere el email del requester
        log.warn("Método updateUserRole sin requester llamado - considerado deprecated");
        throw new UnsupportedOperationException("Este método requiere el requester explícito");
    }

    /**
     * Actualiza el estado activo/inactivo de un usuario
     */
    public UserResponse updateUserStatus(String userId, Boolean active, String reason) {
        log.info("Actualizando estado de usuario ID: {} a activo={}, razón: {}", userId, active, reason);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));

        Boolean previousStatus = user.getIsActive();
        user.setIsActive(active);
        user.setUpdatedAt(LocalDateTime.now());
        
        User updatedUser = userRepository.save(user);
        
        log.info("Estado de usuario {} actualizado de {} a {}, razón: {}", 
                user.getEmail(), previousStatus, active, reason);
        
        return convertToUserResponse(updatedUser);
    }

    /**
     * Elimina un usuario (solo admins)
     */
    public void deleteUser(String userId, String currentUserEmail) {
        log.info("Solicitud de eliminación de usuario ID: {} por usuario: {}", userId, currentUserEmail);

        // Verificar permisos de eliminación
        authorizationService.requireDeleteRights(userId, currentUserEmail);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));

        userRepository.delete(user);
        log.info("Usuario eliminado exitosamente: {} (ID: {})", user.getEmail(), userId);
    }

    /**
     * Convierte un User a UserResponse
     */
    private UserResponse convertToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                authorizationService.getEffectiveRole(user), // usar rol efectivo
                user.getIsActive(),
                user.getCreatedAt(),
                user.getLastLoginAt(),
                user.getCreatedBy()
        );
    }
}