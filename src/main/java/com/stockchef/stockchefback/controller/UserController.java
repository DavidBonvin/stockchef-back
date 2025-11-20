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

import java.util.List;

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

    /**
     * Lista todos los usuarios del sistema con filtros opcionales
     * Solo accesible para ADMIN y DEVELOPER
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean active,
            Authentication authentication) {
        
        log.info("Solicitud de lista de usuarios por {}", authentication.getName());
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Usuario no autenticado intentando listar usuarios");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        String currentUserEmail = authentication.getName();
        List<UserResponse> users = userService.getAllUsers(currentUserEmail, role, active);
        
        log.info("Lista de usuarios devuelta exitosamente: {} usuarios", users.size());
        
        return ResponseEntity.ok(users);
    }

    /**
     * Obtiene un usuario específico por su ID
     * - Usuarios pueden ver su propio perfil
     * - ADMIN y DEVELOPER pueden ver cualquier perfil
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable String id,
            Authentication authentication) {
        
        log.info("Solicitud de usuario ID: {} por {}", id, authentication.getName());
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Usuario no autenticado intentando obtener perfil");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        String currentUserEmail = authentication.getName();
        UserResponse user = userService.getUserById(id, currentUserEmail);
        
        log.info("Usuario encontrado exitosamente: {} (ID: {})", user.email(), user.id());
        
        return ResponseEntity.ok(user);
    }

    /**
     * Elimina (desactiva) un usuario del sistema
     * Solo accesible para ADMIN
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable String id,
            Authentication authentication) {
        
        log.info("Solicitud de eliminación de usuario ID: {} por {}", id, authentication.getName());
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Usuario no autenticado intentando eliminar usuario");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        String currentUserEmail = authentication.getName();
        userService.deleteUser(id, currentUserEmail);
        
        log.info("Usuario eliminado exitosamente: {}", id);
        
        return ResponseEntity.noContent().build();
    }
}