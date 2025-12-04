package com.stockchef.stockchefback.dto.menu;

import com.stockchef.stockchefback.model.menu.StatutMenu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de réponse pour un menu complet avec toutes ses informations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuResponseDTO {
    
    private Long id;
    private String nom;
    private String description;
    private LocalDate dateService;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private StatutMenu statut;
    
    // Informations financières
    private BigDecimal prixVente;
    private BigDecimal coutTotalIngredients;
    private BigDecimal marge;
    private BigDecimal margePercentage;
    
    // État opérationnel
    private Boolean peutEtrePrepare;
    
    // Ingrédients du menu
    private List<MenuIngredientDTO> ingredients;
    
    // Statistiques calculées
    public int getNombreIngredients() {
        return ingredients != null ? ingredients.size() : 0;
    }
    
    public long getNombreIngredientsManquants() {
        return ingredients != null ? ingredients.stream()
            .filter(ing -> ing.getStockSuffisant() != null && !ing.getStockSuffisant())
            .count() : 0;
    }
    
    public boolean hasIngredientsMissing() {
        return getNombreIngredientsManquants() > 0;
    }
}