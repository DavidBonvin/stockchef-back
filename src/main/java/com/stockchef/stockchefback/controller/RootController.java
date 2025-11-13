package com.stockchef.stockchefback.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para la raíz de la aplicación
 */
@RestController
@CrossOrigin(origins = "*")
public class RootController {

    /**
     * Endpoint en la raíz para Railway health checks
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "StockChef Backend API está funcionando");
        response.put("timestamp", LocalDateTime.now());
        response.put("api_docs", "/api/actuator");
        response.put("health_check", "/api/health");
        response.put("auth_endpoint", "/api/auth/login");
        
        // Debug info para Railway
        response.put("server_port", System.getProperty("server.port"));
        response.put("port_env", System.getenv("PORT"));
        response.put("profile", System.getProperty("spring.profiles.active"));
        
        return ResponseEntity.ok(response);
    }
}