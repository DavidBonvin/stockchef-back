package com.stockchef.stockchefback.service.menu;

import com.stockchef.stockchefback.model.inventory.Produit;
import com.stockchef.stockchefback.model.inventory.Unite;
import com.stockchef.stockchefback.model.menu.Menu;
import com.stockchef.stockchefback.model.menu.MenuIngredient;
import com.stockchef.stockchefback.repository.inventory.ProduitRepository;
import com.stockchef.stockchefback.repository.menu.MenuIngredientRepository;
import com.stockchef.stockchefback.repository.menu.MenuRepository;
import com.stockchef.stockchefback.service.inventory.StockService;
import com.stockchef.stockchefback.service.inventory.UniteConversionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service spécialisé pour la gestion des ingrédients de menus
 * 
 * RESPONSABILITÉS:
 * - Ajout et suppression d'ingrédients
 * - Conversion d'unités pour validation de stock
 * - Coordination avec le système de stock
 * - Calcul des coûts d'ingrédients
 */
@Service
@Transactional
public class MenuIngredientService {
    
    private static final Logger log = LoggerFactory.getLogger(MenuIngredientService.class);
    
    private final MenuRepository menuRepository;
    private final MenuIngredientRepository menuIngredientRepository;
    private final ProduitRepository produitRepository;
    private final StockService stockService;
    private final UniteConversionService uniteConversionService;
    
    public MenuIngredientService(MenuRepository menuRepository,
                                MenuIngredientRepository menuIngredientRepository,
                                ProduitRepository produitRepository,
                                StockService stockService,
                                UniteConversionService uniteConversionService) {
        this.menuRepository = menuRepository;
        this.menuIngredientRepository = menuIngredientRepository;
        this.produitRepository = produitRepository;
        this.stockService = stockService;
        this.uniteConversionService = uniteConversionService;
    }
    
    /**
     * Ajoute un ingrédient à un menu avec conversion d'unités et validation de stock
     */
    public MenuIngredient ajouterIngredient(Long menuId, Long produitId, 
                                           BigDecimal quantiteNecessaire, Unite uniteUtilisee, String notes) {
        log.info("Ajout ingrédient au menu {} - Produit: {}, Quantité: {} {}", 
                menuId, produitId, quantiteNecessaire, uniteUtilisee);
        
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("Menu non trouvé: " + menuId));
        
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new IllegalArgumentException("Produit non trouvé: " + produitId));
        
        // Validation: menu modifiable
        if (menu.getStatut().name().equals("CONFIRME") || menu.getStatut().name().equals("PREPARE")) {
            throw new IllegalStateException("Impossible de modifier un menu confirmé ou préparé");
        }
        
        // Validation: pas de doublon
        boolean ingredientExists = menu.getIngredients().stream()
                .anyMatch(ing -> ing.getProduit().getId().equals(produitId));
        if (ingredientExists) {
            throw new IllegalArgumentException("Ingrédient déjà présent dans le menu");
        }
        
        // CONVERSION D'UNITÉS CRITIQUE
        BigDecimal quantiteConvertieStockUnit;
        try {
            if (uniteUtilisee.equals(produit.getUnite())) {
                quantiteConvertieStockUnit = quantiteNecessaire;
            } else {
                quantiteConvertieStockUnit = uniteConversionService.convertir(
                    quantiteNecessaire, uniteUtilisee, produit.getUnite()
                );
            }
        } catch (Exception e) {
            log.error("Erreur de conversion d'unités: {} {} -> {} pour produit {}", 
                    quantiteNecessaire, uniteUtilisee, produit.getUnite(), produitId);
            throw new IllegalArgumentException("Conversion d'unités impossible: " + e.getMessage());
        }
        
        // VALIDATION DE STOCK
        if (!produit.hasSufficientStock(quantiteConvertieStockUnit)) {
            throw new IllegalStateException(
                String.format("Stock insuffisant pour %s. Disponible: %s %s, Demandé: %s %s", 
                        produit.getNom(), 
                        produit.getQuantiteStock(), produit.getUnite(),
                        quantiteConvertieStockUnit, produit.getUnite())
            );
        }
        
        // Calcul du coût
        BigDecimal coutIngredient = quantiteConvertieStockUnit.multiply(produit.getPrixUnitaire());
        
        // Création de l'ingrédient
        MenuIngredient ingredient = new MenuIngredient(
                menu, produit, quantiteNecessaire, uniteUtilisee, notes
        );
        ingredient.setQuantiteConvertieStockUnit(quantiteConvertieStockUnit);
        ingredient.setCoutIngredient(coutIngredient);
        
        MenuIngredient savedIngredient = menuIngredientRepository.save(ingredient);
        
        // Mise à jour du coût total du menu
        mettreAJourCoutTotalMenu(menu);
        
        log.info("Ingrédient ajouté avec succès - Menu: {}, Produit: {}, Coût: {}", 
                menuId, produitId, coutIngredient);
        
        return savedIngredient;
    }
    
    /**
     * Supprime un ingrédient d'un menu
     */
    public void supprimerIngredient(Long menuId, Long produitId) {
        log.info("Suppression ingrédient - Menu: {}, Produit: {}", menuId, produitId);
        
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("Menu non trouvé: " + menuId));
        
        if (menu.getStatut().name().equals("CONFIRME") || menu.getStatut().name().equals("PREPARE")) {
            throw new IllegalStateException("Impossible de modifier un menu confirmé ou préparé");
        }
        
        MenuIngredient ingredient = menu.getIngredients().stream()
                .filter(ing -> ing.getProduit().getId().equals(produitId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ingrédient non trouvé dans le menu"));
        
        // Supprimer l'ingrédient de la collection du menu d'abord
        menu.getIngredients().remove(ingredient);
        
        // Puis supprimer de la base de données
        menuIngredientRepository.delete(ingredient);
        
        // Recharger le menu pour éviter les problèmes de cache Hibernate
        Menu menuRecharge = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("Menu non trouvé après suppression: " + menuId));
        
        // Mise à jour du coût total avec le menu rechargé
        mettreAJourCoutTotalMenu(menuRecharge);
        
        log.info("Ingrédient supprimé avec succès - Menu: {}, Produit: {}", menuId, produitId);
    }
    
    /**
     * Vérifie si le stock est suffisant pour tous les ingrédients d'un menu
     */
    public boolean verifierStockSuffisant(Menu menu) {
        log.debug("Vérification stock pour menu: {}", menu.getId());
        
        for (MenuIngredient ingredient : menu.getIngredients()) {
            Produit produit = ingredient.getProduit();
            BigDecimal quantiteRequise = ingredient.getQuantiteConvertieStockUnit();
            
            if (!produit.hasSufficientStock(quantiteRequise)) {
                log.warn("Stock insuffisant pour {} - Disponible: {}, Requis: {}", 
                        produit.getNom(), produit.getQuantiteStock(), quantiteRequise);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Décremente le stock pour tous les ingrédients d'un menu confirmé
     */
    public void decrementerStockPourMenu(Menu menu) {
        log.info("Décrémentation stock pour menu confirmé: {}", menu.getId());
        
        for (MenuIngredient ingredient : menu.getIngredients()) {
            try {
                stockService.decrementerStock(
                        ingredient.getProduit().getId(),
                        ingredient.getQuantiteConvertieStockUnit(),
                        "Utilisation menu: " + menu.getNom(),
                        menu.getId()
                );
            } catch (Exception e) {
                log.error("Erreur lors de la décrémentation stock pour produit {}: {}", 
                        ingredient.getProduit().getId(), e.getMessage());
                throw new IllegalStateException("Échec décrémentation stock: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Restaure le stock lors de l'annulation d'un menu
     */
    public void restaurerStockPourMenu(Menu menu, String motif) {
        log.info("Restauration stock pour menu annulé: {}", menu.getId());
        
        for (MenuIngredient ingredient : menu.getIngredients()) {
            try {
                stockService.incrementerStock(
                        ingredient.getProduit().getId(),
                        ingredient.getQuantiteConvertieStockUnit(),
                        "Annulation menu: " + motif
                );
            } catch (Exception e) {
                log.error("Erreur lors de la restauration stock pour produit {}: {}", 
                        ingredient.getProduit().getId(), e.getMessage());
                throw new IllegalStateException("Échec restauration stock: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Met à jour le coût total d'un menu en fonction de ses ingrédients
     */
    public void mettreAJourCoutTotalMenu(Menu menu) {
        BigDecimal coutTotal = menu.getIngredients().stream()
                .map(MenuIngredient::getCoutIngredient)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        menu.setCoutTotalIngredients(coutTotal);
        menu.setDateModification(LocalDateTime.now());
        
        menuRepository.save(menu);
        
        log.debug("Coût total menu {} mis à jour: {}", menu.getId(), coutTotal);
    }
    
    /**
     * Calcule l'utilisation totale d'un produit dans les menus
     */
    public BigDecimal calculerUtilisationProduit(Long produitId) {
        List<MenuIngredient> ingredients = menuIngredientRepository.findByProduitId(produitId);
        
        return ingredients.stream()
                .filter(ing -> !ing.getMenu().getStatut().name().equals("ANNULE"))
                .map(MenuIngredient::getQuantiteConvertieStockUnit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Vérifie si un produit peut être supprimé (pas utilisé dans des menus actifs)
     */
    public boolean produitPeutEtreSuprime(Long produitId) {
        List<MenuIngredient> ingredientsActifs = menuIngredientRepository.findByProduitId(produitId)
                .stream()
                .filter(ing -> !ing.getMenu().getStatut().name().equals("ANNULE"))
                .toList();
        
        return ingredientsActifs.isEmpty();
    }
}