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
 * Service spécialisé pour la gestion et opérations CRUD d'utilisateurs
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserManagementService {

    private final UserRepository userRepository;
    private final UserAuthorizationService authorizationService;

    /**
     * Obtient tous les utilisateurs (avec filtres optionnels)
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers(String currentUserEmail, String roleFilter, Boolean activeFilter) {
        log.info("Demande de liste d'utilisateurs par: {}, filtres - rôle: {}, actif: {}", 
                currentUserEmail, roleFilter, activeFilter);

        // Vérifier permissions - seuls les admins peuvent voir les listes filtrées
        if (!authorizationService.canViewFilteredLists(currentUserEmail)) {
            throw new UnauthorizedUserException("Vous n'avez pas les permissions pour voir la liste complète d'utilisateurs");
        }

        List<User> users = userRepository.findAll();

        // Appliquer filtres si fournis
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

        // Vérifier permissions d'accès
        authorizationService.requireOwnershipOrAdmin(userId, currentUserEmail);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé avec ID: " + userId));

        log.info("Utilisateur trouvé: {}", user.getEmail());
        return convertToUserResponse(user);
    }

    /**
     * Obtiene un usuario por email
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        log.info("Recherche utilisateur par email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé avec email: " + email));
        
        log.info("Utilisateur trouvé par email: {}", email);
        return convertToUserResponse(user);
    }

    /**
     * Actualiza la información de un usuario
     */
    public UserResponse updateUser(String userId, UpdateUserRequest request, String currentUserEmail) {
        log.info("Actualización de usuario ID: {} por usuario: {}", userId, currentUserEmail);

        // Vérifier permissions de modification
        authorizationService.requireModificationRights(userId, currentUserEmail);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé avec ID: " + userId));

        // Mettre à jour champs fournis
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
        log.info("Mise à jour rôle utilisateur ID: {} vers {} par utilisateur: {}, raison: {}", 
                userId, newRole, requester.getEmail(), reason);

        // Solo admins pueden cambiar roles
        if (requester.getRole() != UserRole.ROLE_ADMIN) {
            throw new UnauthorizedUserException("Seuls les administrateurs peuvent changer les rôles d'utilisateur");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé avec ID: " + userId));

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
        log.info("Mise à jour statut utilisateur ID: {} vers actif={}, raison: {}", userId, active, reason);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé avec ID: " + userId));

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

        // Vérifier permissions de suppression
        authorizationService.requireDeleteRights(userId, currentUserEmail);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé avec ID: " + userId));

        userRepository.delete(user);
        log.info("Utilisateur supprimé avec succès: {} (ID: {})", user.getEmail(), userId);
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