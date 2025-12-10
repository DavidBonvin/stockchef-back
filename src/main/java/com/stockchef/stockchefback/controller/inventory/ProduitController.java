package com.stockchef.stockchefback.controller.inventory;

import com.stockchef.stockchefback.dto.inventory.*;
import com.stockchef.stockchefback.model.inventory.Produit;
import com.stockchef.stockchefback.service.inventory.ProduitService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des produits
 * Endpoints sécurisés selon les rôles utilisateurs
 */
@RestController
@RequestMapping("/inventory/produits")
public class ProduitController {
    
    private final ProduitService produitService;
    
    public ProduitController(ProduitService produitService) {
        this.produitService = produitService;
    }
    
    /**
     * Création d'un nouveau produit
     * Accessible aux CHEF et ADMIN
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('CHEF', 'ADMIN', 'DEVELOPER')")
    public ResponseEntity<ProduitResponse> createProduit(@Valid @RequestBody ProduitCreateRequest request) {
        ProduitResponse produit = produitService.createProduit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(produit);
    }
    
    /**
     * Liste de tous les produits
     * Accessible aux CHEF, ADMIN et DEVELOPER
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('CHEF', 'ADMIN', 'DEVELOPER')")
    public ResponseEntity<List<ProduitResponse>> getAllProduits() {
        List<ProduitResponse> produits = produitService.getAllProduits();
        return ResponseEntity.ok(produits);
    }
    
    /**
     * Liste paginée des produits
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('CHEF', 'ADMIN', 'DEVELOPER')")
    public ResponseEntity<Page<ProduitResponse>> getAllProduitsPageable(Pageable pageable) {
        Page<ProduitResponse> produits = produitService.getAllProduitsPageable(pageable);
        return ResponseEntity.ok(produits);
    }
    
    /**
     * Endpoint de informes - Resumen básico de inventario
     */
    @GetMapping("/inventory-summary")
    @PreAuthorize("hasAnyRole('CHEF', 'ADMIN', 'DEVELOPER', 'ASSISTANT')")
    public ResponseEntity<java.util.Map<String, Object>> getInventorySummary() {
        java.util.Map<String, Object> summary = new java.util.HashMap<>();
        
        List<ProduitResponse> allProducts = produitService.getAllProduits();
        
        summary.put("status", "success");
        summary.put("totalProduits", allProducts.size());
        summary.put("message", "Resumen de inventario generado");
        summary.put("timestamp", java.time.LocalDateTime.now());
        summary.put("products", allProducts);
        
        return ResponseEntity.ok(summary);
    }
    
    /**
     * Endpoint de informes - Productos próximos a expirar
     */
    @GetMapping("/expiring-soon")
    @PreAuthorize("hasAnyRole('CHEF', 'ADMIN', 'DEVELOPER', 'ASSISTANT')")
    public ResponseEntity<java.util.Map<String, Object>> getExpiringProducts(
            @RequestParam(defaultValue = "7") int days) {
        
        java.util.Map<String, Object> report = new java.util.HashMap<>();
        List<ProduitResponse> allProducts = produitService.getAllProduits();
        
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate limitDate = today.plusDays(days);
        
        List<ProduitResponse> expiringProducts = allProducts.stream()
            .filter(p -> p.datePeremption() != null && 
                        p.datePeremption().isBefore(limitDate) && 
                        !p.datePeremption().isBefore(today))
            .collect(java.util.stream.Collectors.toList());
        
        report.put("status", "success");
        report.put("message", "Productos próximos a expirar en " + days + " días");
        report.put("daysFilter", days);
        report.put("today", today);
        report.put("limitDate", limitDate);
        report.put("totalProductsExpiring", expiringProducts.size());
        report.put("expiringProducts", expiringProducts);
        report.put("timestamp", java.time.LocalDateTime.now());
        
        return ResponseEntity.ok(report);
    }
    
    /**
     * Endpoint de informes - Estadísticas por unidad
     */
    @GetMapping("/stats-by-unit")
    @PreAuthorize("hasAnyRole('CHEF', 'ADMIN', 'DEVELOPER', 'ASSISTANT')")
    public ResponseEntity<java.util.Map<String, Object>> getStatsByUnit() {
        
        java.util.Map<String, Object> report = new java.util.HashMap<>();
        List<ProduitResponse> allProducts = produitService.getAllProduits();
        
        java.util.Map<String, java.util.List<ProduitResponse>> productsByUnit = allProducts.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                product -> product.unite().name()
            ));
        
        java.util.Map<String, Object> unitStats = new java.util.HashMap<>();
        productsByUnit.forEach((unit, products) -> {
            java.util.Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("count", products.size());
            stats.put("totalStock", products.stream()
                .mapToDouble(p -> p.quantiteStock().doubleValue())
                .sum());
            stats.put("averagePrice", products.stream()
                .mapToDouble(p -> p.prixUnitaire().doubleValue())
                .average().orElse(0.0));
            unitStats.put(unit, stats);
        });
        
        report.put("status", "success");
        report.put("message", "Estadísticas por unidad de medida");
        report.put("totalUnits", unitStats.size());
        report.put("unitStatistics", unitStats);
        report.put("timestamp", java.time.LocalDateTime.now());
        
        return ResponseEntity.ok(report);
    }
    
    /**
     * Détails d'un produit par ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CHEF', 'ADMIN', 'DEVELOPER')")
    public ResponseEntity<ProduitResponse> getProduitById(@PathVariable Long id) {
        ProduitResponse produit = produitService.getProduitById(id);
        return ResponseEntity.ok(produit);
    }
    
    /**
     * Mise à jour d'un produit
     * Accessible aux CHEF et ADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CHEF', 'ADMIN', 'DEVELOPER')")
    public ResponseEntity<ProduitResponse> updateProduit(
            @PathVariable Long id,
            @Valid @RequestBody ProduitUpdateRequest request) {
        ProduitResponse produit = produitService.updateProduit(id, request);
        return ResponseEntity.ok(produit);
    }
    
    /**
     * UC2: Sortie de stock (décrémentation)
     * Accessible aux CHEF uniquement
     */
    @PostMapping("/{id}/sortie")
    @PreAuthorize("hasAnyRole('CHEF', 'ADMIN', 'DEVELOPER')")
    public ResponseEntity<ProduitResponse> sortieStock(
            @PathVariable Long id,
            @Valid @RequestBody StockMovementRequest request) {
        ProduitResponse produit = produitService.sortieStock(id, request);
        return ResponseEntity.ok(produit);
    }
    
    /**
     * Entrée de stock (incrémentation)
     * Accessible aux CHEF et ADMIN
     */
    @PostMapping("/{id}/entree")
    @PreAuthorize("hasAnyRole('CHEF', 'ADMIN', 'DEVELOPER')")
    public ResponseEntity<ProduitResponse> entreeStock(
            @PathVariable Long id,
            @Valid @RequestBody StockMovementRequest request) {
        ProduitResponse produit = produitService.entreeStock(id, request);
        return ResponseEntity.ok(produit);
    }
    
    /**
     * Suppression d'un produit (soft delete)
     * Accessible aux CHEF, ADMIN et DEVELOPER
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CHEF', 'ADMIN', 'DEVELOPER')")
    public ResponseEntity<Void> deleteProduit(@PathVariable Long id) {
        produitService.deleteProduit(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * UC5: Liste des produits en alerte (sous seuil)
     * Accessible aux CHEF, ADMIN et DEVELOPER
     */
    @GetMapping("/alerts")
    @PreAuthorize("hasAnyRole('CHEF', 'ADMIN', 'DEVELOPER')")
    public ResponseEntity<List<ProduitResponse>> getProduitsEnAlerte() {
        List<ProduitResponse> produits = produitService.getProduitsUnderAlertThreshold();
        return ResponseEntity.ok(produits);
    }
    
    /**
     * Liste des produits qui expirent bientôt
     */
    @GetMapping("/expiring")
    @PreAuthorize("hasAnyRole('CHEF', 'ADMIN', 'DEVELOPER')")
    public ResponseEntity<List<ProduitResponse>> getProduitsExpiringWithinDays(
            @RequestParam(defaultValue = "7") int days) {
        List<ProduitResponse> produits = produitService.getProduitsExpiringWithinDays(days);
        return ResponseEntity.ok(produits);
    }
    
    /**
     * Recherche de produits par nom
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('CHEF', 'ADMIN', 'DEVELOPER')")
    public ResponseEntity<List<ProduitResponse>> searchProduits(@RequestParam String nom) {
        List<ProduitResponse> produits = produitService.searchProduitsByNom(nom);
        return ResponseEntity.ok(produits);
    }
    
    /**
     * Historique des mouvements de stock pour un produit
     */
    @GetMapping("/{id}/movements")
    @PreAuthorize("hasAnyRole('CHEF', 'ADMIN', 'DEVELOPER')")
    public ResponseEntity<List<StockMovementResponse>> getStockMovements(@PathVariable Long id) {
        List<StockMovementResponse> movements = produitService.getStockMovements(id);
        return ResponseEntity.ok(movements);
    }
}