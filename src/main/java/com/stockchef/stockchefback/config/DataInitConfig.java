package com.stockchef.stockchefback.config;

import com.stockchef.stockchefback.model.User;
import com.stockchef.stockchefback.model.UserRole;
import com.stockchef.stockchefback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Configuration pour initialiser les données dans MySQL
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitConfig implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Initialisation des données utilisateurs dans MySQL...");

        // Insérer seulement s'il n'y a pas d'utilisateurs
        if (userRepository.count() == 0) {
            createDefaultUsers();
        } else {
            log.info("Les utilisateurs existent déjà dans la base de données. Total: {}", userRepository.count());
        }
    }

    private void createDefaultUsers() {
        // Utilisateur Developer (Super-Admin)
        User developer = createUser(
            "developer@stockchef.com", 
            "devpass123",
            "Developer", 
            "Admin", 
            UserRole.ROLE_DEVELOPER
        );
        userRepository.save(developer);
        log.info("Utilisateur Developer créé: {}", developer.getEmail());

        // Utilisateur Admin
        User admin = createUser(
            "admin@stockchef.com", 
            "adminpass123",
            "Admin", 
            "User", 
            UserRole.ROLE_ADMIN
        );
        userRepository.save(admin);
        log.info("Utilisateur Admin créé: {}", admin.getEmail());

        // Utilisateur Chef
        User chef = createUser(
            "chef@stockchef.com", 
            "chefpass123",
            "Head", 
            "Chef", 
            UserRole.ROLE_CHEF
        );
        userRepository.save(chef);
        log.info("Utilisateur Chef créé: {}", chef.getEmail());

        // Utilisateur Employee
        User employee = createUser(
            "employee@stockchef.com", 
            "emppass123",
            "Kitchen", 
            "Employee", 
            UserRole.ROLE_EMPLOYEE
        );
        userRepository.save(employee);
        log.info("Utilisateur Employee créé: {}", employee.getEmail());

        log.info("Tous les utilisateurs ont été créés avec succès.");
    }

    private User createUser(String email, String password, String firstName, String lastName, UserRole role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}