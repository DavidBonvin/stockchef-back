package com.stockchef.stockchefback.config;

import com.stockchef.stockchefback.service.AuthService;
import com.stockchef.stockchefback.service.JwtService;
import com.stockchef.stockchefback.service.user.UserAuthorizationService;
import com.stockchef.stockchefback.service.user.UserManagementService;
import com.stockchef.stockchefback.service.user.UserPasswordService;
import com.stockchef.stockchefback.service.user.UserRegistrationService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration de test pour résoudre les problèmes d'injection de dépendances
 * DÉSACTIVÉE - Causait des conflits de beans
 */
// @TestConfiguration - COMMENTÉ POUR ÉVITER CONFLITS
// @Profile("test")
public class TestConfig {

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    public AuthService authService() {
        return Mockito.mock(AuthService.class);
    }

    @Bean
    @Primary
    public JwtService jwtService() {
        return Mockito.mock(JwtService.class);
    }

    @Bean
    @Primary
    public UserRegistrationService userRegistrationService() {
        return Mockito.mock(UserRegistrationService.class);
    }

    @Bean
    @Primary
    public UserPasswordService userPasswordService() {
        return Mockito.mock(UserPasswordService.class);
    }

    @Bean
    @Primary
    public UserManagementService userManagementService() {
        return Mockito.mock(UserManagementService.class);
    }

    @Bean
    @Primary
    public UserAuthorizationService userAuthorizationService() {
        return Mockito.mock(UserAuthorizationService.class);
    }
}