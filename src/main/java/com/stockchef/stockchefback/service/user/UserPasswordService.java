package com.stockchef.stockchefback.service.user;

import com.stockchef.stockchefback.dto.auth.ChangePasswordRequest;
import com.stockchef.stockchefback.dto.auth.ResetPasswordRequest;
import com.stockchef.stockchefback.exception.InvalidPasswordException;
import com.stockchef.stockchefback.exception.InsufficientPermissionsException;
import com.stockchef.stockchefback.exception.UserNotFoundException;
import com.stockchef.stockchefback.model.User;
import com.stockchef.stockchefback.model.UserRole;
import com.stockchef.stockchefback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service spécialisé pour la gestion des mots de passe
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserPasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserAuthorizationService authorizationService;

    /**
     * Change le mot de passe d'un utilisateur (nécessite mot de passe actuel)
     */
    public void changeUserPassword(String userId, ChangePasswordRequest request, String currentUserEmail) {
        log.info("Début changement de mot de passe pour utilisateur ID: {} par utilisateur: {}", userId, currentUserEmail);
        
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé avec ID: " + userId));
        
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur actuel non trouvé"));
        
        // Valider permissions - peut changer son propre mot de passe ou être ADMIN
        if (!targetUser.getId().equals(currentUser.getId()) && currentUser.getRole() != UserRole.ROLE_ADMIN) {
            throw new InsufficientPermissionsException("Vous n'avez pas les permissions pour changer le mot de passe de cet utilisateur");
        }
        
        // Valider mot de passe actuel
        if (!passwordEncoder.matches(request.currentPassword(), targetUser.getPassword())) {
            throw new InvalidPasswordException("Le mot de passe actuel est incorrect");
        }
        
        // Valider nouveau mot de passe
        validatePasswordRequest(request.newPassword(), request.confirmPassword());
        
        // Changer mot de passe
        targetUser.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(targetUser);
        
        log.info("Mot de passe changé avec succès pour utilisateur ID: {}", userId);
    }

    /**
     * Reset de mot de passe par administrateur (sans besoin de mot de passe actuel)
     */
    public void resetUserPassword(String userId, ResetPasswordRequest request, String currentUserEmail) {
        log.info("Début reset de mot de passe pour utilisateur ID: {} par admin: {}", userId, currentUserEmail);
        
        // Seuls les admins peuvent reseter les mots de passe
        authorizationService.requireAdminRole(currentUserEmail);
        
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé avec ID: " + userId));
        
        // Valider nouveau mot de passe (pour reset admin pas besoin de confirmation)
        validatePasswordForReset(request.newPassword());
        
        // Reset mot de passe
        targetUser.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(targetUser);
        
        log.info("Mot de passe reseté avec succès pour utilisateur ID: {} par admin: {}", userId, currentUserEmail);
    }

    /**
     * Changement de mot de passe personnel de l'utilisateur authentifié
     */
    public void changePersonalPassword(String currentUserEmail, ChangePasswordRequest request) {
        log.info("Changement de mot de passe personnel pour utilisateur: {}", currentUserEmail);
        
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));
        
        // Valider mot de passe actuel
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Le mot de passe actuel est incorrect");
        }
        
        // Valider nouveau mot de passe
        validatePasswordRequest(request.newPassword(), request.confirmPassword());
        
        // Cambiar contraseña
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        
        log.info("Contraseña personal cambiada exitosamente para usuario: {}", currentUserEmail);
    }

    /**
     * Solicita un reset de contraseña por email (endpoint público)
     */
    public void requestPasswordReset(String email) {
        log.info("Solicitud de reset de contraseña para email: {}", email);
        
        // Por seguridad, siempre devolvemos OK aunque el usuario no exista
        // Esto evita enumeration attacks
        
        userRepository.findByEmail(email).ifPresentOrElse(
            user -> {
                // Aquí implementarías el envío de email con token de reset
                log.info("Email de reset enviado para usuario existente: {}", email);
                // TODO: Implementar envío de email con token temporal
            },
            () -> log.info("Solicitud de reset para email inexistente: {} - no se envía email", email)
        );
    }

    /**
     * Valida que las contraseñas cumplan los requisitos y coincidan
     */
    private void validatePasswordRequest(String newPassword, String confirmPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new InvalidPasswordException("La nueva contraseña no puede estar vacía");
        }
        
        if (newPassword.length() < 8) {
            throw new InvalidPasswordException("La contraseña debe tener al menos 8 caracteres");
        }
        
        if (!newPassword.equals(confirmPassword)) {
            throw new InvalidPasswordException("Las contraseñas no coinciden");
        }
    }

    /**
     * Valida que la contraseña cumpla los requisitos (para reset de admin)
     */
    private void validatePasswordForReset(String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new InvalidPasswordException("La nueva contraseña no puede estar vacía");
        }
        
        if (newPassword.length() < 8) {
            throw new InvalidPasswordException("La contraseña debe tener al menos 8 caracteres");
        }
    }
}
