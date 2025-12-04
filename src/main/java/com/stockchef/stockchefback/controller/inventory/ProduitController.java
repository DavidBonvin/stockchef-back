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