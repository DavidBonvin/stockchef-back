package com.stockchef.stockchefback.controller;

import com.stockchef.stockchefback.dto.user.RegisterRequest;
import com.stockchef.stockchefback.dto.user.UpdateUserRequest;
import com.stockchef.stockchefback.dto.user.UserResponse;
import com.stockchef.stockchefback.dto.auth.ChangePasswordRequest;
import com.stockchef.stockchefback.dto.auth.ResetPasswordRequest;
import com.stockchef.stockchefback.dto.auth.ForgotPasswordRequest;
import com.stockchef.stockchefback.exception.UserNotFoundException;
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
     * Obtient le profil de l'utilisateur actuellement authentifié
     * Extrait l'email du token JWT et cherche l'utilisateur dans la base de données
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        log.info("Demande de profil d'utilisateur authentifié");
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Utilisateur non authentifié tentant d'accéder au profil");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // L'email vient du token JWT dans le champ 'username' de Spring Security
        String email = authentication.getName();
        log.info("Recherche profil pour email: {}", email);
        
        UserResponse userProfile = userService.getUserByEmail(email);
        
        log.info("Profil trouvé pour utilisateur: {} (ID: {})", 
                userProfile.email(), userProfile.id());
        
        return ResponseEntity.ok(userProfile);
    }

    /**
     * Met à jour les informations personnelles d'un utilisateur
     * - Tous les rôles peuvent modifier leurs propres informations
     * - DEVELOPER et ADMIN peuvent modifier les informations d'autres utilisateurs
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication) {
        
        log.info("Demande de mise à jour pour utilisateur ID: {} par {}", id, authentication.getName());
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Utilisateur non authentifié tentant de mettre à jour profil");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        String currentUserEmail = authentication.getName();
        UserResponse updatedUser = userService.updateUser(id, request, currentUserEmail);
        
        log.info("Utilisateur mis à jour avec succès: {} (ID: {})", 
                updatedUser.email(), updatedUser.id());
        
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Liste tous les utilisateurs du système avec filtres optionnels
     * Seulement accessible pour ADMIN et DEVELOPER
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean active,
            Authentication authentication) {
        
        log.info("Demande de liste d'utilisateurs par {}", authentication.getName());
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Utilisateur non authentifié tentant de lister les utilisateurs");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        String currentUserEmail = authentication.getName();
        List<UserResponse> users = userService.getAllUsers(currentUserEmail, role, active);
        
        log.info("Liste d'utilisateurs retournée avec succès: {} utilisateurs", users.size());
        
        return ResponseEntity.ok(users);
    }

    /**
     * Obtient un utilisateur spécifique par son ID
     * - Les utilisateurs peuvent voir leur propre profil
     * - ADMIN et DEVELOPER peuvent voir n'importe quel profil
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable String id,
            Authentication authentication) {
        
        log.info("Demande d'utilisateur ID: {} par {}", id, authentication.getName());
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Utilisateur non authentifié tentant d'obtenir profil");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        String currentUserEmail = authentication.getName();
        UserResponse user = userService.getUserById(id, currentUserEmail);
        
        log.info("Utilisateur trouvé avec succès: {} (ID: {})", user.email(), user.id());
        
        return ResponseEntity.ok(user);
    }

    /**
     * Supprime (désactive) un utilisateur du système
     * Seulement accessible pour ADMIN
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable String id,
            Authentication authentication) {
        
        log.info("Demande de suppression d'utilisateur ID: {} par {}", id, authentication.getName());
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Utilisateur non authentifié tentant de supprimer utilisateur");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        String currentUserEmail = authentication.getName();
        userService.deleteUser(id, currentUserEmail);
        
        log.info("Utilisateur supprimé avec succès: {}", id);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Changement de mot de passe d'utilisateur spécifique (propre utilisateur + ADMIN)
     * PUT /users/{id}/password
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<Void> changeUserPassword(
            @PathVariable String id,
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String currentUserEmail = authentication.getName();
        userService.changeUserPassword(id, request, currentUserEmail);
        
        log.info("Mot de passe changé pour utilisateur ID: {}", id);
        return ResponseEntity.ok().build();
    }

    /**
     * Reset de mot de passe (seulement ADMIN)
     * POST /users/{id}/reset-password
     */
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Void> resetUserPassword(
            @PathVariable String id,
            @Valid @RequestBody ResetPasswordRequest request,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String currentUserEmail = authentication.getName();
        userService.resetUserPassword(id, request, currentUserEmail);
        
        log.info("Mot de passe reseté pour utilisateur ID: {} par admin", id);
        return ResponseEntity.ok().build();
    }

    /**
     * Changement de mot de passe personnel
     * POST /users/change-password
     */
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePersonalPassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String currentUserEmail = authentication.getName();
        userService.changePersonalPassword(currentUserEmail, request);
        
        log.info("Mot de passe personnel changé pour utilisateur: {}", currentUserEmail);
        return ResponseEntity.ok().build();
    }

    /**
     * Demander reset de mot de passe
     * POST /users/forgot-password
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Demande de reset de mot de passe pour email: {}", request.email());
        
        try {
            userService.requestPasswordReset(request.email());
        } catch (UserNotFoundException e) {
            // Pour la sécurité, ne pas révéler si l'email existe ou non
            log.warn("Demande de reset pour email inexistant: {}", request.email());
        }
        
        // Toujours retourner 200 pour la sécurité, même si l'email n'existe pas
        return ResponseEntity.ok().build();
    }
}