package com.stockchef.stockchefback.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Configuration pour l'audit automatique - fournit l'utilisateur actuel
 * Utilisé par @CreatedBy et @LastModifiedBy pour tracer les modifications
 */
@Component
public class SecurityAuditorAware implements AuditorAware<String> {
    
    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.of("SYSTEM"); // Valeur par défaut pour les tests et initialization
        }
        
        // Récupérer l'email de l'utilisateur connecté via JWT
        String userEmail = authentication.getName();
        return Optional.of(userEmail != null ? userEmail : "UNKNOWN");
    }
}