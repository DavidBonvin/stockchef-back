package com.stockchef.stockchefback.service.menu;

import com.stockchef.stockchefback.model.menu.Menu;
import com.stockchef.stockchefback.model.menu.StatutMenu;
import com.stockchef.stockchefback.repository.menu.MenuRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service dédié aux requêtes de lecture des menus
 * 
 * RESPONSABILITÉS:
 * - Toutes les opérations de lecture (queries)
 * - Recherches et filtres optimisés
 * - Pas de modification de données
 * - Performance optimisée avec @Transactional(readOnly = true)
 */
@Service
@Transactional(readOnly = true)
public class MenuQueryService {
    
    private static final Logger log = LoggerFactory.getLogger(MenuQueryService.class);
    
    private final MenuRepository menuRepository;
    
    public MenuQueryService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }
    
    // ==================== REQUÊTES DE BASE ====================
    
    /**
     * Obtient un menu par son ID
     */
    public Menu obtenirMenu(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID ne peut pas être null");
        }
        
        log.debug("Recherche menu par ID: {}", id);
        return menuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu non trouvé avec l'ID: " + id));
    }
    
    /**
     * Obtient un menu par ID (version Optional)
     */
    public Optional<Menu> getMenuById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        
        log.debug("Recherche optionnelle menu par ID: {}", id);
        return menuRepository.findById(id);
    }
    
    /**
     * Liste tous les menus avec pagination
     */
    public Page<Menu> listerMenus(Pageable pageable) {
        if (pageable == null) {
            throw new IllegalArgumentException("Le paramètre pageable ne peut pas être null");
        }
        
        log.debug("Listage des menus avec pagination: {}", pageable);
        return menuRepository.findAll(pageable);
    }
    
    // ==================== RECHERCHES AVANCÉES ====================
    
    /**
     * Recherche de menus par critères multiples
     */
    public Page<Menu> rechercherMenus(String nom, LocalDate dateDe, LocalDate dateA, Pageable pageable) {
        log.debug("Recherche menus - Nom: {}, Date de: {}, Date à: {}", nom, dateDe, dateA);
        
        // Recherche par nom et dates
        if (nom != null && !nom.trim().isEmpty() && dateDe != null && dateA != null) {
            return menuRepository.findByNomContainingIgnoreCaseAndDateServiceBetween(
                    nom.trim(), dateDe, dateA, pageable);
        }
        // Recherche par nom uniquement
        else if (nom != null && !nom.trim().isEmpty()) {
            return menuRepository.findByNomContainingIgnoreCase(nom.trim(), pageable);
        }
        // Recherche par dates uniquement
        else if (dateDe != null && dateA != null) {
            return menuRepository.findByDateServiceBetween(dateDe, dateA, pageable);
        }
        // Pas de critères, retourne tout
        else {
            return menuRepository.findAll(pageable);
        }
    }
    
    // ==================== REQUÊTES PAR CRITÈRES SPÉCIFIQUES ====================
    
    /**
     * Obtient les menus par date de service
     */
    public List<Menu> getMenusByDateService(LocalDate dateService) {
        log.debug("Recherche menus par date de service: {}", dateService);
        return menuRepository.findByDateServiceOrderByNom(dateService);
    }
    
    /**
     * Obtient les menus par statut
     */
    public List<Menu> getMenusByStatut(StatutMenu statut) {
        log.debug("Recherche menus par statut: {}", statut);
        return menuRepository.findByStatutOrderByDateServiceDesc(statut);
    }
    
    /**
     * Obtient les menus par chef
     */
    public List<Menu> getMenusByChef(String chefEmail) {
        log.debug("Recherche menus par chef: {}", chefEmail);
        return menuRepository.findByChefResponsableOrderByDateServiceDesc(chefEmail);
    }
    
    /**
     * Obtient les menus réalisables pour une date
     * (avec vérification automatique de stock)
     */
    public List<Menu> obtenirMenusRealisables(LocalDate dateService) {
        log.debug("Recherche menus réalisables pour: {}", dateService);
        
        // Utilise la méthode optimisée du repository qui vérifie le stock
        List<Menu> menusRealisables = menuRepository.findMenusRealisablesParDate(dateService);
        
        log.info("Trouvé {} menus réalisables pour la date {}", 
                menusRealisables.size(), dateService);
        
        return menusRealisables;
    }
    
    /**
     * Obtient les menus réalisables pour une date (alias)
     */
    public List<Menu> getMenusRealisables(LocalDate dateService) {
        return obtenirMenusRealisables(dateService);
    }
    
    // ==================== REQUÊTES UTILITAIRES ====================
    
    /**
     * Recherche de menus par période
     */
    public List<Menu> rechercherMenusParPeriode(LocalDate dateDebut, LocalDate dateFin) {
        log.debug("Recherche menus par période: {} à {}", dateDebut, dateFin);
        return menuRepository.findByDateServiceBetweenOrderByDateService(dateDebut, dateFin);
    }
    
    /**
     * Obtient les menus confirmés pour une période (pour planning)
     */
    public List<Menu> getMenusConfirmesParPeriode(LocalDate dateDebut, LocalDate dateFin) {
        log.debug("Recherche menus confirmés par période: {} à {}", dateDebut, dateFin);
        return menuRepository.findMenusConfirmesEntreDates(StatutMenu.CONFIRME, dateDebut, dateFin);
    }
}