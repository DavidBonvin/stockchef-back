package com.stockchef.stockchefback.service.user;

import com.stockchef.stockchefback.dto.user.RegisterRequest;
import com.stockchef.stockchefback.dto.user.UserResponse;
import com.stockchef.stockchefback.exception.EmailAlreadyExistsException;
import com.stockchef.stockchefback.model.User;
import com.stockchef.stockchefback.model.UserRole;
import com.stockchef.stockchefback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service spécialisé pour l'inscription et création d'utilisateurs
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Inscrit un nouvel utilisateur avec le rôle EMPLOYEE par défaut
     * Accessible publiquement
     */
    public UserResponse registerNewUser(RegisterRequest request) {
        log.info("Tentative d'enregistrement pour email: {}", request.email());

        // Vérifier que l'email n'existe pas déjà
        if (userRepository.findByEmail(request.email()).isPresent()) {
            log.warn("Tentative d'inscription avec email déjà existant: {}", request.email());
            throw new EmailAlreadyExistsException("L'email " + request.email() + " est déjà utilisé");
        }

        // Créer utilisateur
        User newUser = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.ROLE_EMPLOYEE) // Rôle par défaut
                .isActive(true) // Actif par défaut
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("Usuario registrado exitosamente con ID: {} y email: {}", savedUser.getId(), savedUser.getEmail());

        return convertToUserResponse(savedUser);
    }

    private UserResponse convertToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getRole(), // effectiveRole igual al rol para nuevos usuarios
                user.getIsActive(),
                user.getCreatedAt(),
                user.getLastLoginAt(),
                user.getCreatedBy()
        );
    }
}
