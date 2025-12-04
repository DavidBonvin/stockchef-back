package com.stockchef.stockchefback.service.menu;

import com.stockchef.stockchefback.model.inventory.Produit;
import com.stockchef.stockchefback.model.inventory.Unite;
import com.stockchef.stockchefback.model.menu.Menu;
import com.stockchef.stockchefback.model.menu.MenuIngredient;
import com.stockchef.stockchefback.model.menu.StatutMenu;
import com.stockchef.stockchefback.repository.inventory.ProduitRepository;
import com.stockchef.stockchefback.repository.menu.MenuIngredientRepository;
import com.stockchef.stockchefback.repository.menu.MenuRepository;
import com.stockchef.stockchefback.service.inventory.StockService;
import com.stockchef.stockchefback.service.inventory.UniteConversionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests TDD pour MenuService - Focus sur les transactions coordinées et rollback
 * 
 * SCENARIOS CRITIQUES TESTÉS:
 * 1. Transaction réussie: Menu confirmé + Stock décrémenté
 * 2. Transaction échoue: Rollback complet si stock insuffisant
 * 3. Validation préalable de stock pour éviter rollbacks partiels
 * 4. Annulation de menu avec restauration de stock
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "chef@stockchef.com", roles = {"CHEF"})
@DisplayName("MenuService TDD - Transactions coordinées avec Stock")
class MenuServiceTest {
    
    @Autowired
    private MenuService menuService;
    
    @Autowired
    private MenuRepository menuRepository;
    
    @Autowired
    private MenuIngredientRepository menuIngredientRepository;
    
    @Autowired
    private ProduitRepository produitRepository;
    
    @Autowired
    private StockService stockService;
    
    @Autowired
    private UniteConversionService uniteConversionService;
    
    private Produit tomates;
    private Produit fromage;
    private Produit pates;
    private Menu menuTest;
    
    @BeforeEach
    void setUp() {
        // Créer des produits avec stock suffisant
        tomates = new Produit("Tomates", new BigDecimal("5.0"), Unite.KILOGRAMME, 
                             new BigDecimal("3.50"), new BigDecimal("1.0"));
        fromage = new Produit("Fromage", new BigDecimal("2.0"), Unite.KILOGRAMME, 
                             new BigDecimal("8.00"), new BigDecimal("0.5"));
        pates = new Produit("Pâtes", new BigDecimal("10.0"), Unite.KILOGRAMME, 
                           new BigDecimal("1.20"), new BigDecimal("2.0"));
        
        // Sauvegarder les produits
        tomates = produitRepository.save(tomates);
        fromage = produitRepository.save(fromage);
        pates = produitRepository.save(pates);
        
        // Créer un menu de test
        menuTest = menuService.creerMenu(
            "Pasta Pomodoro", 
            "Délicieuses pâtes aux tomates", 
            LocalDate.now().plusDays(1), 
            4, // nombrePortions
            new BigDecimal("25.50")
        );
    }
    
    @Test
    @DisplayName("UC3: Créer un menu en brouillon - Success")
    void shouldCreateMenuInDraftStatus() {
        // When
        Menu menu = menuService.creerMenu(
            "Pizza Margherita", 
            "Pizza classique", 
            LocalDate.now().plusDays(2), 
            6, // nombrePortions
            new BigDecimal("18.90")
        );
        
        // Then
        assertThat(menu).isNotNull();
        assertThat(menu.getId()).isNotNull();
        assertThat(menu.getNom()).isEqualTo("Pizza Margherita");
        assertThat(menu.getStatut()).isEqualTo(StatutMenu.BROUILLON);
        assertThat(menu.getChefResponsable()).isEqualTo("chef@stockchef.com");
        assertThat(menu.getIngredients()).isEmpty();
    }
    
    @Test
    @DisplayName("UC3: Ajouter ingrédient au menu avec conversion d'unité")
    void shouldAddIngredientWithUnitConversion() {
        // Given - Ajouter 500g de tomates (produit stocké en kg)
        BigDecimal quantiteGrammes = new BigDecimal("500");
        
        // When
        MenuIngredient ingredient = menuService.ajouterIngredient(
            menuTest.getId(), 
            tomates.getId(), 
            quantiteGrammes, 
            Unite.GRAMME, 
            "Tomates fraîches"
        );
        
        // Then
        assertThat(ingredient).isNotNull();
        assertThat(ingredient.getQuantiteNecessaire()).isEqualByComparingTo(quantiteGrammes);
        assertThat(ingredient.getUniteUtilisee()).isEqualTo(Unite.GRAMME);
        
        // Vérifier la conversion: 500g = 0.5kg
        assertThat(ingredient.getQuantiteConvertieStockUnit())
            .isEqualByComparingTo(new BigDecimal("0.5"));
        
        // Vérifier le calcul de coût: 0.5kg * 3.50€/kg = 1.75€
        assertThat(ingredient.getCoutIngredient())
            .isEqualByComparingTo(new BigDecimal("1.75"));
        
        // Vérifier que le menu est mis à jour
        Menu menuMisAJour = menuService.getMenuById(menuTest.getId()).orElseThrow();
        assertThat(menuMisAJour.getIngredients()).hasSize(1);
        assertThat(menuMisAJour.getCoutTotalIngredients())
            .isEqualByComparingTo(new BigDecimal("1.75"));
    }
    
    @Test
    @DisplayName("UC2+UC3: TRANSACTION RÉUSSIE - Confirmer menu avec décrémentation stock")
    void shouldConfirmMenuAndDecrementStockSuccessfully() {
        // Given - Préparer un menu avec plusieurs ingrédients
        menuService.ajouterIngredient(menuTest.getId(), tomates.getId(), 
                                    new BigDecimal("1.0"), Unite.KILOGRAMME, "Tomates");
        menuService.ajouterIngredient(menuTest.getId(), fromage.getId(), 
                                    new BigDecimal("0.3"), Unite.KILOGRAMME, "Fromage");
        menuService.ajouterIngredient(menuTest.getId(), pates.getId(), 
                                    new BigDecimal("0.5"), Unite.KILOGRAMME, "Pâtes");
        
        // Capturer les stocks initiaux
        BigDecimal stockInitialTomates = tomates.getQuantiteStock();
        BigDecimal stockInitialFromage = fromage.getQuantiteStock();
        BigDecimal stockInitialPates = pates.getQuantiteStock();
        
        // When - TRANSACTION CRITIQUE
        Menu menuConfirme = menuService.confirmerMenu(menuTest.getId());
        
        // Then - Vérifications transactionnelles
        
        // 1. Le menu doit être confirmé
        assertThat(menuConfirme.getStatut()).isEqualTo(StatutMenu.CONFIRME);
        assertThat(menuConfirme.getDernierModificateurEmail()).isEqualTo("chef@stockchef.com");
        
        // 2. TOUS les stocks doivent être décrémentés
        Produit tomatesApres = produitRepository.findById(tomates.getId()).orElseThrow();
        Produit fromageApres = produitRepository.findById(fromage.getId()).orElseThrow();
        Produit patesApres = produitRepository.findById(pates.getId()).orElseThrow();
        
        assertThat(tomatesApres.getQuantiteStock())
            .isEqualByComparingTo(stockInitialTomates.subtract(new BigDecimal("1.0"))); // 5.0 - 1.0 = 4.0
        assertThat(fromageApres.getQuantiteStock())
            .isEqualByComparingTo(stockInitialFromage.subtract(new BigDecimal("0.3"))); // 2.0 - 0.3 = 1.7
        assertThat(patesApres.getQuantiteStock())
            .isEqualByComparingTo(stockInitialPates.subtract(new BigDecimal("0.5"))); // 10.0 - 0.5 = 9.5
        
        // 3. Les mouvements de stock doivent être créés avec audit trail
        // (Vérification via le StockService que nous avons déjà testé)
    }
    
    @Test
    @DisplayName("UC2+UC3: TRANSACTION ÉCHOUE - Rollback complet si stock insuffisant")
    void shouldRollbackCompletelyWhenInsufficientStock() {
        // Given - Préparer un menu avec stock suffisant et insuffisant
        menuService.ajouterIngredient(menuTest.getId(), tomates.getId(), 
                                    new BigDecimal("1.0"), Unite.KILOGRAMME, "OK");
        menuService.ajouterIngredient(menuTest.getId(), fromage.getId(), 
                                    new BigDecimal("3.0"), Unite.KILOGRAMME, "STOCK INSUFFISANT"); // > 2.0kg disponible
        menuService.ajouterIngredient(menuTest.getId(), pates.getId(), 
                                    new BigDecimal("0.5"), Unite.KILOGRAMME, "OK");
        
        // Capturer les stocks initiaux
        BigDecimal stockInitialTomates = tomates.getQuantiteStock();
        BigDecimal stockInitialFromage = fromage.getQuantiteStock();
        BigDecimal stockInitialPates = pates.getQuantiteStock();
        
        // When & Then - L'exception doit être levée AVANT toute décrémentation
        assertThatThrownBy(() -> {
            menuService.confirmerMenu(menuTest.getId());
        })
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Stock insuffisant pour")
        .hasMessageContaining("Fromage");
        
        // Then - ROLLBACK COMPLET: AUCUN stock ne doit être décrémenté
        Produit tomatesApres = produitRepository.findById(tomates.getId()).orElseThrow();
        Produit fromageApres = produitRepository.findById(fromage.getId()).orElseThrow();
        Produit patesApres = produitRepository.findById(pates.getId()).orElseThrow();
        
        assertThat(tomatesApres.getQuantiteStock()).isEqualByComparingTo(stockInitialTomates);
        assertThat(fromageApres.getQuantiteStock()).isEqualByComparingTo(stockInitialFromage);
        assertThat(patesApres.getQuantiteStock()).isEqualByComparingTo(stockInitialPates);
        
        // Le menu doit rester en BROUILLON
        Menu menuApresEchec = menuService.getMenuById(menuTest.getId()).orElseThrow();
        assertThat(menuApresEchec.getStatut()).isEqualTo(StatutMenu.BROUILLON);
    }
    
    @Test
    @DisplayName("UC3: Validation préalable de stock - Éviter rollbacks partiels")
    void shouldValidateAllStockBeforeAnyDecrement() {
        // Given - Menu avec ingrédients: OK, INSUFFISANT, OK
        menuService.ajouterIngredient(menuTest.getId(), tomates.getId(), 
                                    new BigDecimal("2.0"), Unite.KILOGRAMME, "Première OK");
        menuService.ajouterIngredient(menuTest.getId(), fromage.getId(), 
                                    new BigDecimal("5.0"), Unite.KILOGRAMME, "INSUFFISANT");
        menuService.ajouterIngredient(menuTest.getId(), pates.getId(), 
                                    new BigDecimal("1.0"), Unite.KILOGRAMME, "Dernière OK");
        
        // When & Then - L'exception doit être détectée IMMÉDIATEMENT
        assertThatThrownBy(() -> {
            menuService.confirmerMenu(menuTest.getId());
        })
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Stock insuffisant")
        .hasMessageContaining("Fromage");
        
        // Aucun mouvement de stock ne doit être créé
        // (Vérification implicite via l'absence de décrémentation)
    }
    
    @Test
    @DisplayName("UC3: Annuler menu confirmé avec restauration du stock")
    void shouldCancelMenuAndRestoreStock() {
        // Given - Menu confirmé avec stock décrémenté
        menuService.ajouterIngredient(menuTest.getId(), tomates.getId(), 
                                    new BigDecimal("1.5"), Unite.KILOGRAMME, "Tomates");
        menuService.ajouterIngredient(menuTest.getId(), pates.getId(), 
                                    new BigDecimal("0.8"), Unite.KILOGRAMME, "Pâtes");
        
        Menu menuConfirme = menuService.confirmerMenu(menuTest.getId());
        assertThat(menuConfirme.getStatut()).isEqualTo(StatutMenu.CONFIRME);
        
        // Capturer les stocks après confirmation
        Produit tomatesAvantAnnulation = produitRepository.findById(tomates.getId()).orElseThrow();
        Produit patesAvantAnnulation = produitRepository.findById(pates.getId()).orElseThrow();
        
        BigDecimal stockTomatesApresConfirm = tomatesAvantAnnulation.getQuantiteStock();
        BigDecimal stockPatesApresConfirm = patesAvantAnnulation.getQuantiteStock();
        
        // When - Annuler le menu
        String motifAnnulation = "Client a annulé la réservation";
        Menu menuAnnule = menuService.annulerMenu(menuTest.getId(), motifAnnulation);
        
        // Then - Vérifications de l'annulation
        
        // 1. Le menu doit être annulé
        assertThat(menuAnnule.getStatut()).isEqualTo(StatutMenu.ANNULE);
        
        // 2. Le stock doit être RESTAURÉ
        Produit tomatesApresAnnulation = produitRepository.findById(tomates.getId()).orElseThrow();
        Produit patesApresAnnulation = produitRepository.findById(pates.getId()).orElseThrow();
        
        assertThat(tomatesApresAnnulation.getQuantiteStock())
            .isEqualByComparingTo(stockTomatesApresConfirm.add(new BigDecimal("1.5"))); // Restauré
        assertThat(patesApresAnnulation.getQuantiteStock())
            .isEqualByComparingTo(stockPatesApresConfirm.add(new BigDecimal("0.8"))); // Restauré
    }
    
    @Test
    @DisplayName("UC3: Impossible d'annuler un menu déjà préparé")
    void shouldNotCancelPreparedMenu() {
        // Given - Menu confirmé puis marqué comme préparé
        menuService.ajouterIngredient(menuTest.getId(), tomates.getId(), 
                                    new BigDecimal("1.0"), Unite.KILOGRAMME, "Test");
        
        Menu menuConfirme = menuService.confirmerMenu(menuTest.getId());
        menuConfirme.marquerCommePrepare();
        menuRepository.save(menuConfirme);
        
        // When & Then
        assertThatThrownBy(() -> {
            menuService.annulerMenu(menuTest.getId(), "Tentative impossible");
        })
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Impossible d'annuler un menu déjà préparé");
    }
    
    @Test
    @DisplayName("UC3: Impossible de modifier un menu confirmé")
    void shouldNotModifyConfirmedMenu() {
        // Given - Menu confirmé
        menuService.ajouterIngredient(menuTest.getId(), tomates.getId(), 
                                    new BigDecimal("1.0"), Unite.KILOGRAMME, "Test");
        menuService.confirmerMenu(menuTest.getId());
        
        // When & Then
        assertThatThrownBy(() -> {
            menuService.ajouterIngredient(menuTest.getId(), fromage.getId(), 
                                        new BigDecimal("0.5"), Unite.KILOGRAMME, "Impossible");
        })
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("ne peut plus être modifié dans le statut");
    }
    
    @Test
    @DisplayName("UC3: Calcul automatique du coût total du menu")
    void shouldCalculateMenuTotalCostAutomatically() {
        // Given - Ajouter plusieurs ingrédients avec coûts différents
        menuService.ajouterIngredient(menuTest.getId(), tomates.getId(), 
                                    new BigDecimal("2.0"), Unite.KILOGRAMME, "Tomates");  // 2.0 * 3.50 = 7.00
        menuService.ajouterIngredient(menuTest.getId(), fromage.getId(), 
                                    new BigDecimal("0.5"), Unite.KILOGRAMME, "Fromage"); // 0.5 * 8.00 = 4.00
        menuService.ajouterIngredient(menuTest.getId(), pates.getId(), 
                                    new BigDecimal("1.0"), Unite.KILOGRAMME, "Pâtes");   // 1.0 * 1.20 = 1.20
        
        // When - Récupérer le menu mis à jour
        Menu menuAvecCout = menuService.getMenuById(menuTest.getId()).orElseThrow();
        
        // Then - Vérifier le calcul automatique: 7.00 + 4.00 + 1.20 = 12.20€
        assertThat(menuAvecCout.getCoutTotalIngredients())
            .isEqualByComparingTo(new BigDecimal("12.20"));
    }
    
    @Test
    @DisplayName("UC3: Détection d'alerte de stock lors de la confirmation")
    void shouldDetectStockAlertDuringConfirmation() {
        // Given - Ajouter un ingrédient qui passera sous le seuil d'alerte
        // Tomates: stock=5.0, seuil=1.0 -> prendre 4.5 -> reste 0.5 < 1.0 = ALERTE
        menuService.ajouterIngredient(menuTest.getId(), tomates.getId(), 
                                    new BigDecimal("4.5"), Unite.KILOGRAMME, "Beaucoup de tomates");
        
        // When - La confirmation doit réussir malgré l'alerte
        Menu menuConfirme = menuService.confirmerMenu(menuTest.getId());
        
        // Then - Le menu est confirmé mais le stock est en alerte
        assertThat(menuConfirme.getStatut()).isEqualTo(StatutMenu.CONFIRME);
        
        Produit tomatesApres = produitRepository.findById(tomates.getId()).orElseThrow();
        assertThat(tomatesApres.getQuantiteStock()).isEqualByComparingTo(new BigDecimal("0.5"));
        assertThat(tomatesApres.isUnderAlertThreshold()).isTrue();
    }
    
    @Test
    @DisplayName("UC3: Gestion des conversions d'unités complexes")
    void shouldHandleComplexUnitConversions() {
        // Given - Ajouter des ingrédients en différentes unités
        menuService.ajouterIngredient(menuTest.getId(), tomates.getId(), 
                                    new BigDecimal("2500"), Unite.GRAMME, "Tomates en grammes"); // 2.5 kg
        
        // When - Confirmer le menu
        Menu menuConfirme = menuService.confirmerMenu(menuTest.getId());
        
        // Then - La conversion doit être correcte
        assertThat(menuConfirme.getStatut()).isEqualTo(StatutMenu.CONFIRME);
        
        // Le stock de tomates doit être réduit de 2.5kg (5.0 - 2.5 = 2.5)
        Produit tomatesApres = produitRepository.findById(tomates.getId()).orElseThrow();
        assertThat(tomatesApres.getQuantiteStock()).isEqualByComparingTo(new BigDecimal("2.5"));
        
        // Le coût doit être calculé correctement: 2.5kg * 3.50€/kg = 8.75€
        Menu menuFinal = menuService.getMenuById(menuTest.getId()).orElseThrow();
        assertThat(menuFinal.getCoutTotalIngredients()).isEqualByComparingTo(new BigDecimal("8.75"));
    }
}