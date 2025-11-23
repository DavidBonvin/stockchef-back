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
 * Service spécialisé pour l'autorisation et gestion des rôles
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserAuthorizationService {

    private final UserRepository userRepository;

    /**
     * Vérifie que l'utilisateur actuel ait le rôle ADMIN
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
     * Vérifie que l'utilisateur puisse accéder à la ressource (propre ressource ou admin)
     */
    public void requireOwnershipOrAdmin(String targetUserId, String currentUserEmail) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario actual no encontrado"));
        
        // Si c'est un admin, peut accéder à tout
        if (currentUser.getRole() == UserRole.ROLE_ADMIN) {
            return;
        }
        
        // Si ce n'est pas un admin, peut seulement accéder à sa propre ressource
        if (!currentUser.getId().equals(targetUserId)) {
            log.warn("Usuario {} intentó acceder al recurso de usuario {} sin permisos", 
                    currentUserEmail, targetUserId);
            throw new UnauthorizedUserException("Solo puedes acceder a tu propia información");
        }
    }

    /**
     * Vérifie que l'utilisateur puisse modifier la ressource (propre ressource ou admin)
     */
    public void requireModificationRights(String targetUserId, String currentUserEmail) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario actual no encontrado"));
        
        // Les admins peuvent modifier n'importe qui
        if (currentUser.getRole() == UserRole.ROLE_ADMIN) {
            return;
        }
        
        // Les utilisateurs peuvent seulement se modifier eux-mêmes
        if (!currentUser.getId().equals(targetUserId)) {
            log.warn("Usuario {} intentó modificar usuario {} sin permisos", 
                    currentUserEmail, targetUserId);
            throw new UnauthorizedUserException("Solo puedes modificar tu propia información");
        }
    }

    /**
     * Obtient le rôle effectif de l'utilisateur
     * - Si actif: retourne le rôle normal
     * - Si inactif: retourne ROLE_EMPLOYEE (rôle dégradé)
     */
    public UserRole getEffectiveRole(User user) {
        if (user == null) {
            return null;
        }
        
        // Si l'utilisateur est inactif, il est dégradé au rôle EMPLOYEE
        if (!user.getIsActive()) {
            return UserRole.ROLE_EMPLOYEE;
        }
        
        return user.getRole();
    }

    /**
     * Vérifie si l'utilisateur peut supprimer un autre utilisateur
     */
    public void requireDeleteRights(String targetUserId, String currentUserEmail) {
        // Seuls les admins peuvent supprimer des utilisateurs
        requireAdminRole(currentUserEmail);
        
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario actual no encontrado"));
        
        // Un admin ne peut pas se supprimer lui-même
        if (currentUser.getId().equals(targetUserId)) {
            throw new InsufficientPermissionsException("Vous ne pouvez pas supprimer votre propre compte");
        }
    }

    /**
     * Met à jour la dernière connexion de l'utilisateur
     */
    @Transactional
    public void updateLastLogin(User user) {
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        log.debug("Último login actualizado para usuario: {}", user.getEmail());
    }

    /**
     * Obtient l'utilisateur actuel par email
     */
    public User getCurrentUser(String currentUserEmail) {
        return userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario actual no encontrado"));
    }

    /**
     * Vérifie si l'utilisateur a les permissions pour voir les listes filtrées
     */
    public boolean canViewFilteredLists(String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        return currentUser.getRole() == UserRole.ROLE_ADMIN;
    }
}
