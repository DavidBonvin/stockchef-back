package com.stockchef.stockchefback.controller.menu;

import com.stockchef.stockchefback.dto.menu.MenuCreationDTO;
import com.stockchef.stockchefback.dto.menu.MenuIngredientDTO;
import com.stockchef.stockchefback.dto.menu.MenuResponseDTO;
import com.stockchef.stockchefback.model.menu.Menu;
import com.stockchef.stockchefback.model.menu.MenuIngredient;
import com.stockchef.stockchefback.service.menu.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Contrôleur REST pour la gestion des menus
 * Expose les endpoints pour créer, modifier, confirmer et consulter les menus
 */
@Slf4j
@RestController
@RequestMapping("/menus")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MenuController {
    
    private final MenuService menuService;
    
    /**
     * Créer un nouveau menu
     * POST /api/menus
     */
    @PostMapping
    public ResponseEntity<MenuResponseDTO> creerMenu(@Valid @RequestBody MenuCreationDTO menuCreationDTO) {
        log.info("Création d'un nouveau menu: {}", menuCreationDTO.getNom());
        
        Menu menu = menuService.creerMenu(
            menuCreationDTO.getNom(),
            menuCreationDTO.getDescription(),
            menuCreationDTO.getDateService(),
            1, // Valeur par défaut pour le nombre de portions
            menuCreationDTO.getPrixVente()
        );
        
        MenuResponseDTO response = convertirMenuEnDTO(menu);
        
        log.info("Menu créé avec succès - ID: {}", menu.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Obtenir un menu par son ID
     * GET /api/menus/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<MenuResponseDTO> obtenirMenu(@PathVariable Long id) {
        log.info("Récupération du menu ID: {}", id);
        
        Menu menu = menuService.obtenirMenu(id);
        MenuResponseDTO response = convertirMenuEnDTO(menu);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Obtenir tous les menus avec pagination
     * GET /api/menus
     */
    @GetMapping
    public ResponseEntity<Page<MenuResponseDTO>> listerMenus(Pageable pageable) {
        try {
            log.info("Récupération des menus - Page: {}, Taille: {}", 
                    pageable.getPageNumber(), pageable.getPageSize());
            
            Page<Menu> menus = menuService.listerMenus(pageable);
            log.info("Menus trouvés: {}", menus.getTotalElements());
            
            Page<MenuResponseDTO> response = menus.map(this::convertirMenuEnDTOSafe);
            log.info("Conversion DTO réussie pour {} menus", response.getContent().size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des menus", e);
            throw new RuntimeException("Erreur lors de la récupération des menus: " + e.getMessage());
        }
    }

    /**
     * Endpoint de diagnostic pour les menus (version simple)
     * GET /api/menus/simple
     */
    @GetMapping("/simple")
    public ResponseEntity<List<Map<String, Object>>> listerMenusSimple() {
        try {
            log.info("Diagnostic: Récupération simple des menus");
            
            Page<Menu> menus = menuService.listerMenus(Pageable.unpaged());
            
            List<Map<String, Object>> response = menus.getContent().stream()
                .map(menu -> {
                    Map<String, Object> simpleMenu = new HashMap<>();
                    simpleMenu.put("id", menu.getId());
                    simpleMenu.put("nom", menu.getNom());
                    simpleMenu.put("dateService", menu.getDateService());
                    simpleMenu.put("statut", menu.getStatut().name());
                    simpleMenu.put("dateCreation", menu.getDateCreation());
                    return simpleMenu;
                })
                .collect(Collectors.toList());
            
            log.info("Diagnostic réussi: {} menus trouvés", response.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur dans le diagnostic des menus", e);
            throw new RuntimeException("Erreur diagnostic: " + e.getMessage());
        }
    }
    
    /**
     * Rechercher des menus par critères
     * GET /api/menus/recherche
     */
    @GetMapping("/recherche")
    public ResponseEntity<Page<MenuResponseDTO>> rechercherMenus(
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDe,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateA,
            Pageable pageable) {
        
        log.info("Recherche menus - Nom: {}, DateDe: {}, DateA: {}", nom, dateDe, dateA);
        
        Page<Menu> menus = menuService.rechercherMenus(nom, dateDe, dateA, pageable);
        Page<MenuResponseDTO> response = menus.map(this::convertirMenuEnDTO);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Ajouter un ingrédient à un menu
     * POST /api/menus/{id}/ingredients
     */
    @PostMapping("/{id}/ingredients")
    public ResponseEntity<MenuResponseDTO> ajouterIngredient(
            @PathVariable Long id,
            @Valid @RequestBody MenuIngredientDTO ingredientDTO) {
        
        log.info("Ajout ingrédient au menu {} - Produit: {}, Quantité: {}", 
                id, ingredientDTO.getProduitId(), ingredientDTO.getQuantiteNecessaire());
        
        MenuIngredient ingredient = menuService.ajouterIngredient(
            id,
            ingredientDTO.getProduitId(),
            ingredientDTO.getQuantiteNecessaire(),
            ingredientDTO.getUniteUtilisee(),
            ingredientDTO.getNotes()
        );
        
        // Recharger le menu complet après modification
        Menu menuMisAJour = menuService.obtenirMenu(id);
        MenuResponseDTO response = convertirMenuEnDTO(menuMisAJour);
        
        log.info("Ingrédient ajouté avec succès - Menu mis à jour");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Supprimer un ingrédient d'un menu
     * DELETE /api/menus/{menuId}/ingredients/{produitId}
     * Accessible aux CHEF, ADMIN et DEVELOPER
     */
    @DeleteMapping("/{menuId}/ingredients/{produitId}")
    @PreAuthorize("hasAnyRole('CHEF', 'ADMIN', 'DEVELOPER')")
    public ResponseEntity<MenuResponseDTO> supprimerIngredient(
            @PathVariable Long menuId,
            @PathVariable Long produitId) {
        
        log.info("Suppression ingrédient du menu {} - Produit: {}", menuId, produitId);
        
        menuService.supprimerIngredient(menuId, produitId);
        
        // Recharger le menu complet après modification
        Menu menuMisAJour = menuService.obtenirMenu(menuId);
        MenuResponseDTO response = convertirMenuEnDTO(menuMisAJour);
        
        log.info("Ingrédient supprimé avec succès - Menu mis à jour");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Confirmer un menu (transaction critique avec décrémentation stock)
     * PUT /api/menus/{id}/confirmer
     */
    @PutMapping("/{id}/confirmer")
    public ResponseEntity<MenuResponseDTO> confirmerMenu(@PathVariable Long id) {
        log.info("=== DEMANDE CONFIRMATION MENU {} ===", id);
        
        try {
            Menu menuConfirme = menuService.confirmerMenu(id);
            MenuResponseDTO response = convertirMenuEnDTO(menuConfirme);
            
            log.info("=== MENU {} CONFIRMÉ AVEC SUCCÈS ===", id);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("=== ÉCHEC CONFIRMATION MENU {} - {} ===", id, e.getMessage());
            throw e; // Sera géré par l'exception handler
        }
    }
    
    /**
     * Annuler un menu confirmé (restauration du stock)
     * PUT /api/menus/{id}/annuler
     */
    @PutMapping("/{id}/annuler")
    public ResponseEntity<MenuResponseDTO> annulerMenu(@PathVariable Long id) {
        log.info("=== DEMANDE ANNULATION MENU {} ===", id);
        
        try {
            Menu menuAnnule = menuService.annulerMenu(id);
            MenuResponseDTO response = convertirMenuEnDTO(menuAnnule);
            
            log.info("=== MENU {} ANNULÉ AVEC SUCCÈS ===", id);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("=== ÉCHEC ANNULATION MENU {} - {} ===", id, e.getMessage());
            throw e; // Sera géré par l'exception handler
        }
    }
    
    /**
     * Mettre à jour les informations générales d'un menu
     * PUT /api/menus/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<MenuResponseDTO> mettreAJourMenu(
            @PathVariable Long id,
            @Valid @RequestBody MenuCreationDTO menuUpdateDTO) {
        
        log.info("Mise à jour du menu {} - Nom: {}", id, menuUpdateDTO.getNom());
        
        Menu menuMisAJour = menuService.mettreAJourMenu(
            id,
            menuUpdateDTO.getNom(),
            menuUpdateDTO.getDescription(),
            menuUpdateDTO.getDateService(),
            menuUpdateDTO.getPrixVente()
        );
        
        MenuResponseDTO response = convertirMenuEnDTO(menuMisAJour);
        
        log.info("Menu {} mis à jour avec succès", id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Supprimer un menu
     * DELETE /api/menus/{id}
     * Accessible aux CHEF, ADMIN et DEVELOPER
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CHEF', 'ADMIN', 'DEVELOPER')")
    public ResponseEntity<Void> supprimerMenu(@PathVariable Long id) {
        log.info("Suppression du menu ID: {}", id);
        
        menuService.supprimerMenu(id);
        
        log.info("Menu {} supprimé avec succès", id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Obtenir les menus réalisables pour une date
     * GET /api/menus/realisables
     */
    @GetMapping("/realisables")
    public ResponseEntity<List<MenuResponseDTO>> obtenirMenusRealisables(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        LocalDate dateRecherche = date != null ? date : LocalDate.now();
        log.info("Recherche menus réalisables pour le: {}", dateRecherche);
        
        List<Menu> menusRealisables = menuService.obtenirMenusRealisables(dateRecherche);
        List<MenuResponseDTO> response = menusRealisables.stream()
            .map(this::convertirMenuEnDTO)
            .toList();
        
        log.info("Trouvé {} menus réalisables", response.size());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Obtenir les statistiques d'un menu (coûts, marges, etc.)
     * GET /api/menus/{id}/statistiques
     */
    @GetMapping("/{id}/statistiques")
    public ResponseEntity<MenuResponseDTO> obtenirStatistiquesMenu(@PathVariable Long id) {
        log.info("Récupération des statistiques du menu ID: {}", id);
        
        Menu menu = menuService.obtenirMenu(id);
        MenuResponseDTO response = convertirMenuEnDTO(menu);
        
        // Les statistiques sont automatiquement calculées dans le DTO
        return ResponseEntity.ok(response);
    }
    
    /**
     * Convertit une entité Menu en DTO de réponse (version sécurisée avec gestion d'erreurs)
     */
    private MenuResponseDTO convertirMenuEnDTOSafe(Menu menu) {
        try {
            return MenuResponseDTO.builder()
                .id(menu.getId())
                .nom(menu.getNom())
                .description(menu.getDescription())
                .dateService(menu.getDateService())
                .dateCreation(menu.getDateCreation())
                .dateModification(menu.getDateModification())
                .statut(menu.getStatut())
                .prixVente(menu.getPrixVente())
                .coutTotalIngredients(menu.getCoutTotalIngredients() != null ? menu.getCoutTotalIngredients() : BigDecimal.ZERO)
                .marge(safeCalculerMarge(menu))
                .margePercentage(menu.getMargePercentage() != null ? menu.getMargePercentage() : BigDecimal.ZERO)
                .peutEtrePrepare(safePeutEtrePrepare(menu))
                .ingredients(safeConvertirIngredients(menu))
                .build();
        } catch (Exception e) {
            log.error("Erreur lors de la conversion du menu {} en DTO", menu.getId(), e);
            // Retourner un DTO minimal en cas d'erreur
            return MenuResponseDTO.builder()
                .id(menu.getId())
                .nom(menu.getNom())
                .description(menu.getDescription())
                .dateService(menu.getDateService())
                .statut(menu.getStatut())
                .prixVente(menu.getPrixVente())
                .coutTotalIngredients(BigDecimal.ZERO)
                .marge(BigDecimal.ZERO)
                .margePercentage(BigDecimal.ZERO)
                .peutEtrePrepare(false)
                .ingredients(List.of())
                .build();
        }
    }

    private BigDecimal safeCalculerMarge(Menu menu) {
        try {
            return menu.calculerMarge();
        } catch (Exception e) {
            log.warn("Erreur calcul marge pour menu {}: {}", menu.getId(), e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    private Boolean safePeutEtrePrepare(Menu menu) {
        try {
            return menu.peutEtrePrepare();
        } catch (Exception e) {
            log.warn("Erreur vérification préparation pour menu {}: {}", menu.getId(), e.getMessage());
            return false;
        }
    }

    private List<MenuIngredientDTO> safeConvertirIngredients(Menu menu) {
        try {
            if (menu.getIngredients() == null) {
                return List.of();
            }
            return menu.getIngredients().stream()
                .map(this::convertirIngredientEnDTOSafe)
                .toList();
        } catch (Exception e) {
            log.warn("Erreur conversion ingrédients pour menu {}: {}", menu.getId(), e.getMessage());
            return List.of();
        }
    }

    private MenuIngredientDTO convertirIngredientEnDTOSafe(MenuIngredient ingredient) {
        try {
            return MenuIngredientDTO.builder()
                .id(ingredient.getId())
                .produitId(ingredient.getProduit() != null ? ingredient.getProduit().getId() : null)
                .produitNom(ingredient.getProduit() != null ? ingredient.getProduit().getNom() : "Produit inconnu")
                .quantiteNecessaire(ingredient.getQuantiteNecessaire())
                .uniteUtilisee(ingredient.getUniteUtilisee())
                .quantiteConvertieStockUnit(ingredient.getQuantiteConvertieStockUnit())
                .coutIngredient(ingredient.getCoutIngredient() != null ? ingredient.getCoutIngredient() : BigDecimal.ZERO)
                .notes(ingredient.getNotes())
                .stockSuffisant(safeStockSuffisant(ingredient))
                .quantiteManquante(null) // Calculé côté client si nécessaire
                .build();
        } catch (Exception e) {
            log.error("Erreur conversion ingrédient {}: {}", ingredient.getId(), e.getMessage());
            return MenuIngredientDTO.builder()
                .id(ingredient.getId())
                .produitNom("Erreur de conversion")
                .quantiteNecessaire(BigDecimal.ZERO)
                .stockSuffisant(false)
                .build();
        }
    }

    private Boolean safeStockSuffisant(MenuIngredient ingredient) {
        try {
            return ingredient.stockSuffisant();
        } catch (Exception e) {
            log.warn("Erreur vérification stock pour ingrédient {}: {}", ingredient.getId(), e.getMessage());
            return false;
        }
    }

    /**
     * Convertit une entité Menu en DTO de réponse
     */
    private MenuResponseDTO convertirMenuEnDTO(Menu menu) {
        return MenuResponseDTO.builder()
            .id(menu.getId())
            .nom(menu.getNom())
            .description(menu.getDescription())
            .dateService(menu.getDateService())
            .dateCreation(menu.getDateCreation())
            .dateModification(menu.getDateModification())
            .statut(menu.getStatut())
            .prixVente(menu.getPrixVente())
            .coutTotalIngredients(menu.getCoutTotalIngredients())
            .marge(menu.calculerMarge())
            .margePercentage(menu.getMargePercentage())
            .peutEtrePrepare(menu.peutEtrePrepare())
            .ingredients(menu.getIngredients().stream()
                .map(this::convertirIngredientEnDTO)
                .toList())
            .build();
    }
    
    /**
     * Convertit une entité MenuIngredient en DTO
     */
    private MenuIngredientDTO convertirIngredientEnDTO(MenuIngredient ingredient) {
        return MenuIngredientDTO.builder()
            .id(ingredient.getId())
            .produitId(ingredient.getProduit().getId())
            .produitNom(ingredient.getProduit().getNom())
            .quantiteNecessaire(ingredient.getQuantiteNecessaire())
            .uniteUtilisee(ingredient.getUniteUtilisee())
            .quantiteConvertieStockUnit(ingredient.getQuantiteConvertieStockUnit())
            .coutIngredient(ingredient.getCoutIngredient())
            .notes(ingredient.getNotes())
            .stockSuffisant(ingredient.stockSuffisant())
            .quantiteManquante(ingredient.stockSuffisant() ? null : ingredient.getQuantiteManquante())
            .build();
    }
}