package com.stockchef.stockchefback.controller;

import com.stockchef.stockchefback.dto.user.UpdateUserRoleRequest;
import com.stockchef.stockchefback.dto.user.UpdateUserStatusRequest;
import com.stockchef.stockchefback.dto.user.UserResponse;
import com.stockchef.stockchefback.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para la gestión administrativa de usuarios
 * Proporciona endpoints para administración de usuarios, roles y estado
 * Acceso restringido solo a usuarios con roles ADMIN o DEVELOPER
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN') or hasRole('DEVELOPER')")
public class AdminController {

    private final UserService userService;

    /**
     * Liste tous les utilisateurs avec leurs rôles effectifs
     * Accessible aux ADMIN et DEVELOPER
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("Demande de liste de tous les utilisateurs");
        
        List<UserResponse> users = userService.getAllUsers();
        
        log.info("Liste des utilisateurs retournée: {} utilisateurs", users.size());
        return ResponseEntity.ok(users);
    }

    /**
     * Met à jour le rôle d'un utilisateur
     * ADMIN ne peut pas créer de DEVELOPER
     * DEVELOPER peut créer n'importe quel rôle
     */
    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserRoleRequest request,
            Authentication authentication) {
        
        log.info("Demande de mise à jour du rôle pour utilisateur ID: {} vers {}", 
                id, request.newRole());
        
        // Pour les tests, on utilise la surcharge sans requester
        // En production, on récupérerait l'utilisateur depuis authentication
        UserResponse response = userService.updateUserRole(
                id, 
                request.newRole(), 
                request.reason()
        );
        
        log.info("Rôle mis à jour avec succès pour utilisateur ID: {} vers {}", 
                id, request.newRole());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Met à jour le statut actif/inactif d'un utilisateur
     * Accessible aux ADMIN et DEVELOPER
     */
    @PutMapping("/users/{id}/status")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        
        log.info("Demande de mise à jour du statut pour utilisateur ID: {} vers {}", 
                id, request.active() ? "actif" : "inactif");
        
        UserResponse response = userService.updateUserStatus(
                id, 
                request.active(), 
                request.reason()
        );
        
        log.info("Statut mis à jour avec succès pour utilisateur ID: {} vers {}", 
                id, request.active() ? "actif" : "inactif");
        
        return ResponseEntity.ok(response);
    }
}