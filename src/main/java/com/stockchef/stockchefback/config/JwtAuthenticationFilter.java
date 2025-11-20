package com.stockchef.stockchefback.config;

import com.stockchef.stockchefback.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtre JWT qui intercepte toutes les requests pour vérifier et traiter les tokens JWT
 * S'exécute une fois par request pour établir l'authentification dans SecurityContext
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // S'il n'y a pas de header Authorization ou ne commence pas par "Bearer ", continuer sans authentification
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extraire le token JWT (supprimer "Bearer " du début)
            final String jwt = authHeader.substring(7);
            log.debug("Token JWT extrait de la request: {}", jwt.substring(0, Math.min(jwt.length(), 20)) + "...");

            // Valider le token et extraire l'email
            if (!jwtService.isTokenExpired(jwt)) {
                String userEmail = jwtService.extractEmail(jwt);
                log.debug("Token valide pour utilisateur: {}", userEmail);

                // S'il n'y a pas d'authentification préalable dans le contexte
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    
                    // Extraire le rôle du token pour établir les authorities
                    String role = jwtService.extractRole(jwt);
                    List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

                    // Créer le token d'authentification
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userEmail, // Principal (username)
                            null,      // Credentials (not needed for JWT)
                            authorities // Authorities from token
                    );

                    // Établir les détails de la request
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Établir l'authentification dans SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    log.debug("Authentification établie pour utilisateur: {} avec rôle: {}", userEmail, role);
                }
            } else {
                log.warn("Token JWT invalide ou expiré");
            }
        } catch (Exception e) {
            log.error("Erreur lors du traitement du token JWT: {}", e.getMessage(), e);
            // On ne lance pas l'exception, on logue seulement et on continue sans authentification
        }

        // Continuer avec la chaîne de filtres
        filterChain.doFilter(request, response);
    }
}