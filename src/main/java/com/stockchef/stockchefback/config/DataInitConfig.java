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
 * Configuración para inicializar datos de usuario en la base de datos
 * Crea usuarios por defecto para cada rol si la base de datos está vacía
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitConfig implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Inicialización de datos de usuarios...");

        // Insertar solo si no hay usuarios existentes
        if (userRepository.count() == 0) {
            createDefaultUsers();
        } else {
            log.info("Los usuarios ya existen en la base de datos. Total: {}", userRepository.count());
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
        log.info("Usuario Developer creado: {}", developer.getEmail());

        // Utilisateur Admin
        User admin = createUser(
            "admin@stockchef.com", 
            "adminpass123",
            "Admin", 
            "User", 
            UserRole.ROLE_ADMIN
        );
        userRepository.save(admin);
        log.info("Usuario Admin creado: {}", admin.getEmail());

        // Utilisateur Chef
        User chef = createUser(
            "chef@stockchef.com", 
            "chefpass123",
            "Head", 
            "Chef", 
            UserRole.ROLE_CHEF
        );
        userRepository.save(chef);
        log.info("Usuario Chef creado: {}", chef.getEmail());

        // Utilisateur Employee
        User employee = createUser(
            "employee@stockchef.com", 
            "emppass123",
            "Kitchen", 
            "Employee", 
            UserRole.ROLE_EMPLOYEE
        );
        userRepository.save(employee);
        log.info("Usuario Employee creado: {}", employee.getEmail());

        log.info("Todos los usuarios han sido creados exitosamente.");
    }

    private User createUser(String email, String password, String firstName, String lastName, UserRole role) {
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
}