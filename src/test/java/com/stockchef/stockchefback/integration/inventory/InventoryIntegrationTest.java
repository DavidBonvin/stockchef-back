package com.stockchef.stockchefback.integration.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockchef.stockchefback.dto.inventory.ProduitCreateRequest;
import com.stockchef.stockchefback.dto.inventory.StockMovementRequest;
import com.stockchef.stockchefback.model.inventory.Unite;
import com.stockchef.stockchefback.repository.inventory.ProduitRepository;
import com.stockchef.stockchefback.repository.inventory.StockMovementRepository;
import com.stockchef.stockchefback.model.inventory.Produit;
import com.stockchef.stockchefback.model.inventory.StockMovement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour l'API Inventory
 * Teste l'ensemble du flux : Controller -> Service -> Repository
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class InventoryIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ProduitRepository produitRepository;
    
    @Autowired
    private StockMovementRepository stockMovementRepository;
    
    @BeforeEach
    void setUp() {
        stockMovementRepository.deleteAll();
        produitRepository.deleteAll();
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateProduit_Success() throws Exception {
        // Given
        ProduitCreateRequest request = new ProduitCreateRequest(
            "Tomates",
            new BigDecimal("5.0"),
            Unite.KILOGRAMME,
            new BigDecimal("12.50"),
            new BigDecimal("1.0"),
            LocalDate.now().plusDays(7),
            "Tomates fraîches du jardin"
        );
        
        // When & Then
        mockMvc.perform(post("/inventory/produits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nom").value("Tomates"))
                .andExpect(jsonPath("$.quantiteStock").value(5.0))
                .andExpect(jsonPath("$.unite").value("KILOGRAMME"))
                .andExpect(jsonPath("$.prixUnitaire").value(12.50))
                .andExpect(jsonPath("$.seuilAlerte").value(1.0));
        
        // Vérifier en base
        List<Produit> produits = produitRepository.findAll();
        assertThat(produits).hasSize(1);
        assertThat(produits.get(0).getNom()).isEqualTo("Tomates");
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateProduit_ValidationErrors() throws Exception {
        // Given - Requête avec des erreurs de validation
        ProduitCreateRequest request = new ProduitCreateRequest(
            "", // Nom vide
            new BigDecimal("-1.0"), // Quantité négative
            null, // Unité nulle
            new BigDecimal("0.0"), // Prix nul
            new BigDecimal("-5.0"), // Seuil négatif
            LocalDate.now().minusDays(1), // Date passée
            "x".repeat(600) // Description trop longue
        );
        
        // When & Then
        mockMvc.perform(post("/inventory/produits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Erreurs de validation"));
    }
    
    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    void testSortieStock_Success() throws Exception {
        // Given - Créer un produit avec du stock
        Produit produit = new Produit();
        produit.setNom("Farine");
        produit.setQuantiteStock(new BigDecimal("10.0"));
        produit.setUnite(Unite.KILOGRAMME);
        produit.setPrixUnitaire(new BigDecimal("2.50"));
        produit.setSeuilAlerte(new BigDecimal("2.0"));
        produit = produitRepository.save(produit);
        
        StockMovementRequest request = new StockMovementRequest(
            new BigDecimal("3.0"),
            null, // Même unité que le produit
            "Préparation pain",
            null
        );
        
        // When & Then
        mockMvc.perform(post("/inventory/produits/{id}/sortie", produit.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantiteStock").value(7.0));
        
        // Vérifier le mouvement de stock enregistré
        List<StockMovement> movements = stockMovementRepository.findByProduitOrderByDateMouvementDesc(produit);
        assertThat(movements).hasSize(1);
        assertThat(movements.get(0).getQuantite()).isEqualTo(new BigDecimal("3.0"));
    }
    
    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    void testSortieStock_StockInsuffisant() throws Exception {
        // Given - Produit avec peu de stock
        Produit produit = new Produit();
        produit.setNom("Huile");
        produit.setQuantiteStock(new BigDecimal("2.0"));
        produit.setUnite(Unite.LITRE);
        produit.setPrixUnitaire(new BigDecimal("5.0"));
        produit.setSeuilAlerte(new BigDecimal("1.0"));
        produit = produitRepository.save(produit);
        
        StockMovementRequest request = new StockMovementRequest(
            new BigDecimal("5.0"), // Plus que disponible
            null,
            "Test sortie excessive",
            null
        );
        
        // When & Then
        mockMvc.perform(post("/inventory/produits/{id}/sortie", produit.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Stock insuffisant"));
    }
    
    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    void testSortieStockAvecConversion_Success() throws Exception {
        // Given - Produit en kilogrammes
        Produit produit = new Produit();
        produit.setNom("Sucre");
        produit.setQuantiteStock(new BigDecimal("2.0")); // 2 kg
        produit.setUnite(Unite.KILOGRAMME);
        produit.setPrixUnitaire(new BigDecimal("3.0"));
        produit.setSeuilAlerte(new BigDecimal("0.5"));
        produit = produitRepository.save(produit);
        
        StockMovementRequest request = new StockMovementRequest(
            new BigDecimal("500.0"), // 500g = 0.5kg
            Unite.GRAMME,
            "Conversion automatique",
            null
        );
        
        // When & Then
        mockMvc.perform(post("/inventory/produits/{id}/sortie", produit.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantiteStock").value(1.5)); // 2.0 - 0.5 = 1.5
        
        // Vérifier la conversion dans le mouvement de stock
        List<StockMovement> movements = stockMovementRepository.findByProduitOrderByDateMouvementDesc(produit);
        assertThat(movements).hasSize(1);
        assertThat(movements.get(0).getQuantite()).isEqualTo(new BigDecimal("500.0"));
        assertThat(movements.get(0).getUnite()).isEqualTo(Unite.GRAMME);
    }
    
    @Test
    @WithMockUser(roles = "GESTIONNAIRE")
    void testEntreeStock_Success() throws Exception {
        // Given
        Produit produit = new Produit();
        produit.setNom("Carottes");
        produit.setQuantiteStock(new BigDecimal("3.0"));
        produit.setUnite(Unite.KILOGRAMME);
        produit.setPrixUnitaire(new BigDecimal("1.80"));
        produit.setSeuilAlerte(new BigDecimal("1.0"));
        produit = produitRepository.save(produit);
        
        StockMovementRequest request = new StockMovementRequest(
            new BigDecimal("2.0"),
            null,
            "Réapprovisionnement",
            null
        );
        
        // When & Then
        mockMvc.perform(post("/inventory/produits/{id}/entree", produit.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantiteStock").value(5.0));
    }
    
    @Test
    @WithMockUser(roles = "USER")
    void testGetProduitsEnAlerte() throws Exception {
        // Given - Créer des produits, certains en alerte
        Produit produitOk = new Produit();
        produitOk.setNom("Produit OK");
        produitOk.setQuantiteStock(new BigDecimal("10.0"));
        produitOk.setSeuilAlerte(new BigDecimal("5.0"));
        produitOk.setUnite(Unite.KILOGRAMME);
        produitOk.setPrixUnitaire(new BigDecimal("1.0"));
        produitRepository.save(produitOk);
        
        Produit produitAlerte = new Produit();
        produitAlerte.setNom("Produit Alerte");
        produitAlerte.setQuantiteStock(new BigDecimal("2.0"));
        produitAlerte.setSeuilAlerte(new BigDecimal("5.0"));
        produitAlerte.setUnite(Unite.KILOGRAMME);
        produitAlerte.setPrixUnitaire(new BigDecimal("1.0"));
        produitRepository.save(produitAlerte);
        
        // When & Then
        mockMvc.perform(get("/inventory/produits/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nom").value("Produit Alerte"));
    }
    
    @Test
    void testAccesNonAutorise() throws Exception {
        // When & Then - Test sans authentification
        mockMvc.perform(get("/inventory/produits"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(roles = "USER")
    void testAccesInterditPourCreation() throws Exception {
        // Given
        ProduitCreateRequest request = new ProduitCreateRequest(
            "Test",
            new BigDecimal("1.0"),
            Unite.KILOGRAMME,
            new BigDecimal("1.0"),
            new BigDecimal("0.5"),
            null,
            "Test"
        );
        
        // When & Then - Utilisateur normal ne peut pas créer
        mockMvc.perform(post("/inventory/produits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}