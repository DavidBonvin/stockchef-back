package com.stockchef.stockchefback.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur de santé pour vérifier l'état du backend
 */
@RestController
@RequestMapping("/health")
@CrossOrigin(origins = "*")
public class HealthController {

    /**
     * Endpoint simple pour vérifier que le backend fonctionne
     * @return ResponseEntity avec informations de l'état
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "StockChef Backend fonctionne correctement");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "0.0.1-SNAPSHOT");
        response.put("service", "stockchef-back");
        
        // Informations du système pour le monitoring
        response.put("server_port", System.getProperty("server.port"));
        response.put("port_env", System.getenv("PORT"));
        response.put("profile", System.getProperty("spring.profiles.active"));
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint pour informations détaillées du backend
     * @return ResponseEntity avec informations détaillées
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "StockChef Backend");
        response.put("description", "Système de gestion d'inventaire et stock");
        response.put("version", "0.0.1-SNAPSHOT");
        response.put("java.version", System.getProperty("java.version"));
        response.put("os.name", System.getProperty("os.name"));
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint pour vérifier la connectivité de base
     * @return String simple
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong - StockChef Backend");
    }
}