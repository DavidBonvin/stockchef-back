package com.stockchef.stockchefback.service.menu;

import com.stockchef.stockchefback.model.menu.Menu;
import com.stockchef.stockchefback.model.menu.StatutMenu;
import com.stockchef.stockchefback.repository.menu.MenuRepository;
import com.stockchef.stockchefback.service.inventory.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Service spécialisé pour la création et gestion du cycle de vie des menus
 * 
 * RESPONSABILITÉS:
 * - Création de nouveaux menus
 * - Confirmation de menus (avec coordination de stock)
 * - Annulation de menus
 * - Mise à jour des propriétés de base des menus
 */
@Service
@Transactional
public class MenuCreationService {
    
    private static final Logger log = LoggerFactory.getLogger(MenuCreationService.class);
    
    private final MenuRepository menuRepository;
    private final StockService stockService;
    private final MenuIngredientService menuIngredientService;
    
    public MenuCreationService(MenuRepository menuRepository, 
                              StockService stockService,
                              MenuIngredientService menuIngredientService) {
        this.menuRepository = menuRepository;
        this.stockService = stockService;
        this.menuIngredientService = menuIngredientService;
    }
    
    /**
     * Crée un nouveau menu en statut BROUILLON
     */
    public Menu creerMenu(String nom, String description, LocalDate dateService, 
                         int nombrePortions, BigDecimal prixVente) {
        log.info("Création d'un nouveau menu: {}", nom);
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String chefEmail = auth.getName();
        
        Menu menu = Menu.creerMenu(nom, description, dateService, nombrePortions, chefEmail);
        menu.setPrixVente(prixVente);
        menu.setStatut(StatutMenu.BROUILLON);
        
        Menu savedMenu = menuRepository.save(menu);
        log.info("Menu créé avec succès - ID: {}", savedMenu.getId());
        
        return savedMenu;
    }
    
    /**
     * Confirme un menu et coordonne avec le système de stock
     * TRANSACTION CRITIQUE: Rollback automatique si échec stock
     */
    public Menu confirmerMenu(Long menuId) {
        log.info("Confirmation du menu ID: {}", menuId);
        
        if (menuId == null) {
            throw new IllegalArgumentException("L'ID du menu ne peut pas être null");
        }
        
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("Menu non trouvé avec l'ID: " + menuId));
        
        if (menu.getStatut() == StatutMenu.CONFIRME) {
            log.warn("Menu déjà confirmé: {}", menuId);
            return menu;
        }
        
        if (menu.getStatut() == StatutMenu.ANNULE) {
            throw new IllegalStateException("Impossible de confirmer un menu annulé: " + menuId);
        }
        
        // Validation des ingrédients
        if (menu.getIngredients().isEmpty()) {
            throw new IllegalStateException("Impossible de confirmer un menu sans ingrédients: " + menuId);
        }
        
        // COORDINATION CRITIQUE AVEC STOCK
        try {
            boolean stockSuffisant = menuIngredientService.verifierStockSuffisant(menu);
            if (!stockSuffisant) {
                throw new IllegalStateException("Stock insuffisant pour confirmer le menu: " + menuId);
            }
            
            // Décrémentation coordonnée du stock
            menuIngredientService.decrementerStockPourMenu(menu);
            
            // Mise à jour du statut
            menu.setStatut(StatutMenu.CONFIRME);
            menu.setDateModification(LocalDateTime.now());
            
            Menu confirmedMenu = menuRepository.save(menu);
            log.info("Menu confirmé avec succès - ID: {}", menuId);
            
            return confirmedMenu;
            
        } catch (Exception e) {
            log.error("Erreur lors de la confirmation du menu {}: {}", menuId, e.getMessage(), e);
            throw new IllegalStateException("Échec de la confirmation du menu: " + e.getMessage(), e);
        }
    }
    
    /**
     * Annule un menu et restaure le stock si nécessaire
     */
    public Menu annulerMenu(Long menuId, String motifAnnulation) {
        log.info("Annulation du menu ID: {}, motif: {}", menuId, motifAnnulation);
        
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("Menu non trouvé: " + menuId));
        
        if (menu.getStatut() == StatutMenu.ANNULE) {
            log.warn("Menu déjà annulé: {}", menuId);
            return menu;
        }
        
        // Si le menu était confirmé, restaurer le stock
        if (menu.getStatut() == StatutMenu.CONFIRME) {
            try {
                menuIngredientService.restaurerStockPourMenu(menu, motifAnnulation);
                log.info("Stock restauré pour le menu annulé: {}", menuId);
            } catch (Exception e) {
                log.error("Erreur lors de la restauration du stock pour le menu {}: {}", menuId, e.getMessage());
                throw new IllegalStateException("Échec de la restauration du stock: " + e.getMessage(), e);
            }
        }
        
        menu.setStatut(StatutMenu.ANNULE);
        menu.setDateModification(LocalDateTime.now());
        
        Menu cancelledMenu = menuRepository.save(menu);
        log.info("Menu annulé avec succès - ID: {}", menuId);
        
        return cancelledMenu;
    }
    
    /**
     * Met à jour les propriétés de base d'un menu (nom, description, etc.)
     */
    public Menu mettreAJourMenu(Long id, String nom, String description, LocalDate dateService, BigDecimal prixVente) {
        log.info("Mise à jour du menu ID: {}", id);
        
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu non trouvé: " + id));
        
        if (menu.getStatut() == StatutMenu.CONFIRME || menu.getStatut() == StatutMenu.PREPARE) {
            throw new IllegalStateException("Impossible de modifier un menu confirmé ou préparé");
        }
        
        if (nom != null && !nom.trim().isEmpty()) {
            menu.setNom(nom);
        }
        if (description != null) {
            menu.setDescription(description);
        }
        if (dateService != null) {
            menu.setDateService(dateService);
        }
        if (prixVente != null) {
            menu.setPrixVente(prixVente);
        }
        
        menu.setDateModification(LocalDateTime.now());
        
        Menu updatedMenu = menuRepository.save(menu);
        log.info("Menu mis à jour avec succès - ID: {}", id);
        
        return updatedMenu;
    }
    
    /**
     * Supprime un menu (uniquement si en brouillon)
     */
    public void supprimerMenu(Long id) {
        log.info("Suppression du menu ID: {}", id);
        
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu non trouvé: " + id));
        
        if (menu.getStatut() != StatutMenu.BROUILLON) {
            throw new IllegalStateException("Impossible de supprimer un menu qui n'est pas en brouillon");
        }
        
        menuRepository.delete(menu);
        log.info("Menu supprimé avec succès - ID: {}", id);
    }
}