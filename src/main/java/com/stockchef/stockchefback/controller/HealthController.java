package com.stockchef.stockchefback.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de salud para verificar el estado del backend
 */
@RestController
@RequestMapping("/health")
@CrossOrigin(origins = "*")
public class HealthController {

    /**
     * Endpoint simple para verificar que el backend está funcionando
     * @return ResponseEntity con información del estado
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "StockChef Backend está funcionando correctamente");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "0.0.1-SNAPSHOT");
        response.put("service", "stockchef-back");
        
        // Información del sistema para monitoreo
        response.put("server_port", System.getProperty("server.port"));
        response.put("port_env", System.getenv("PORT"));
        response.put("profile", System.getProperty("spring.profiles.active"));
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para información detallada del backend
     * @return ResponseEntity con información detallada
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "StockChef Backend");
        response.put("description", "Sistema de gestión de inventario y stock");
        response.put("version", "0.0.1-SNAPSHOT");
        response.put("java.version", System.getProperty("java.version"));
        response.put("os.name", System.getProperty("os.name"));
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para verificar conectividad básica
     * @return String simple
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong - StockChef Backend");
    }
}