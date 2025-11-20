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
 * Servicio especializado para la gestión de contraseñas
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
     * Cambia la contraseña de un usuario (requiere contraseña actual)
     */
    public void changeUserPassword(String userId, ChangePasswordRequest request, String currentUserEmail) {
        log.info("Inicio cambio de contraseña para usuario ID: {} por usuario: {}", userId, currentUserEmail);
        
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));
        
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario actual no encontrado"));
        
        // Validar permisos - puede cambiar su propia contraseña o ser ADMIN
        if (!targetUser.getId().equals(currentUser.getId()) && currentUser.getRole() != UserRole.ROLE_ADMIN) {
            throw new InsufficientPermissionsException("No tienes permisos para cambiar la contraseña de este usuario");
        }
        
        // Validar contraseña actual
        if (!passwordEncoder.matches(request.currentPassword(), targetUser.getPassword())) {
            throw new InvalidPasswordException("La contraseña actual es incorrecta");
        }
        
        // Validar nueva contraseña
        validatePasswordRequest(request.newPassword(), request.confirmPassword());
        
        // Cambiar contraseña
        targetUser.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(targetUser);
        
        log.info("Contraseña cambiada exitosamente para usuario ID: {}", userId);
    }

    /**
     * Reset de contraseña por administrador (sin necesidad de contraseña actual)
     */
    public void resetUserPassword(String userId, ResetPasswordRequest request, String currentUserEmail) {
        log.info("Inicio reset de contraseña para usuario ID: {} por admin: {}", userId, currentUserEmail);
        
        // Solo admins pueden resetear contraseñas
        authorizationService.requireAdminRole(currentUserEmail);
        
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + userId));
        
        // Validar nueva contraseña (para reset de admin no se requiere confirmación)
        validatePasswordForReset(request.newPassword());
        
        // Reset contraseña
        targetUser.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(targetUser);
        
        log.info("Contraseña reseteada exitosamente para usuario ID: {} por admin: {}", userId, currentUserEmail);
    }

    /**
     * Cambio de contraseña personal del usuario autenticado
     */
    public void changePersonalPassword(String currentUserEmail, ChangePasswordRequest request) {
        log.info("Cambio de contraseña personal para usuario: {}", currentUserEmail);
        
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        
        // Validar contraseña actual
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("La contraseña actual es incorrecta");
        }
        
        // Validar nueva contraseña
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
