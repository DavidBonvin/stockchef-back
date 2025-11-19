package com.stockchef.stockchefback.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de Spring Security para StockChef
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configurar el encoder de passwords usando BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Deshabilitar CSRF para APIs REST
            .csrf(csrf -> csrf.disable())
            
            // Configurar autorización de requests
            .authorizeHttpRequests(authz -> authz
                // Permitir acceso sin autenticación a endpoints de auth
                .requestMatchers("/auth/**").permitAll()
                // Permitir acceso a endpoints de health
                .requestMatchers("/api/health", "/api/health/**").permitAll()
                .requestMatchers("/health", "/health/**").permitAll()
                // Permitir acceso a endpoints de Actuator
                .requestMatchers("/actuator/**").permitAll()
                // Todas las demás rutas requieren autenticación
                .anyRequest().permitAll() // En desarrollo - cambiar a .authenticated() en producción
            )
            
            // Configurar manejo de sesiones como STATELESS (para JWT)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        
        return http.build();
    }
}