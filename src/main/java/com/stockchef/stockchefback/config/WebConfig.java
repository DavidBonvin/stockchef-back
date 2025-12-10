package com.stockchef.stockchefback.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration CORS pour permettre les requêtes depuis le frontend
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                    // Desarrollo local
                    "http://localhost:3000", 
                    "http://localhost:4200", 
                    "http://localhost:5173",
                    "http://localhost:5173/",
                    // Producción (añadir tu URL de frontend aquí)
                    "https://stockchef-front.vercel.app",
                    "https://*.vercel.app",
                    "https://*.netlify.app"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                //.allowedHeaders("*")
                .allowCredentials(true) // Cambiar a false cuando uses "*" en origins
                .maxAge(3600);
    }
}