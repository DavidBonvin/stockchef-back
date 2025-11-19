package com.stockchef.stockchefback.controller;

import com.stockchef.stockchefback.dto.user.RegisterRequest;
import com.stockchef.stockchefback.dto.user.UpdateUserRequest;
import com.stockchef.stockchefback.dto.user.UserResponse;
import com.stockchef.stockchefback.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur pour la gestion publique des utilisateurs
 * Endpoints accessibles publiquement ou par les utilisateurs authentifiés
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private final UserService userService;

    /**
     * Enregistrement public d'un nouvel utilisateur
     * N'importe qui peut créer un compte avec le rôle EMPLOYEE par défaut
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterRequest request) {
        log.info("Demande d'enregistrement pour email: {}", request.email());
        
        UserResponse response = userService.registerNewUser(request);
        
        log.info("Utilisateur enregistré avec succès: {} (ID: {})", 
                response.email(), response.id());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Récupère un utilisateur par son ID UUID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        log.info("Demande de récupération utilisateur ID: {}", id);
        
        // Méthode préparée pour l'intégration future avec findById()
        log.warn("Fonctionnalité getUserById non encore implémentée pour ID: {}", id);
        return ResponseEntity.notFound().build();
    }

    /**
     * Obtiene el perfil del usuario actualmente autenticado
     * Extrae el email del JWT token y busca el usuario en la base de datos
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        log.info("Solicitud de perfil de usuario autenticado");
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Usuario no autenticado intentando acceder al perfil");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // El email viene del JWT token en el campo 'username' de Spring Security
        String email = authentication.getName();
        log.info("Buscando perfil para email: {}", email);
        
        UserResponse userProfile = userService.getUserByEmail(email);
        
        log.info("Perfil encontrado para usuario: {} (ID: {})", 
                userProfile.email(), userProfile.id());
        
        return ResponseEntity.ok(userProfile);
    }

    /**
     * Actualiza la información personal de un usuario
     * - Todos los roles pueden modificar su propia información
     * - DEVELOPER y ADMIN pueden modificar información de otros usuarios
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication) {
        
        log.info("Solicitud de actualización para usuario ID: {} por {}", id, authentication.getName());
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Usuario no autenticado intentando actualizar perfil");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String currentUserEmail = authentication.getName();
        UserResponse updatedUser = userService.updateUser(id, request, currentUserEmail);
        
        log.info("Usuario actualizado exitosamente: {} (ID: {})", 
                updatedUser.email(), updatedUser.id());
        
        return ResponseEntity.ok(updatedUser);
    }
}