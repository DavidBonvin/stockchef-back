package com.stockchef.stockchefback.controller.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockchef.stockchefback.dto.inventory.ProduitCreateRequest;
import com.stockchef.stockchefback.dto.inventory.ProduitUpdateRequest;
import com.stockchef.stockchefback.dto.inventory.StockMovementRequest;
import com.stockchef.stockchefback.model.inventory.Produit;
import com.stockchef.stockchefback.model.inventory.Unite;
import com.stockchef.stockchefback.repository.inventory.ProduitRepository;
import com.stockchef.stockchefback.service.JwtService;
import com.stockchef.stockchefback.model.User;
import com.stockchef.stockchefback.model.UserRole;
import com.stockchef.stockchefback.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests TDD pour ProduitController
 * Phase RED - Tests d'intégration avec sécurité JWT
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@DisplayName("ProduitController - Tests TDD Integration")
class ProduitControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ProduitRepository produitRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtService jwtService;
    
    private String chefToken;
    private String adminToken;
    private String employeeToken;
    private User testChef;
    private User testAdmin;
    private User testEmployee;
    
    @BeforeEach
    void setUp() {
        // Créer utilisateurs de test avec JWT tokens
        testChef = createUser("chef@test.com", UserRole.ROLE_CHEF);
        testAdmin = createUser("admin@test.com", UserRole.ROLE_ADMIN);
        testEmployee = createUser("employee@test.com", UserRole.ROLE_EMPLOYEE);
        
        chefToken = jwtService.generateToken(testChef);
        adminToken = jwtService.generateToken(testAdmin);
        employeeToken = jwtService.generateToken(testEmployee);
    }
    
    @Test
    @DisplayName("POST /api/inventory/produits - Should create produit as CHEF")
    void shouldCreateProduitAsChef() throws Exception {
        // Given
        ProduitCreateRequest request = new ProduitCreateRequest(
            "Tomate",
            new BigDecimal("10.0"),
            Unite.KILOGRAMME,
            new BigDecimal("2.50"),
            new BigDecimal("1.0"),
            LocalDate.now().plusDays(7),
            "Tomates fraîches du marché"
        );
        
        // When & Then
        mockMvc.perform(post("/inventory/produits")
                .header("Authorization", "Bearer " + chefToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nom").value("Tomate"))
                .andExpect(jsonPath("$.quantiteStock").value(10.0))
                .andExpect(jsonPath("$.unite").value("KILOGRAMME"))
                .andExpect(jsonPath("$.prixUnitaire").value(2.50))
                .andExpect(jsonPath("$.seuilAlerte").value(1.0))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.dateEntree").exists());
    }
    
    @Test
    @DisplayName("POST /api/inventory/produits - Should create produit as ADMIN")
    void shouldCreateProduitAsAdmin() throws Exception {
        // Given
        ProduitCreateRequest request = new ProduitCreateRequest(
            "Carotte",
            new BigDecimal("5.0"),
            Unite.KILOGRAMME,
            new BigDecimal("1.20"),
            new BigDecimal("0.5"),
            null, // Pas de date de péremption
            null
        );
        
        // When & Then
        mockMvc.perform(post("/inventory/produits")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nom").value("Carotte"))
                .andExpect(jsonPath("$.datePeremption").isEmpty());
    }
    
    @Test
    @DisplayName("POST /api/inventory/produits - Should reject EMPLOYEE access")
    void shouldRejectEmployeeAccess() throws Exception {
        // Given
        ProduitCreateRequest request = new ProduitCreateRequest(
            "Test",
            new BigDecimal("1.0"),
            Unite.UNITE,
            new BigDecimal("1.00"),
            new BigDecimal("0.1"),
            null,
            null
        );
        
        // When & Then
        mockMvc.perform(post("/inventory/produits")
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("POST /api/inventory/produits - Should validate request data")
    void shouldValidateRequestData() throws Exception {
        // Given - Request avec données invalides
        ProduitCreateRequest invalidRequest = new ProduitCreateRequest(
            "", // Nom vide
            new BigDecimal("-1.0"), // Quantité négative
            null, // Unité null
            new BigDecimal("0.0"), // Prix zéro
            new BigDecimal("-0.5"), // Seuil négatif
            LocalDate.now().minusDays(1), // Date passée
            null
        );
        
        // When & Then
        mockMvc.perform(post("/inventory/produits")
                .header("Authorization", "Bearer " + chefToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors", hasSize(greaterThan(0))));
    }
    
    @Test
    @DisplayName("GET /api/inventory/produits - Should list all produits")
    void shouldListAllProduits() throws Exception {
        // Given - Créer quelques produits
        createTestProduit("Tomate", "10.0", Unite.KILOGRAMME);
        createTestProduit("Carotte", "5.0", Unite.KILOGRAMME);
        
        // When & Then
        mockMvc.perform(get("/inventory/produits")
                .header("Authorization", "Bearer " + chefToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].nom", containsInAnyOrder("Tomate", "Carotte")));
    }
    
    @Test
    @DisplayName("GET /api/inventory/produits/{id} - Should get produit by id")
    void shouldGetProduitById() throws Exception {
        // Given
        Produit produit = createTestProduit("Pomme", "3.0", Unite.KILOGRAMME);
        
        // When & Then
        mockMvc.perform(get("/inventory/produits/{id}", produit.getId())
                .header("Authorization", "Bearer " + chefToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(produit.getId()))
                .andExpect(jsonPath("$.nom").value("Pomme"))
                .andExpect(jsonPath("$.quantiteStock").value(3.0));
    }
    
    @Test
    @DisplayName("GET /api/inventory/produits/{id} - Should return 404 for non-existent produit")
    void shouldReturn404ForNonExistentProduit() throws Exception {
        // When & Then
        mockMvc.perform(get("/inventory/produits/99999")
                .header("Authorization", "Bearer " + chefToken))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("PUT /api/inventory/produits/{id} - Should update produit")
    void shouldUpdateProduit() throws Exception {
        // Given
        Produit produit = createTestProduit("Ancien Nom", "10.0", Unite.KILOGRAMME);
        
        ProduitUpdateRequest updateRequest = new ProduitUpdateRequest(
            "Nouveau Nom",
            new BigDecimal("3.00"),
            new BigDecimal("2.0"),
            LocalDate.now().plusDays(5),
            "Description mise à jour"
        );
        
        // When & Then
        mockMvc.perform(put("/inventory/produits/{id}", produit.getId())
                .header("Authorization", "Bearer " + chefToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Nouveau Nom"))
                .andExpect(jsonPath("$.prixUnitaire").value(3.00))
                .andExpect(jsonPath("$.seuilAlerte").value(2.0));
    }
    
    @Test
    @DisplayName("POST /api/inventory/produits/{id}/sortie - UC2: Should decrement stock")
    void shouldDecrementStock() throws Exception {
        // Given
        Produit produit = createTestProduit("Test Produit", "10.0", Unite.KILOGRAMME);
        
        StockMovementRequest request = new StockMovementRequest(
            new BigDecimal("3.0"),
            null, // Même unité que le produit
            "Utilisation menu du jour",
            123L
        );
        
        // When & Then
        mockMvc.perform(post("/inventory/produits/{id}/sortie", produit.getId())
                .header("Authorization", "Bearer " + chefToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantiteStock").value(7.0))
                .andExpect(jsonPath("$.isUnderAlertThreshold").value(false));
    }
    
    @Test
    @DisplayName("POST /api/inventory/produits/{id}/sortie - Should handle unit conversion")
    void shouldHandleUnitConversionInSortie() throws Exception {
        // Given - Produit en kg
        Produit produit = createTestProduit("Test Produit", "10.0", Unite.KILOGRAMME);
        
        StockMovementRequest request = new StockMovementRequest(
            new BigDecimal("2000"), // 2000 grammes = 2 kg
            Unite.GRAMME,
            "Utilisation en grammes",
            null
        );
        
        // When & Then
        mockMvc.perform(post("/inventory/produits/{id}/sortie", produit.getId())
                .header("Authorization", "Bearer " + chefToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantiteStock").value(8.0)); // 10 - 2 = 8 kg
    }
    
    @Test
    @DisplayName("POST /api/inventory/produits/{id}/sortie - Should return alert when under threshold")
    void shouldReturnAlertWhenUnderThreshold() throws Exception {
        // Given - Produit avec seuil à 2kg
        Produit produit = createTestProduit("Test Produit", "3.0", Unite.KILOGRAMME);
        produit.setSeuilAlerte(new BigDecimal("2.0"));
        produitRepository.save(produit);
        
        StockMovementRequest request = new StockMovementRequest(
            new BigDecimal("2.5"), // Sortie qui va passer sous le seuil
            null,
            "Test seuil d'alerte",
            null
        );
        
        // When & Then
        mockMvc.perform(post("/inventory/produits/{id}/sortie", produit.getId())
                .header("Authorization", "Bearer " + chefToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantiteStock").value(0.5))
                .andExpect(jsonPath("$.isUnderAlertThreshold").value(true));
    }
    
    @Test
    @DisplayName("POST /api/inventory/produits/{id}/sortie - Should reject insufficient stock")
    void shouldRejectInsufficientStock() throws Exception {
        // Given
        Produit produit = createTestProduit("Test Produit", "2.0", Unite.KILOGRAMME);
        
        StockMovementRequest request = new StockMovementRequest(
            new BigDecimal("5.0"), // Plus que disponible
            null,
            "Tentative excessive",
            null
        );
        
        // When & Then
        mockMvc.perform(post("/inventory/produits/{id}/sortie", produit.getId())
                .header("Authorization", "Bearer " + chefToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Stock insuffisant")));
    }
    
    @Test
    @DisplayName("POST /api/inventory/produits/{id}/entree - Should increment stock")
    void shouldIncrementStock() throws Exception {
        // Given
        Produit produit = createTestProduit("Test Produit", "5.0", Unite.KILOGRAMME);
        
        StockMovementRequest request = new StockMovementRequest(
            new BigDecimal("3.0"),
            null,
            "Livraison fournisseur",
            null
        );
        
        // When & Then
        mockMvc.perform(post("/inventory/produits/{id}/entree", produit.getId())
                .header("Authorization", "Bearer " + chefToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantiteStock").value(8.0));
    }
    
    @Test
    @DisplayName("DELETE /api/inventory/produits/{id} - Should soft delete produit as ADMIN")
    void shouldSoftDeleteProduitAsAdmin() throws Exception {
        // Given
        Produit produit = createTestProduit("Test Produit", "5.0", Unite.KILOGRAMME);
        
        // When & Then
        mockMvc.perform(delete("/inventory/produits/{id}", produit.getId())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }
    
    @Test
    @DisplayName("DELETE /api/inventory/produits/{id} - Should allow CHEF access")
    void shouldAllowChefDeleteAccess() throws Exception {
        // Given
        Produit produit = createTestProduit("Test Produit", "5.0", Unite.KILOGRAMME);
        
        // When & Then
        mockMvc.perform(delete("/inventory/produits/{id}", produit.getId())
                .header("Authorization", "Bearer " + chefToken))
                .andExpect(status().isNoContent());
    }
    
    @Test
    @DisplayName("DELETE /api/inventory/produits/{id} - Should reject EMPLOYEE access")
    void shouldRejectEmployeeDeleteAccess() throws Exception {
        // Given
        Produit produit = createTestProduit("Test Produit", "5.0", Unite.KILOGRAMME);
        
        // When & Then
        mockMvc.perform(delete("/inventory/produits/{id}", produit.getId())
                .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @DisplayName("GET /api/inventory/produits/alerts - Should get products under threshold")
    void shouldGetProductsUnderThreshold() throws Exception {
        // Given
        Produit produitOk = createTestProduit("Produit OK", "10.0", Unite.KILOGRAMME);
        produitOk.setSeuilAlerte(new BigDecimal("5.0"));
        
        Produit produitAlerte = createTestProduit("Produit Alerte", "2.0", Unite.KILOGRAMME);
        produitAlerte.setSeuilAlerte(new BigDecimal("5.0")); // Sous le seuil
        
        produitRepository.save(produitOk);
        produitRepository.save(produitAlerte);
        
        // When & Then
        mockMvc.perform(get("/inventory/produits/alerts")
                .header("Authorization", "Bearer " + chefToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nom").value("Produit Alerte"));
    }
    
    // Helper methods
    private User createUser(String email, UserRole role) {
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode("password123"))
                .firstName("Test")
                .lastName("User")
                .role(role)
                .isActive(true)
                .build();
        return userRepository.save(user);
    }
    
    private Produit createTestProduit(String nom, String quantite, Unite unite) {
        Produit produit = new Produit(
            nom,
            new BigDecimal(quantite),
            unite,
            new BigDecimal("1.00"),
            new BigDecimal("0.5")
        );
        return produitRepository.save(produit);
    }
}
