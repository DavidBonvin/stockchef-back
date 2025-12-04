package com.stockchef.stockchefback.repository.inventory;

import com.stockchef.stockchefback.model.inventory.Produit;
import com.stockchef.stockchefback.model.inventory.Unite;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests TDD pour ProduitRepository
 * Phase RED - Écrire les tests avant l'implémentation
 */
@DataJpaTest
@DisplayName("ProduitRepository - Tests TDD")
class ProduitRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private ProduitRepository produitRepository;
    
    @Test
    @DisplayName("Should save and retrieve produit with all properties")
    void shouldSaveAndRetrieveProduit() {
        // Given - RED: Ce test va échouer car le repository n'existe pas encore
        Produit produit = new Produit(
            "Tomate",
            new BigDecimal("10.500"),
            Unite.KILOGRAMME,
            new BigDecimal("2.50"),
            new BigDecimal("1.000")
        );
        produit.setDatePeremption(LocalDate.now().plusDays(7));
        
        // When
        Produit saved = produitRepository.save(produit);
        
        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getNom()).isEqualTo("Tomate");
        assertThat(saved.getQuantiteStock()).isEqualTo(new BigDecimal("10.500"));
        assertThat(saved.getUnite()).isEqualTo(Unite.KILOGRAMME);
        assertThat(saved.getPrixUnitaire()).isEqualTo(new BigDecimal("2.50"));
        assertThat(saved.getSeuilAlerte()).isEqualTo(new BigDecimal("1.000"));
        assertThat(saved.getDateEntree()).isNotNull();
        assertThat(saved.getDeleted()).isFalse();
    }
    
    @Test
    @DisplayName("Should find produit by id")
    void shouldFindProduitById() {
        // Given
        Produit produit = createTestProduit("Carotte", "5.0", Unite.KILOGRAMME);
        Produit saved = entityManager.persistAndFlush(produit);
        
        // When
        Optional<Produit> found = produitRepository.findById(saved.getId());
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getNom()).isEqualTo("Carotte");
    }
    
    @Test
    @DisplayName("Should find all non-deleted produits")
    void shouldFindAllNonDeletedProduits() {
        // Given
        Produit produit1 = createTestProduit("Pomme", "3.0", Unite.KILOGRAMME);
        Produit produit2 = createTestProduit("Banane", "2.0", Unite.KILOGRAMME);
        Produit produit3 = createTestProduit("Orange", "4.0", Unite.KILOGRAMME);
        produit3.setDeleted(true); // Soft deleted
        
        entityManager.persistAndFlush(produit1);
        entityManager.persistAndFlush(produit2);
        entityManager.persistAndFlush(produit3);
        
        // When
        List<Produit> produits = produitRepository.findAll();
        
        // Then - Should only return non-deleted products
        assertThat(produits).hasSize(2);
        assertThat(produits).extracting(Produit::getNom)
                            .containsExactlyInAnyOrder("Pomme", "Banane");
    }
    
    @Test
    @DisplayName("Should find produits by nom containing text")
    void shouldFindProduitsByNomContaining() {
        // Given
        Produit tomate1 = createTestProduit("Tomate Rouge", "5.0", Unite.KILOGRAMME);
        Produit tomate2 = createTestProduit("Tomate Verte", "3.0", Unite.KILOGRAMME);
        Produit carotte = createTestProduit("Carotte", "2.0", Unite.KILOGRAMME);
        
        entityManager.persistAndFlush(tomate1);
        entityManager.persistAndFlush(tomate2);
        entityManager.persistAndFlush(carotte);
        
        // When
        List<Produit> found = produitRepository.findByNomContainingIgnoreCase("tomate");
        
        // Then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(Produit::getNom)
                         .containsExactlyInAnyOrder("Tomate Rouge", "Tomate Verte");
    }
    
    @Test
    @DisplayName("Should find produits under alert threshold")
    void shouldFindProduitsUnderAlertThreshold() {
        // Given
        Produit produitOk = createTestProduit("Produit OK", "10.0", Unite.KILOGRAMME);
        produitOk.setSeuilAlerte(new BigDecimal("5.0")); // Stock > seuil
        
        Produit produitAlerte = createTestProduit("Produit Alerte", "2.0", Unite.KILOGRAMME);
        produitAlerte.setSeuilAlerte(new BigDecimal("5.0")); // Stock < seuil
        
        entityManager.persistAndFlush(produitOk);
        entityManager.persistAndFlush(produitAlerte);
        
        // When
        List<Produit> alerteProduits = produitRepository.findProduitsUnderAlertThreshold();
        
        // Then
        assertThat(alerteProduits).hasSize(1);
        assertThat(alerteProduits.get(0).getNom()).isEqualTo("Produit Alerte");
    }
    
    @Test
    @DisplayName("Should find produits expired or expiring soon")
    void shouldFindProduitsExpiringOrExpired() {
        // Given
        LocalDate today = LocalDate.now();
        
        Produit produitOk = createTestProduit("Produit OK", "5.0", Unite.KILOGRAMME);
        produitOk.setDatePeremption(today.plusDays(10)); // OK
        
        Produit produitExpire = createTestProduit("Produit Expiré", "3.0", Unite.KILOGRAMME);
        produitExpire.setDatePeremption(today.minusDays(1)); // Expiré
        
        Produit produitBientotExpire = createTestProduit("Produit Bientôt Expiré", "2.0", Unite.KILOGRAMME);
        produitBientotExpire.setDatePeremption(today.plusDays(2)); // Bientôt expiré
        
        entityManager.persistAndFlush(produitOk);
        entityManager.persistAndFlush(produitExpire);
        entityManager.persistAndFlush(produitBientotExpire);
        
        // When
        List<Produit> expiringProduits = produitRepository.findProduitsExpiringWithinDays(3);
        
        // Then
        assertThat(expiringProduits).hasSize(2);
        assertThat(expiringProduits).extracting(Produit::getNom)
                                   .containsExactlyInAnyOrder("Produit Expiré", "Produit Bientôt Expiré");
    }
    
    @Test
    @DisplayName("Should update quantite stock")
    void shouldUpdateQuantiteStock() {
        // Given
        Produit produit = createTestProduit("Test Produit", "10.0", Unite.KILOGRAMME);
        Produit saved = entityManager.persistAndFlush(produit);
        
        // When
        int updated = produitRepository.updateQuantiteStock(saved.getId(), new BigDecimal("7.0"));
        entityManager.clear(); // Force refresh
        
        // Then
        assertThat(updated).isEqualTo(1);
        Produit updatedProduit = produitRepository.findById(saved.getId()).orElseThrow();
        assertThat(updatedProduit.getQuantiteStock()).isEqualTo(new BigDecimal("7.0"));
    }
    
    // Helper method
    private Produit createTestProduit(String nom, String quantite, Unite unite) {
        return new Produit(
            nom,
            new BigDecimal(quantite),
            unite,
            new BigDecimal("1.00"),
            new BigDecimal("0.5")
        );
    }
}