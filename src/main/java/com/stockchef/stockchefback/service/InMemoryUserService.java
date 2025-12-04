package com.stockchef.stockchefback.service;

import com.stockchef.stockchefback.model.User;
import com.stockchef.stockchefback.model.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service temporaire pour gérer les utilisateurs en mémoire
 * DÉSACTIVÉ - Causait des conflits de beans avec UserService
 * Remplace la fonctionnalité de JPA pendant qu'on résout les problèmes de configuration
 */
// @Service - COMMENTÉ POUR ÉVITER CONFLITS
@RequiredArgsConstructor
@Slf4j
public class InMemoryUserService {

    private final PasswordEncoder passwordEncoder;
    private final Map<String, User> users = new HashMap<>();

    /**
     * Initialiser les utilisateurs par défaut
     */
    public void initializeDefaultUsers() {
        if (users.isEmpty()) {
            log.info("Initialisation des utilisateurs en mémoire...");

            // Utilisateur Developer (Super-Admin)
            User developer = createUser(
                1L, 
                "developer@stockchef.com", 
                "devpass123",
                "Developer", 
                "Admin", 
                UserRole.ROLE_DEVELOPER
            );
            users.put(developer.getEmail(), developer);

            // Utilisateur Admin
            User admin = createUser(
                2L, 
                "admin@stockchef.com", 
                "adminpass123",
                "Admin", 
                "User", 
                UserRole.ROLE_ADMIN
            );
            users.put(admin.getEmail(), admin);

            // Utilisateur Chef
            User chef = createUser(
                3L, 
                "chef@stockchef.com", 
                "chefpass123",
                "Head", 
                "Chef", 
                UserRole.ROLE_CHEF
            );
            users.put(chef.getEmail(), chef);

            // Utilisateur Employee
            User employee = createUser(
                4L, 
                "employee@stockchef.com", 
                "emppass123",
                "Kitchen", 
                "Employee", 
                UserRole.ROLE_EMPLOYEE
            );
            users.put(employee.getEmail(), employee);

            log.info("Utilisateurs initialisés: {}", users.keySet());
        }
    }

    /**
     * Rechercher utilisateur par email
     */
    public Optional<User> findByEmail(String email) {
        initializeDefaultUsers();
        return Optional.ofNullable(users.get(email));
    }

    /**
     * Vérifier si utilisateur existe par email
     */
    public boolean existsByEmail(String email) {
        initializeDefaultUsers();
        return users.containsKey(email);
    }

    /**
     * Créer utilisateur helper
     */
    private User createUser(Long id, String email, String password, String firstName, String lastName, UserRole role) {
        return User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy("system")
                .build();
    }

    /**
     * Obtient tous les utilisateurs pour administration
     */
    public Map<String, User> getAllUsers() {
        initializeDefaultUsers();
        return new HashMap<>(users);
    }
}