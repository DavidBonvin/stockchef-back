package com.stockchef.stockchefback.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

/**
 * Contrôleur de test pour vérifier que le backend fonctionne
 */
@RestController
@RequestMapping("/test")
public class TestController {
    
    @GetMapping("/hello")
    public ResponseEntity<Map<String, String>> hello() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "StockChef Backend fonctionne!");
        response.put("status", "OK");
        response.put("port", "8090");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/auth-mock")
    public ResponseEntity<Map<String, Object>> mockAuth(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        String email = request.get("email");
        String password = request.get("password");
        
        // Mock authentication
        if ("developer@stockchef.com".equals(email) && "devpass123".equals(password)) {
            response.put("token", "mock-jwt-token-developer");
            response.put("email", email);
            response.put("fullName", "Super Admin");
            response.put("role", "ROLE_DEVELOPER");
            response.put("expiresIn", 86400000);
        } else if ("admin@stockchef.com".equals(email) && "adminpass123".equals(password)) {
            response.put("token", "mock-jwt-token-admin");
            response.put("email", email);
            response.put("fullName", "Admin User");
            response.put("role", "ROLE_ADMIN");
            response.put("expiresIn", 86400000);
        } else {
            response.put("error", "Identifiants invalides");
            return ResponseEntity.status(401).body(response);
        }
        
        return ResponseEntity.ok(response);
    }
}