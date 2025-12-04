package com.stockchef.stockchefback.service.inventory;

import com.stockchef.stockchefback.dto.inventory.*;
import com.stockchef.stockchefback.model.inventory.Produit;
import com.stockchef.stockchefback.model.inventory.StockMovement;
import com.stockchef.stockchefback.repository.inventory.ProduitRepository;
import com.stockchef.stockchefback.repository.inventory.StockMovementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des produits
 * Orchestration de la logique métier et mapping DTOs
 */
@Service
@Transactional
public class ProduitService {
    
    private static final Logger log = LoggerFactory.getLogger(ProduitService.class);
    
    private final ProduitRepository produitRepository;
    private final StockMovementRepository stockMovementRepository;
    private final StockService stockService;
    
    public ProduitService(ProduitRepository produitRepository,
                         StockMovementRepository stockMovementRepository,
                         StockService stockService) {
        this.produitRepository = produitRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.stockService = stockService;
    }
    
    /**
     * Crée un nouveau produit
     */
    public ProduitResponse createProduit(ProduitCreateRequest request) {
        log.info("Création d'un nouveau produit: {}", request.nom());
        
        Produit produit = new Produit(
            request.nom(),
            request.quantiteInitiale(),
            request.unite(),
            request.prixUnitaire(),
            request.seuilAlerte()
        );
        
        if (request.datePeremption() != null) {
            produit.setDatePeremption(request.datePeremption());
        }
        
        // Sauvegarder le produit (avec la quantité initiale déjà dans l'entité)
        Produit saved = produitRepository.save(produit);
        
        // Créer un mouvement d'entrée initial pour l'audit trail SEULEMENT
        if (request.quantiteInitiale().compareTo(java.math.BigDecimal.ZERO) > 0) {
            stockService.createInitialStockMovement(
                saved.getId(),
                request.quantiteInitiale(),
                "Stock initial à la création"
            );
        }
        
        log.info("Produit créé avec succès - ID: {}", saved.getId());
        return mapToProduitResponse(saved);
    }
    
    /**
     * Récupère tous les produits
     */
    @Transactional(readOnly = true)
    public List<ProduitResponse> getAllProduits() {
        return produitRepository.findAll().stream()
                .map(this::mapToProduitResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère tous les produits avec pagination
     */
    @Transactional(readOnly = true)
    public Page<ProduitResponse> getAllProduitsPageable(Pageable pageable) {
        return produitRepository.findAll(pageable)
                .map(this::mapToProduitResponse);
    }
    
    /**
     * Récupère un produit par son ID
     */
    @Transactional(readOnly = true)
    public ProduitResponse getProduitById(Long id) {
        Produit produit = findProduitById(id);
        return mapToProduitResponse(produit);
    }
    
    /**
     * Met à jour un produit existant
     */
    public ProduitResponse updateProduit(Long id, ProduitUpdateRequest request) {
        log.info("Mise à jour du produit ID: {}", id);
        
        Produit produit = findProduitById(id);
        
        // Mise à jour des champs modifiables
        if (request.nom() != null && !request.nom().trim().isEmpty()) {
            produit.setNom(request.nom());
        }
        if (request.prixUnitaire() != null) {
            produit.setPrixUnitaire(request.prixUnitaire());
        }
        if (request.seuilAlerte() != null) {
            produit.setSeuilAlerte(request.seuilAlerte());
        }
        if (request.datePeremption() != null) {
            produit.setDatePeremption(request.datePeremption());
        }
        
        Produit updated = produitRepository.save(produit);
        log.info("Produit mis à jour avec succès - ID: {}", id);
        
        return mapToProduitResponse(updated);
    }
    
    /**
     * UC2: Sortie de stock (décrémentation)
     */
    public ProduitResponse sortieStock(Long id, StockMovementRequest request) {
        log.info("Sortie de stock - Produit ID: {}, Quantité: {}", id, request.quantite());
        
        // Utiliser StockService pour la logique métier
        boolean isUnderThreshold;
        if (request.unite() != null) {
            isUnderThreshold = stockService.decrementerStockAvecConversion(
                id, request.quantite(), request.unite(), request.motif(), request.menuId()
            );
        } else {
            isUnderThreshold = stockService.decrementerStock(
                id, request.quantite(), request.motif(), request.menuId()
            );
        }
        
        if (isUnderThreshold) {
            log.warn("Alerte: Le produit ID {} est passé sous le seuil d'alerte", id);
        }
        
        Produit produit = findProduitById(id);
        return mapToProduitResponse(produit);
    }
    
    /**
     * Entrée de stock (incrémentation)
     */
    public ProduitResponse entreeStock(Long id, StockMovementRequest request) {
        log.info("Entrée de stock - Produit ID: {}, Quantité: {}", id, request.quantite());
        
        // Utiliser StockService pour la logique métier
        stockService.incrementerStock(id, request.quantite(), request.motif());
        
        Produit produit = findProduitById(id);
        return mapToProduitResponse(produit);
    }
    
    /**
     * Suppression d'un produit (soft delete)
     */
    public void deleteProduit(Long id) {
        log.info("Suppression du produit ID: {}", id);
        
        Produit produit = findProduitById(id);
        produit.setDeleted(true);
        produitRepository.save(produit);
        
        log.info("Produit supprimé avec succès - ID: {}", id);
    }
    
    /**
     * UC5: Produits sous seuil d'alerte
     */
    @Transactional(readOnly = true)
    public List<ProduitResponse> getProduitsUnderAlertThreshold() {
        return produitRepository.findProduitsUnderAlertThreshold().stream()
                .map(this::mapToProduitResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Produits qui expirent dans X jours
     */
    @Transactional(readOnly = true)
    public List<ProduitResponse> getProduitsExpiringWithinDays(int days) {
        return produitRepository.findProduitsExpiringWithinDays(days).stream()
                .map(this::mapToProduitResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Recherche de produits par nom
     */
    @Transactional(readOnly = true)
    public List<ProduitResponse> searchProduitsByNom(String nom) {
        return produitRepository.findByNomContainingIgnoreCase(nom).stream()
                .map(this::mapToProduitResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Historique des mouvements de stock
     */
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getStockMovements(Long produitId) {
        Produit produit = findProduitById(produitId);
        return stockMovementRepository.findByProduitOrderByDateMouvementDesc(produit).stream()
                .map(this::mapToStockMovementResponse)
                .collect(Collectors.toList());
    }
    
    // Helper methods
    
    private Produit findProduitById(Long id) {
        return produitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produit non trouvé avec l'ID: " + id));
    }
    
    private ProduitResponse mapToProduitResponse(Produit produit) {
        return new ProduitResponse(
            produit.getId(),
            produit.getNom(),
            produit.getQuantiteStock(),
            produit.getUnite(),
            produit.getPrixUnitaire(),
            produit.getSeuilAlerte(),
            produit.getDatePeremption(),
            produit.getDateEntree(),
            produit.getLastModified(),
            produit.isUnderAlertThreshold(),
            produit.isExpired(),
            null // description - à ajouter plus tard
        );
    }
    
    private StockMovementResponse mapToStockMovementResponse(StockMovement movement) {
        return new StockMovementResponse(
            movement.getId(),
            movement.getProduit().getNom(),
            movement.getTypeMouvement(),
            movement.getQuantite(),
            movement.getQuantiteApres(),
            movement.getMotif(),
            movement.getMenuId(),
            movement.getDateMouvement(),
            movement.getUtilisateur()
        );
    }
}