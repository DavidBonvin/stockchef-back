package com.stockchef.stockchefback.controller;

import com.stockchef.stockchefback.dto.user.RegisterRequest;
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
     * Récupère le profil de l'utilisateur actuellement connecté
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        log.info("Demande de profil utilisateur connecté");
        // Méthode préparée pour l'intégration JWT future
        return ResponseEntity.ok().build();
    }
}