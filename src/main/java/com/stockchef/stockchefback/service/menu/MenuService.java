package com.stockchef.stockchefback.service.menu;

import com.stockchef.stockchefback.model.inventory.Unite;
import com.stockchef.stockchefback.model.menu.Menu;
import com.stockchef.stockchefback.model.menu.MenuIngredient;
import com.stockchef.stockchefback.model.menu.StatutMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service principal de coordination pour la gestion des menus
 * 
 * ARCHITECTURE MODULARISÉE:
 * - Délègue les opérations spécialisées aux services dédiés
 * - Maintient la coordination transactionnelle de haut niveau
 * - Point d'entrée principal pour les contrôleurs
 * 
 * SERVICES DÉLÉGUÉS:
 * - MenuCreationService: Création et cycle de vie des menus
 * - MenuIngredientService: Gestion des ingrédients
 * - MenuQueryService: Recherches et requêtes
 */
@Service
@Transactional
public class MenuService {
    
    private static final Logger log = LoggerFactory.getLogger(MenuService.class);
    
    private final MenuCreationService menuCreationService;
    private final MenuIngredientService menuIngredientService;
    private final MenuQueryService menuQueryService;
    
    public MenuService(MenuCreationService menuCreationService,
                      MenuIngredientService menuIngredientService,
                      MenuQueryService menuQueryService) {
        this.menuCreationService = menuCreationService;
        this.menuIngredientService = menuIngredientService;
        this.menuQueryService = menuQueryService;
    }
    
    // ==================== DÉLÉGATION CRÉATION ====================
    
    /**
     * Crée un nouveau menu en brouillon
     */
    public Menu creerMenu(String nom, String description, LocalDate dateService, 
                         int nombrePortions, BigDecimal prixVente) {
        return menuCreationService.creerMenu(nom, description, dateService, nombrePortions, prixVente);
    }
    
    /**
     * Confirme un menu et coordonne avec le système de stock
     */
    public Menu confirmerMenu(Long menuId) {
        return menuCreationService.confirmerMenu(menuId);
    }
    
    /**
     * Annule un menu avec motif
     */
    public Menu annulerMenu(Long menuId, String motifAnnulation) {
        return menuCreationService.annulerMenu(menuId, motifAnnulation);
    }
    
    /**
     * Annule un menu (sans motif)
     */
    public Menu annulerMenu(Long menuId) {
        return menuCreationService.annulerMenu(menuId, "Annulation manuelle");
    }
    
    /**
     * Met à jour les propriétés d'un menu
     */
    public Menu mettreAJourMenu(Long id, String nom, String description, LocalDate dateService, BigDecimal prixVente) {
        return menuCreationService.mettreAJourMenu(id, nom, description, dateService, prixVente);
    }
    
    /**
     * Supprime un menu
     */
    public void supprimerMenu(Long id) {
        menuCreationService.supprimerMenu(id);
    }
    
    // ==================== DÉLÉGATION INGRÉDIENTS ====================
    
    /**
     * Ajoute un ingrédient à un menu
     */
    public MenuIngredient ajouterIngredient(Long menuId, Long produitId, 
                                          BigDecimal quantiteNecessaire, Unite uniteUtilisee, String notes) {
        return menuIngredientService.ajouterIngredient(menuId, produitId, quantiteNecessaire, uniteUtilisee, notes);
    }
    
    /**
     * Supprime un ingrédient d'un menu
     */
    public void supprimerIngredient(Long menuId, Long produitId) {
        menuIngredientService.supprimerIngredient(menuId, produitId);
    }
    
    /**
     * Calcule l'utilisation d'un produit dans les menus
     */
    public BigDecimal calculerUtilisationProduit(Long produitId) {
        return menuIngredientService.calculerUtilisationProduit(produitId);
    }
    
    /**
     * Vérifie si un produit peut être supprimé
     */
    public boolean produitPeutEtreSuprime(Long produitId) {
        return menuIngredientService.produitPeutEtreSuprime(produitId);
    }
    
    // ==================== DÉLÉGATION REQUÊTES ====================
    
    /**
     * Obtient un menu par son ID
     */
    @Transactional(readOnly = true)
    public Menu obtenirMenu(Long id) {
        return menuQueryService.obtenirMenu(id);
    }
    
    /**
     * Obtient un menu par ID (version Optional)
     */
    @Transactional(readOnly = true)
    public Optional<Menu> getMenuById(Long id) {
        return menuQueryService.getMenuById(id);
    }
    
    /**
     * Liste tous les menus avec pagination
     */
    @Transactional(readOnly = true)
    public Page<Menu> listerMenus(Pageable pageable) {
        return menuQueryService.listerMenus(pageable);
    }
    
    /**
     * Recherche de menus par critères
     */
    @Transactional(readOnly = true)
    public Page<Menu> rechercherMenus(String nom, LocalDate dateDe, LocalDate dateA, Pageable pageable) {
        return menuQueryService.rechercherMenus(nom, dateDe, dateA, pageable);
    }
    
    /**
     * Obtient les menus par date de service
     */
    @Transactional(readOnly = true)
    public List<Menu> getMenusByDateService(LocalDate dateService) {
        return menuQueryService.getMenusByDateService(dateService);
    }
    
    /**
     * Obtient les menus par statut
     */
    @Transactional(readOnly = true)
    public List<Menu> getMenusByStatut(StatutMenu statut) {
        return menuQueryService.getMenusByStatut(statut);
    }
    
    /**
     * Obtient les menus par chef
     */
    @Transactional(readOnly = true)
    public List<Menu> getMenusByChef(String chefEmail) {
        return menuQueryService.getMenusByChef(chefEmail);
    }
    
    /**
     * Obtient les menus réalisables pour une date
     */
    @Transactional(readOnly = true)
    public List<Menu> obtenirMenusRealisables(LocalDate dateService) {
        return menuQueryService.obtenirMenusRealisables(dateService);
    }
    
    /**
     * Alias pour la compatibilité
     */
    @Transactional(readOnly = true)
    public List<Menu> getMenusRealisables(LocalDate dateService) {
        return menuQueryService.getMenusRealisables(dateService);
    }
}