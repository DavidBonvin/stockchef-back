package com.stockchef.stockchefback.service.user;

import com.stockchef.stockchefback.exception.InsufficientPermissionsException;
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

/**
 * Servicio especializado para autorización y gestión de roles
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserAuthorizationService {

    private final UserRepository userRepository;

    /**
     * Verifica que el usuario actual tenga rol de ADMIN
     */
    public void requireAdminRole(String currentUserEmail) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario actual no encontrado"));
        
        if (currentUser.getRole() != UserRole.ROLE_ADMIN) {
            log.warn("Usuario {} intentó realizar acción de admin sin permisos", currentUserEmail);
            throw new InsufficientPermissionsException("Se requieren permisos de administrador");
        }
    }

    /**
     * Verifica que el usuario pueda acceder al recurso (propio recurso o admin)
     */
    public void requireOwnershipOrAdmin(String targetUserId, String currentUserEmail) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario actual no encontrado"));
        
        // Si es admin, puede acceder a todo
        if (currentUser.getRole() == UserRole.ROLE_ADMIN) {
            return;
        }
        
        // Si no es admin, solo puede acceder a su propio recurso
        if (!currentUser.getId().equals(targetUserId)) {
            log.warn("Usuario {} intentó acceder al recurso de usuario {} sin permisos", 
                    currentUserEmail, targetUserId);
            throw new UnauthorizedUserException("Solo puedes acceder a tu propia información");
        }
    }

    /**
     * Verifica que el usuario pueda modificar el recurso (propio recurso o admin)
     */
    public void requireModificationRights(String targetUserId, String currentUserEmail) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario actual no encontrado"));
        
        // Los admins pueden modificar a cualquiera
        if (currentUser.getRole() == UserRole.ROLE_ADMIN) {
            return;
        }
        
        // Los usuarios solo pueden modificarse a sí mismos
        if (!currentUser.getId().equals(targetUserId)) {
            log.warn("Usuario {} intentó modificar usuario {} sin permisos", 
                    currentUserEmail, targetUserId);
            throw new UnauthorizedUserException("Solo puedes modificar tu propia información");
        }
    }

    /**
     * Obtiene el rol efectivo del usuario
     */
    public UserRole getEffectiveRole(User user) {
        if (user == null || !user.getIsActive()) {
            return null;
        }
        return user.getRole();
    }

    /**
     * Verifica si el usuario puede eliminar a otro usuario
     */
    public void requireDeleteRights(String targetUserId, String currentUserEmail) {
        // Solo admins pueden eliminar usuarios
        requireAdminRole(currentUserEmail);
        
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario actual no encontrado"));
        
        // Un admin no puede eliminarse a sí mismo
        if (currentUser.getId().equals(targetUserId)) {
            throw new InsufficientPermissionsException("No puedes eliminar tu propia cuenta");
        }
    }

    /**
     * Actualiza el último login del usuario
     */
    @Transactional
    public void updateLastLogin(User user) {
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        log.debug("Último login actualizado para usuario: {}", user.getEmail());
    }

    /**
     * Obtiene el usuario actual por email
     */
    public User getCurrentUser(String currentUserEmail) {
        return userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario actual no encontrado"));
    }

    /**
     * Verifica si el usuario tiene permisos para ver listados filtrados
     */
    public boolean canViewFilteredLists(String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        return currentUser.getRole() == UserRole.ROLE_ADMIN;
    }
}
