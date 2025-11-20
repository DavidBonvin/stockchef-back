package com.stockchef.stockchefback.controller;

import com.stockchef.stockchefback.dto.auth.LoginRequest;
import com.stockchef.stockchefback.dto.auth.LoginResponse;
import com.stockchef.stockchefback.dto.auth.RefreshTokenRequest;
import com.stockchef.stockchefback.exception.InvalidTokenException;
import com.stockchef.stockchefback.model.User;
import com.stockchef.stockchefback.repository.UserRepository;
import com.stockchef.stockchefback.service.AuthService;
import com.stockchef.stockchefback.service.AuthService.TokenResponse;
import com.stockchef.stockchefback.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Contrôleur pour l'authentification des utilisateurs
 * Maintenant utilise UserRepository avec MySQL
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthService authService;

    /**
     * Endpoint pour l'authentification des utilisateurs
     * 
     * @param loginRequest Request avec email et password
     * @return LoginResponse avec token JWT et données de l'utilisateur
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Tentative de connexion pour email: {}", loginRequest.email());
        
        try {
            // Rechercher utilisateur par email avec UserRepository
            Optional<User> userOptional = userRepository.findByEmail(loginRequest.email());
            if (userOptional.isEmpty()) {
                log.warn("Utilisateur non trouvé: {}", loginRequest.email());
                throw new UsernameNotFoundException("Identifiants invalides");
            }
            
            User user = userOptional.get();
            
            // Vérifier que l'utilisateur est actif
            if (!user.getIsActive()) {
                log.warn("Utilisateur inactif tente de se connecter: {}", loginRequest.email());
                throw new BadCredentialsException("Utilisateur inactif");
            }
            
            // Vérifier le mot de passe
            if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
                log.warn("Mot de passe incorrect pour utilisateur: {}", loginRequest.email());
                throw new BadCredentialsException("Identifiants invalides");
            }
            
            // Générer le token JWT
            String token = jwtService.generateToken(user);
            
            // Créer la réponse
            LoginResponse response = new LoginResponse(
                    token,
                    user.getEmail(),
                    user.getFirstName() + " " + user.getLastName(),
                    user.getRole(),
                    86400000L // 24 heures en millisecondes
            );
            
            log.info("Connexion réussie pour utilisateur: {} avec rôle: {}", user.getEmail(), user.getRole());
            return ResponseEntity.ok(response);
            
        } catch (UsernameNotFoundException | BadCredentialsException e) {
            log.error("Erreur d'authentification: {}", e.getMessage());
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            log.error("Erreur interne durant la connexion: ", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Renouveler token JWT
     * POST /auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Demande de renouvellement de token reçue");
        
        try {
            TokenResponse response = authService.refreshToken(request);
            log.info("Token renouvelé avec succès");
            return ResponseEntity.ok(response);
        } catch (InvalidTokenException e) {
            log.warn("Tentative de renouvellement avec token invalide: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Invalider token (logout)
     * POST /auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Tentative de logout sans authentification");
            return ResponseEntity.status(403).build();
        }

        String userEmail = authentication.getName();
        log.info("Logout demandé pour utilisateur: {}", userEmail);
        
        try {
            authService.invalidateToken(userEmail);
            log.info("Logout réussi pour utilisateur: {}", userEmail);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Erreur durant logout pour utilisateur {}: {}", userEmail, e.getMessage());
            throw e;
        }
    }
}