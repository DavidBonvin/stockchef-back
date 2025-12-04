package com.stockchef.stockchefback.dto.menu;

import com.stockchef.stockchefback.model.inventory.Unite;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO pour l'ajout/modification d'ingrédients dans un menu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuIngredientDTO {
    
    private Long id;
    
    @NotNull(message = "L'ID du produit est obligatoire")
    private Long produitId;
    
    // Informations du produit (lecture seule)
    private String produitNom;
    
    @NotNull(message = "La quantité nécessaire est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "La quantité doit être positive")
    @Digits(integer = 8, fraction = 3, message = "Format de quantité invalide")
    private BigDecimal quantiteNecessaire;
    
    @NotNull(message = "L'unité utilisée est obligatoire")
    private Unite uniteUtilisee;
    
    // Champs calculés (lecture seule)
    private BigDecimal quantiteConvertieStockUnit;
    private BigDecimal coutIngredient;
    private Boolean stockSuffisant;
    private BigDecimal quantiteManquante;
    
    @Size(max = 255, message = "Les notes ne peuvent pas dépasser 255 caractères")
    private String notes;
}