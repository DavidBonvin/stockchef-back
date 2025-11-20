package com.stockchef.stockchefback.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur pour la racine de l'application
 */
@RestController
@CrossOrigin(origins = "*")
public class RootController {

    /**
     * Endpoint à la racine pour les health checks de Railway
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "StockChef Backend API fonctionne");
        response.put("timestamp", LocalDateTime.now());
        response.put("api_docs", "/api/actuator");
        response.put("health_check", "/api/health");
        response.put("auth_endpoint", "/api/auth/login");
        
        // Información del sistema para monitoreo
        response.put("server_port", System.getProperty("server.port"));
        response.put("port_env", System.getenv("PORT"));
        response.put("profile", System.getProperty("spring.profiles.active"));
        
        return ResponseEntity.ok(response);
    }
}