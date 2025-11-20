package com.stockchef.stockchefback.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de Spring Security para StockChef
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Configurer l'encodeur de mots de passe utilisant BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Désactiver CSRF pour les APIs REST
            .csrf(csrf -> csrf.disable())
            
            // Configurer l'autorisation des requests
            .authorizeHttpRequests(authz -> authz
                // Permettre l'accès sans authentification aux endpoints d'auth
                .requestMatchers("/auth/**").permitAll()
                // Permettre l'accès public à l'inscription d'utilisateurs
                .requestMatchers("/users/register").permitAll()
                // Permettre l'accès public à forgot-password
                .requestMatchers("/users/forgot-password").permitAll()
                // Permettre l'accès aux endpoints de health
                .requestMatchers("/api/health", "/api/health/**").permitAll()
                .requestMatchers("/health", "/health/**").permitAll()
                // Permettre l'accès aux endpoints d'Actuator
                .requestMatchers("/actuator/**").permitAll()
                // Toutes les autres routes nécessitent une authentification
                .anyRequest().authenticated()
            )
            
            // Configurer la gestion des sessions comme STATELESS (pour JWT)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Ajouter le filtre JWT avant le filtre d'authentification utilisateur/mot de passe
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}