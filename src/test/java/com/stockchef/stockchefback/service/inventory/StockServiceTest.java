package com.stockchef.stockchefback.service.inventory;

import com.stockchef.stockchefback.model.inventory.Produit;
import com.stockchef.stockchefback.model.inventory.StockMovement;
import com.stockchef.stockchefback.model.inventory.TypeMouvement;
import com.stockchef.stockchefback.model.inventory.Unite;
import com.stockchef.stockchefback.repository.inventory.ProduitRepository;
import com.stockchef.stockchefback.repository.inventory.StockMovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests TDD pour StockService - Integration Tests
 * Phase RED - Tests pour la logique métier critique du stock
 */
@SpringBootTest
@Transactional
@DisplayName("StockService - Tests TDD Integration")
class StockServiceTest {
    
    @Autowired
    private StockService stockService;
    
    @Autowired
    private ProduitRepository produitRepository;
    
    @Autowired
    private StockMovementRepository stockMovementRepository;
    
    @Autowired
    private UniteConversionService uniteConversionService;
    
    private Produit testProduit;
    
    @BeforeEach
    void setUp() {
        testProduit = new Produit(
            "Tomate",
            new BigDecimal("10.0"), // 10 kg de stock
            Unite.KILOGRAMME,
            new BigDecimal("2.50"),
            new BigDecimal("2.0") // Seuil d'alerte à 2 kg
        );
        testProduit = produitRepository.save(testProduit);
    }
    
    @Test
    @DisplayName("UC2: Décrementer stock avec audit trail")
    void shouldDecrementStockWithAuditTrail() {
        // Given
        BigDecimal quantiteARetirer = new BigDecimal("3.0");
        String motif = "Utilisation menu du jour";
        Long menuId = 123L;
        
        // When
        stockService.decrementerStock(testProduit.getId(), quantiteARetirer, motif, menuId);
        
        // Then - Vérifier mise à jour du stock
        Produit produitMisAJour = produitRepository.findById(testProduit.getId()).orElseThrow();
        assertThat(produitMisAJour.getQuantiteStock()).isEqualByComparingTo(new BigDecimal("7.0"));
        
        // Then - Vérifier création du mouvement de stock
        List<StockMovement> movements = stockMovementRepository.findByProduitOrderByDateMouvementDesc(testProduit);
        assertThat(movements).hasSize(1);
        
        StockMovement movement = movements.get(0);
        assertThat(movement.getTypeMouvement()).isEqualTo(TypeMouvement.SORTIE);
        assertThat(movement.getQuantite()).isEqualByComparingTo(new BigDecimal("-3.0")); // Négatif pour sortie
        assertThat(movement.getQuantiteApres()).isEqualByComparingTo(new BigDecimal("7.0"));
        assertThat(movement.getMotif()).isEqualTo(motif);
        assertThat(movement.getMenuId()).isEqualTo(menuId);
        assertThat(movement.getDateMouvement()).isNotNull();
    }
    
    @Test
    @DisplayName("Should increment stock with audit trail")
    void shouldIncrementStockWithAuditTrail() {
        // Given
        BigDecimal quantiteAAjouter = new BigDecimal("5.0");
        String motif = "Livraison fournisseur";
        
        // When
        stockService.incrementerStock(testProduit.getId(), quantiteAAjouter, motif);
        
        // Then - Vérifier mise à jour du stock
        Produit produitMisAJour = produitRepository.findById(testProduit.getId()).orElseThrow();
        assertThat(produitMisAJour.getQuantiteStock()).isEqualByComparingTo(new BigDecimal("15.0"));
        
        // Then - Vérifier création du mouvement de stock
        List<StockMovement> movements = stockMovementRepository.findByProduitOrderByDateMouvementDesc(testProduit);
        assertThat(movements).hasSize(1);
        
        StockMovement movement = movements.get(0);
        assertThat(movement.getTypeMouvement()).isEqualTo(TypeMouvement.ENTREE);
        assertThat(movement.getQuantite()).isEqualByComparingTo(new BigDecimal("5.0")); // Positif pour entrée
        assertThat(movement.getQuantiteApres()).isEqualByComparingTo(new BigDecimal("15.0"));
        assertThat(movement.getMotif()).isEqualTo(motif);
        assertThat(movement.getMenuId()).isNull();
    }
    
    @Test
    @DisplayName("Should convert units before decrementing stock")
    void shouldConvertUnitsBeforeDecrementingStock() {
        // Given - Produit en kg, mais on retire en grammes
        BigDecimal quantiteEnGrammes = new BigDecimal("2000"); // 2000g = 2kg
        String motif = "Utilisation recette";
        
        // When
        stockService.decrementerStockAvecConversion(
            testProduit.getId(), 
            quantiteEnGrammes, 
            Unite.GRAMME, 
            motif, 
            null
        );
        
        // Then - Stock doit être réduit de 2kg
        Produit produitMisAJour = produitRepository.findById(testProduit.getId()).orElseThrow();
        assertThat(produitMisAJour.getQuantiteStock()).isEqualByComparingTo(new BigDecimal("8.0"));
        
        // Then - Le mouvement doit être enregistré dans l'unité originale de la demande
        List<StockMovement> movements = stockMovementRepository.findByProduitOrderByDateMouvementDesc(testProduit);
        StockMovement movement = movements.get(0);
        assertThat(movement.getQuantite()).isEqualByComparingTo(new BigDecimal("-2000")); // Quantité originale en grammes (négative pour sortie)
        assertThat(movement.getUnite()).isEqualTo(Unite.GRAMME); // Unité originale
        assertThat(movement.getTypeMouvement()).isEqualTo(TypeMouvement.SORTIE);
    }
    
    @Test
    @DisplayName("Should throw exception when insufficient stock")
    void shouldThrowExceptionWhenInsufficientStock() {
        // Given - Essayer de retirer plus que disponible
        BigDecimal quantiteTropGrande = new BigDecimal("15.0"); // Plus que les 10kg disponibles
        
        // When & Then
        assertThatThrownBy(() -> stockService.decrementerStock(
            testProduit.getId(), 
            quantiteTropGrande, 
            "Tentative excessive", 
            null
        ))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Stock insuffisant")
        .hasMessageContaining("Tomate")
        .hasMessageContaining("10.0")
        .hasMessageContaining("15.0");
        
        // Then - Le stock ne doit pas avoir été modifié
        Produit produitInchange = produitRepository.findById(testProduit.getId()).orElseThrow();
        assertThat(produitInchange.getQuantiteStock()).isEqualByComparingTo(new BigDecimal("10.0"));
        
        // Then - Aucun mouvement ne doit avoir été créé
        List<StockMovement> movements = stockMovementRepository.findByProduitOrderByDateMouvementDesc(testProduit);
        assertThat(movements).isEmpty();
    }
    
    @Test
    @DisplayName("Should throw exception when product not found")
    void shouldThrowExceptionWhenProductNotFound() {
        // Given
        Long idInexistant = 99999L;
        
        // When & Then
        assertThatThrownBy(() -> stockService.decrementerStock(
            idInexistant, 
            new BigDecimal("1.0"), 
            "Test", 
            null
        ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Produit non trouvé")
        .hasMessageContaining("99999");
    }
    
    @Test
    @DisplayName("Should detect alert threshold after decrement")
    void shouldDetectAlertThresholdAfterDecrement() {
        // Given - Seuil à 2kg, on retire 9kg pour passer à 1kg (sous le seuil)
        BigDecimal quantiteARetirer = new BigDecimal("9.0");
        
        // When
        boolean isUnderThreshold = stockService.decrementerStock(
            testProduit.getId(), 
            quantiteARetirer, 
            "Test seuil", 
            null
        );
        
        // Then
        assertThat(isUnderThreshold).isTrue(); // Doit retourner true car sous le seuil
        
        Produit produitMisAJour = produitRepository.findById(testProduit.getId()).orElseThrow();
        assertThat(produitMisAJour.getQuantiteStock()).isEqualByComparingTo(new BigDecimal("1.0"));
        assertThat(produitMisAJour.isUnderAlertThreshold()).isTrue();
    }
    
    @Test
    @DisplayName("Should not trigger alert when stock remains above threshold")
    void shouldNotTriggerAlertWhenStockRemainsAboveThreshold() {
        // Given - Seuil à 2kg, on retire 1kg pour rester à 9kg (au-dessus du seuil)
        BigDecimal quantiteARetirer = new BigDecimal("1.0");
        
        // When
        boolean isUnderThreshold = stockService.decrementerStock(
            testProduit.getId(), 
            quantiteARetirer, 
            "Test pas d'alerte", 
            null
        );
        
        // Then
        assertThat(isUnderThreshold).isFalse();
        
        Produit produitMisAJour = produitRepository.findById(testProduit.getId()).orElseThrow();
        assertThat(produitMisAJour.isUnderAlertThreshold()).isFalse();
    }
    
    @Test
    @DisplayName("Should handle concurrent stock decrements safely")
    void shouldHandleConcurrentStockDecrementsafely() {
        // Given - Simulation de concurrence (ce test vérifie la transaction atomique)
        BigDecimal quantite1 = new BigDecimal("3.0");
        BigDecimal quantite2 = new BigDecimal("4.0");
        
        // When - Les deux opérations devraient réussir séquentiellement
        stockService.decrementerStock(testProduit.getId(), quantite1, "Première opération", 1L);
        stockService.decrementerStock(testProduit.getId(), quantite2, "Deuxième opération", 2L);
        
        // Then
        Produit produitFinal = produitRepository.findById(testProduit.getId()).orElseThrow();
        assertThat(produitFinal.getQuantiteStock()).isEqualByComparingTo(new BigDecimal("3.0")); // 10 - 3 - 4 = 3
        
        // Vérifier que les deux mouvements ont été créés
        List<StockMovement> movements = stockMovementRepository.findByProduitOrderByDateMouvementDesc(testProduit);
        assertThat(movements).hasSize(2);
    }
    
    @Test
    @DisplayName("Should validate null parameters")
    void shouldValidateNullParameters() {
        // When & Then
        assertThatThrownBy(() -> stockService.decrementerStock(null, new BigDecimal("1.0"), "test", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("L'ID du produit ne peut pas être nul");
        
        assertThatThrownBy(() -> stockService.decrementerStock(testProduit.getId(), null, "test", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("La quantité ne peut pas être nulle");
        
        assertThatThrownBy(() -> stockService.decrementerStock(testProduit.getId(), new BigDecimal("1.0"), null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Le motif ne peut pas être nul");
    }
    
    @Test
    @DisplayName("Should validate negative quantities")
    void shouldValidateNegativeQuantities() {
        // When & Then
        assertThatThrownBy(() -> stockService.decrementerStock(
            testProduit.getId(), 
            new BigDecimal("-1.0"), 
            "test", 
            null
        ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("La quantité doit être positive");
    }
}