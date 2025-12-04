package com.stockchef.stockchefback.service.inventory;

import com.stockchef.stockchefback.model.inventory.Produit;
import com.stockchef.stockchefback.model.inventory.StockMovement;
import com.stockchef.stockchefback.model.inventory.TypeMouvement;
import com.stockchef.stockchefback.model.inventory.Unite;
import com.stockchef.stockchefback.repository.inventory.ProduitRepository;
import com.stockchef.stockchefback.repository.inventory.StockMovementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Service central pour la gestion des stocks
 * Gère les mouvements de stock avec audit trail complet
 */
@Service
@Transactional
public class StockService {
    
    private static final Logger log = LoggerFactory.getLogger(StockService.class);
    
    private final ProduitRepository produitRepository;
    private final StockMovementRepository stockMovementRepository;
    private final UniteConversionService uniteConversionService;
    
    public StockService(ProduitRepository produitRepository, 
                       StockMovementRepository stockMovementRepository,
                       UniteConversionService uniteConversionService) {
        this.produitRepository = produitRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.uniteConversionService = uniteConversionService;
    }
    
    /**
     * UC2: Décrémente le stock d'un produit avec audit trail
     * 
     * @param produitId ID du produit
     * @param quantite Quantité à retirer (dans l'unité du produit)
     * @param motif Raison de la sortie
     * @param menuId ID du menu associé (optionnel)
     * @return true si le stock passe sous le seuil d'alerte, false sinon
     * @throws IllegalArgumentException si les paramètres sont invalides
     * @throws IllegalStateException si le stock est insuffisant
     */
    public boolean decrementerStock(Long produitId, BigDecimal quantite, String motif, Long menuId) {
        validateParameters(produitId, quantite, motif);
        
        log.info("Décrémentation stock - Produit: {}, Quantité: {}, Motif: {}", produitId, quantite, motif);
        
        // Récupérer le produit
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new IllegalArgumentException("Produit non trouvé avec l'ID: " + produitId));
        
        // Vérifier le stock disponible
        if (!produit.hasSufficientStock(quantite)) {
            throw new IllegalStateException(
                String.format("Stock insuffisant pour le produit '%s'. Disponible: %s %s, Demandé: %s %s",
                            produit.getNom(),
                            produit.getQuantiteStock(),
                            produit.getUnite().getSymbol(),
                            quantite,
                            produit.getUnite().getSymbol())
            );
        }
        
        // Calculer le nouveau stock
        BigDecimal nouveauStock = produit.getQuantiteStock().subtract(quantite);
        
        // Mettre à jour le stock
        produit.setQuantiteStock(nouveauStock);
        produitRepository.save(produit);
        
        // Créer le mouvement de stock pour audit
        StockMovement movement = StockMovement.createSortie(
            produit, quantite, produit.getUnite(), nouveauStock, motif, menuId
        );
        stockMovementRepository.save(movement);
        
        // Vérifier le seuil d'alerte
        boolean isUnderThreshold = produit.isUnderAlertThreshold();
        if (isUnderThreshold) {
            log.warn("ALERTE: Le produit '{}' est passé sous le seuil d'alerte. Stock: {} {}, Seuil: {} {}",
                    produit.getNom(),
                    nouveauStock,
                    produit.getUnite().getSymbol(),
                    produit.getSeuilAlerte(),
                    produit.getUnite().getSymbol());
        }
        
        log.info("Stock décrémenté avec succès - Nouveau stock: {} {}", nouveauStock, produit.getUnite().getSymbol());
        return isUnderThreshold;
    }
    
    /**
     * Incrémente le stock d'un produit avec audit trail
     * 
     * @param produitId ID du produit
     * @param quantite Quantité à ajouter (dans l'unité du produit)
     * @param motif Raison de l'entrée
     */
    public void incrementerStock(Long produitId, BigDecimal quantite, String motif) {
        validateParameters(produitId, quantite, motif);
        
        log.info("Incrémentation stock - Produit: {}, Quantité: {}, Motif: {}", produitId, quantite, motif);
        
        // Récupérer le produit
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new IllegalArgumentException("Produit non trouvé avec l'ID: " + produitId));
        
        // Calculer le nouveau stock
        BigDecimal nouveauStock = produit.getQuantiteStock().add(quantite);
        
        // Mettre à jour le stock
        produit.setQuantiteStock(nouveauStock);
        produitRepository.save(produit);
        
        // Créer le mouvement de stock pour audit
        StockMovement movement = StockMovement.createEntree(
            produit, quantite, produit.getUnite(), nouveauStock, motif
        );
        stockMovementRepository.save(movement);
        
        log.info("Stock incrémenté avec succès - Nouveau stock: {} {}", nouveauStock, produit.getUnite().getSymbol());
    }
    
    /**
     * Décrémente le stock avec conversion d'unité automatique
     * 
     * @param produitId ID du produit
     * @param quantite Quantité à retirer
     * @param uniteQuantite Unité de la quantité fournie
     * @param motif Raison de la sortie
     * @param menuId ID du menu associé (optionnel)
     * @return true si le stock passe sous le seuil d'alerte
     */
    @Transactional
    public boolean decrementerStockAvecConversion(Long produitId, BigDecimal quantite, 
                                                Unite uniteQuantite, String motif, Long menuId) {
        validateParameters(produitId, quantite, motif);
        
        if (uniteQuantite == null) {
            throw new IllegalArgumentException("L'unité de la quantité ne peut pas être nulle");
        }
        
        // Récupérer le produit pour connaître son unité
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new IllegalArgumentException("Produit non trouvé avec l'ID: " + produitId));
        
        // Convertir la quantité dans l'unité du produit si nécessaire
        BigDecimal quantiteConvertie = uniteConversionService.convertir(quantite, uniteQuantite, produit.getUnite());
        
        log.info("Conversion effectuée: {} {} -> {} {} pour le produit '{}'",
                quantite, uniteQuantite.getSymbol(),
                quantiteConvertie, produit.getUnite().getSymbol(),
                produit.getNom());
        
        // Vérifier le stock suffisant avec la quantité convertie
        if (!produit.hasSufficientStock(quantiteConvertie)) {
            log.warn("Stock insuffisant pour le produit '{}': demandé {} {}, disponible {} {}",
                    produit.getNom(), quantite, uniteQuantite.getSymbol(),
                    produit.getQuantiteStock(), produit.getUnite().getSymbol());
            return false;
        }
        
        // Calculer le nouveau stock
        BigDecimal nouveauStock = produit.getQuantiteStock().subtract(quantiteConvertie);
        
        // Mettre à jour le stock
        produit.setQuantiteStock(nouveauStock);
        produitRepository.save(produit);
        
        // Créer le mouvement de stock avec l'unité originale de la demande
        StockMovement movement = StockMovement.createSortie(
            produit, quantite, uniteQuantite, nouveauStock, motif, menuId
        );
        stockMovementRepository.save(movement);
        
        log.info("Stock décrémenté avec conversion pour le produit '{}': -{} {} (converti en -{} {})",
                produit.getNom(), quantite, uniteQuantite.getSymbol(),
                quantiteConvertie, produit.getUnite().getSymbol());
        
        return produit.isUnderAlertThreshold();
    }
    
    /**
     * Valide les paramètres communs
     */
    private void validateParameters(Long produitId, BigDecimal quantite, String motif) {
        if (produitId == null) {
            throw new IllegalArgumentException("L'ID du produit ne peut pas être nul");
        }
        
        if (quantite == null) {
            throw new IllegalArgumentException("La quantité ne peut pas être nulle");
        }
        
        if (quantite.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La quantité doit être positive");
        }
        
        if (motif == null || motif.trim().isEmpty()) {
            throw new IllegalArgumentException("Le motif ne peut pas être nul ou vide");
        }
    }
    
    /**
     * Crée un mouvement de stock initial lors de la création d'un produit
     * Sans modifier la quantité du produit (déjà définie)
     * 
     * @param produitId ID du produit
     * @param quantiteInitiale Quantité initiale pour l'audit trail
     * @param motif Motif du mouvement
     */
    public void createInitialStockMovement(Long produitId, BigDecimal quantiteInitiale, String motif) {
        log.info("Création du mouvement de stock initial pour le produit {}", produitId);
        
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new IllegalArgumentException("Produit non trouvé avec l'ID: " + produitId));
        
        // Créer le mouvement d'audit SANS modifier le stock (déjà défini dans l'entité)
        StockMovement movement = new StockMovement(
                produit,
                TypeMouvement.ENTREE,
                quantiteInitiale,
                produit.getUnite(),
                produit.getQuantiteStock(), // La quantité après est celle déjà présente dans l'entité
                motif
        );
        
        stockMovementRepository.save(movement);
        
        log.info("Mouvement de stock initial créé pour le produit '{}': +{} {} (stock final: {})",
                produit.getNom(), quantiteInitiale, produit.getUnite().getSymbol(), produit.getQuantiteStock());
    }
}