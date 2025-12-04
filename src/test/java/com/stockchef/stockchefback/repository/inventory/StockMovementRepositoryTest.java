package com.stockchef.stockchefback.repository.inventory;

import com.stockchef.stockchefback.model.inventory.Produit;
import com.stockchef.stockchefback.model.inventory.StockMovement;
import com.stockchef.stockchefback.model.inventory.TypeMouvement;
import com.stockchef.stockchefback.model.inventory.Unite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests TDD pour StockMovementRepository
 * Phase RED - Tests pour l'audit trail des mouvements de stock
 */
@DataJpaTest
@DisplayName("StockMovementRepository - Tests TDD")
class StockMovementRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private StockMovementRepository stockMovementRepository;
    
    private Produit testProduit;
    
    @BeforeEach
    void setUp() {
        testProduit = new Produit(
            "Test Produit",
            new BigDecimal("10.0"),
            Unite.KILOGRAMME,
            new BigDecimal("2.50"),
            new BigDecimal("1.0")
        );
        // Configurar manuellement la date pour éviter les problèmes avec @CreatedDate dans les tests
        testProduit.setDateEntree(LocalDateTime.now());
        testProduit = entityManager.persistAndFlush(testProduit);
    }
    
    @Test
    @DisplayName("Should save and retrieve stock movement")
    void shouldSaveAndRetrieveStockMovement() {
        // Given
        StockMovement movement = StockMovement.createSortie(
            testProduit,
            new BigDecimal("2.0"),
            Unite.KILOGRAMME,
            new BigDecimal("8.0"),
            "Utilisation menu du jour",
            123L
        );
        
        // When
        StockMovement saved = stockMovementRepository.save(movement);
        
        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getProduit().getId()).isEqualTo(testProduit.getId());
        assertThat(saved.getTypeMouvement()).isEqualTo(TypeMouvement.SORTIE);
        assertThat(saved.getQuantite()).isEqualTo(new BigDecimal("-2.0"));
        assertThat(saved.getUnite()).isEqualTo(Unite.KILOGRAMME);
        assertThat(saved.getQuantiteApres()).isEqualTo(new BigDecimal("8.0"));
        assertThat(saved.getMotif()).isEqualTo("Utilisation menu du jour");
        assertThat(saved.getMenuId()).isEqualTo(123L);
        assertThat(saved.getDateMouvement()).isNotNull();
        
        // Vérifier que l'objet récupéré est identique au sauvegardé
        StockMovement retrieved = stockMovementRepository.findById(saved.getId()).orElse(null);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getId()).isEqualTo(saved.getId());
        assertThat(retrieved.getProduit().getId()).isEqualTo(saved.getProduit().getId());
        assertThat(retrieved.getTypeMouvement()).isEqualTo(saved.getTypeMouvement());
        assertThat(retrieved.getQuantite()).isEqualTo(saved.getQuantite());
        assertThat(retrieved.getUnite()).isEqualTo(saved.getUnite());
        assertThat(retrieved.getQuantiteApres()).isEqualTo(saved.getQuantiteApres());
        assertThat(retrieved.getMotif()).isEqualTo(saved.getMotif());
        assertThat(retrieved.getMenuId()).isEqualTo(saved.getMenuId());
        assertThat(retrieved.getDateMouvement()).isEqualTo(saved.getDateMouvement());
    }
    
    @Test
    @DisplayName("Should find movements by produit ordered by date desc")
    void shouldFindMovementsByProduitOrderedByDateDesc() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        
        StockMovement movement1 = createMovement(TypeMouvement.ENTREE, new BigDecimal("5.0"), "Premier");
        movement1.setDateMouvement(now.minusHours(2));
        
        StockMovement movement2 = createMovement(TypeMouvement.SORTIE, new BigDecimal("-2.0"), "Deuxième");
        movement2.setDateMouvement(now.minusHours(1));
        
        StockMovement movement3 = createMovement(TypeMouvement.SORTIE, new BigDecimal("-1.0"), "Troisième");
        movement3.setDateMouvement(now);
        
        entityManager.persistAndFlush(movement1);
        entityManager.persistAndFlush(movement2);
        entityManager.persistAndFlush(movement3);
        
        // When
        List<StockMovement> movements = stockMovementRepository.findByProduitOrderByDateMouvementDesc(testProduit);
        
        // Then
        assertThat(movements).hasSize(3);
        assertThat(movements.get(0).getMotif()).isEqualTo("Troisième"); // Plus récent en premier
        assertThat(movements.get(1).getMotif()).isEqualTo("Deuxième");
        assertThat(movements.get(2).getMotif()).isEqualTo("Premier");
    }
    
    @Test
    @DisplayName("Should find movements by produit and type")
    void shouldFindMovementsByProduitAndType() {
        // Given
        StockMovement entree1 = createMovement(TypeMouvement.ENTREE, new BigDecimal("5.0"), "Entrée 1");
        StockMovement entree2 = createMovement(TypeMouvement.ENTREE, new BigDecimal("3.0"), "Entrée 2");
        StockMovement sortie1 = createMovement(TypeMouvement.SORTIE, new BigDecimal("-2.0"), "Sortie 1");
        
        entityManager.persistAndFlush(entree1);
        entityManager.persistAndFlush(entree2);
        entityManager.persistAndFlush(sortie1);
        
        // When
        List<StockMovement> entrees = stockMovementRepository.findByProduitAndTypeMouvement(testProduit, TypeMouvement.ENTREE);
        List<StockMovement> sorties = stockMovementRepository.findByProduitAndTypeMouvement(testProduit, TypeMouvement.SORTIE);
        
        // Then
        assertThat(entrees).hasSize(2);
        assertThat(entrees).extracting(StockMovement::getMotif)
                          .containsExactlyInAnyOrder("Entrée 1", "Entrée 2");
        
        assertThat(sorties).hasSize(1);
        assertThat(sorties.get(0).getMotif()).isEqualTo("Sortie 1");
    }
    
    @Test
    @DisplayName("Should find movements by menu id")
    void shouldFindMovementsByMenuId() {
        // Given
        Long menuId = 123L;
        
        StockMovement movement1 = createMovement(TypeMouvement.SORTIE, new BigDecimal("-2.0"), "Menu 123 - Tomate");
        movement1.setMenuId(menuId);
        
        StockMovement movement2 = createMovement(TypeMouvement.SORTIE, new BigDecimal("-1.0"), "Menu 123 - Carotte");
        movement2.setMenuId(menuId);
        
        StockMovement movement3 = createMovement(TypeMouvement.SORTIE, new BigDecimal("-1.5"), "Menu 456 - Autre");
        movement3.setMenuId(456L);
        
        entityManager.persistAndFlush(movement1);
        entityManager.persistAndFlush(movement2);
        entityManager.persistAndFlush(movement3);
        
        // When
        List<StockMovement> movementsMenu123 = stockMovementRepository.findByMenuId(menuId);
        
        // Then
        assertThat(movementsMenu123).hasSize(2);
        assertThat(movementsMenu123).extracting(StockMovement::getMotif)
                                   .containsExactlyInAnyOrder("Menu 123 - Tomate", "Menu 123 - Carotte");
    }
    
    @Test
    @DisplayName("Should find movements between dates")
    void shouldFindMovementsBetweenDates() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(2);
        LocalDateTime endDate = LocalDateTime.now().minusDays(1);
        
        StockMovement oldMovement = createMovement(TypeMouvement.ENTREE, new BigDecimal("5.0"), "Trop ancien");
        oldMovement.setDateMouvement(startDate.minusHours(1));
        
        StockMovement validMovement1 = createMovement(TypeMouvement.SORTIE, new BigDecimal("-1.0"), "Valide 1");
        validMovement1.setDateMouvement(startDate.plusHours(1));
        
        StockMovement validMovement2 = createMovement(TypeMouvement.SORTIE, new BigDecimal("-2.0"), "Valide 2");
        validMovement2.setDateMouvement(endDate.minusHours(1));
        
        StockMovement recentMovement = createMovement(TypeMouvement.ENTREE, new BigDecimal("3.0"), "Trop récent");
        recentMovement.setDateMouvement(endDate.plusHours(1));
        
        entityManager.persistAndFlush(oldMovement);
        entityManager.persistAndFlush(validMovement1);
        entityManager.persistAndFlush(validMovement2);
        entityManager.persistAndFlush(recentMovement);
        
        // When
        List<StockMovement> movements = stockMovementRepository.findByDateMouvementBetween(startDate, endDate);
        
        // Then
        assertThat(movements).hasSize(2);
        assertThat(movements).extracting(StockMovement::getMotif)
                             .containsExactlyInAnyOrder("Valide 1", "Valide 2");
    }
    
    @Test
    @DisplayName("Should calculate total quantity by type for produit")
    void shouldCalculateTotalQuantityByTypeForProduit() {
        // Given
        StockMovement entree1 = createMovement(TypeMouvement.ENTREE, new BigDecimal("5.0"), "Entrée 1");
        StockMovement entree2 = createMovement(TypeMouvement.ENTREE, new BigDecimal("3.0"), "Entrée 2");
        StockMovement sortie1 = createMovement(TypeMouvement.SORTIE, new BigDecimal("-2.0"), "Sortie 1");
        StockMovement sortie2 = createMovement(TypeMouvement.SORTIE, new BigDecimal("-1.5"), "Sortie 2");
        
        entityManager.persistAndFlush(entree1);
        entityManager.persistAndFlush(entree2);
        entityManager.persistAndFlush(sortie1);
        entityManager.persistAndFlush(sortie2);
        
        // When
        BigDecimal totalEntrees = stockMovementRepository.getTotalQuantiteByProduitAndType(testProduit.getId(), TypeMouvement.ENTREE);
        BigDecimal totalSorties = stockMovementRepository.getTotalQuantiteByProduitAndType(testProduit.getId(), TypeMouvement.SORTIE);
        
        // Then
        assertThat(totalEntrees).isEqualTo(new BigDecimal("8.0")); // 5.0 + 3.0
        assertThat(totalSorties).isEqualTo(new BigDecimal("-3.5")); // -2.0 + -1.5
    }
    
    // Helper method
    private StockMovement createMovement(TypeMouvement type, BigDecimal quantite, String motif) {
        StockMovement movement = new StockMovement(
            testProduit,
            type,
            quantite,
            Unite.KILOGRAMME, // Unité par défaut
            new BigDecimal("10.0"), // Quantité après (exemple)
            motif
        );
        return movement;
    }
}