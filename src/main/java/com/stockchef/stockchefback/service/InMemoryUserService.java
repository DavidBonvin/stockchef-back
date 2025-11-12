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
 * Servicio temporal para manejar usuarios en memoria
 * Reemplaza la funcionalidad de JPA mientras solucionamos los problemas de configuración
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InMemoryUserService {

    private final PasswordEncoder passwordEncoder;
    private final Map<String, User> users = new HashMap<>();

    /**
     * Inicializar usuarios por defecto
     */
    public void initializeDefaultUsers() {
        if (users.isEmpty()) {
            log.info("Inicializando usuarios en memoria...");

            // Usuario Developer (Super-Admin)
            User developer = createUser(
                1L, 
                "developer@stockchef.com", 
                "devpass123",
                "Developer", 
                "Admin", 
                UserRole.ROLE_DEVELOPER
            );
            users.put(developer.getEmail(), developer);

            // Usuario Admin
            User admin = createUser(
                2L, 
                "admin@stockchef.com", 
                "adminpass123",
                "Admin", 
                "User", 
                UserRole.ROLE_ADMIN
            );
            users.put(admin.getEmail(), admin);

            // Usuario Chef
            User chef = createUser(
                3L, 
                "chef@stockchef.com", 
                "chefpass123",
                "Head", 
                "Chef", 
                UserRole.ROLE_CHEF
            );
            users.put(chef.getEmail(), chef);

            // Usuario Employee
            User employee = createUser(
                4L, 
                "employee@stockchef.com", 
                "emppass123",
                "Kitchen", 
                "Employee", 
                UserRole.ROLE_EMPLOYEE
            );
            users.put(employee.getEmail(), employee);

            log.info("Usuarios inicializados: {}", users.keySet());
        }
    }

    /**
     * Buscar usuario por email
     */
    public Optional<User> findByEmail(String email) {
        initializeDefaultUsers();
        return Optional.ofNullable(users.get(email));
    }

    /**
     * Verificar si existe usuario por email
     */
    public boolean existsByEmail(String email) {
        initializeDefaultUsers();
        return users.containsKey(email);
    }

    /**
     * Crear usuario helper
     */
    private User createUser(Long id, String email, String password, String firstName, String lastName, UserRole role) {
        User user = new User();
        user.setId(id);
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

    /**
     * Obtener todos los usuarios (para debug)
     */
    public Map<String, User> getAllUsers() {
        initializeDefaultUsers();
        return new HashMap<>(users);
    }
}