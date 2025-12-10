package com.stockchef.stockchefback.controller;

import com.stockchef.stockchefback.repository.inventory.ProduitRepository;
import com.stockchef.stockchefback.model.inventory.Produit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour la racine de l'application
 */
@RestController
@CrossOrigin(origins = "*")
public class RootController {

    @Autowired
    private ProduitRepository produitRepository;

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

    /**
     * Endpoint de prueba simple para informes
     */
    @GetMapping("/test-reports")
    public ResponseEntity<String> testReports() {
        return ResponseEntity.ok("Test endpoint funcionando correctamente");
    }

    /**
     * Endpoint para obtener un resumen rápido de inventarios
     */
    @GetMapping("/api/reports/inventory-summary")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CHEF') or hasRole('ROLE_ASSISTANT')")
    public ResponseEntity<Map<String, Object>> getInventorySummary() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            long totalProduits = produitRepository.count();
            
            response.put("status", "success");
            response.put("totalProduits", totalProduits);
            response.put("message", "Résumé de l'inventaire disponible");
            response.put("timestamp", LocalDateTime.now());
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Erreur lors de la récupération des données: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para obtener todos los productos 
     */
    @GetMapping("/api/reports/all-products")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_CHEF') or hasRole('ROLE_ASSISTANT')")
    public ResponseEntity<Map<String, Object>> getAllProducts() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Produit> allProducts = produitRepository.findAll();
            
            response.put("status", "success");
            response.put("totalProduits", allProducts.size());
            response.put("products", allProducts);
            response.put("timestamp", LocalDateTime.now());
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Erreur lors de la récupération des produits: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}