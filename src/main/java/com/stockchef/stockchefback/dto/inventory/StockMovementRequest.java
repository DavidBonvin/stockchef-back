package com.stockchef.stockchefback.dto.inventory;

import com.stockchef.stockchefback.model.inventory.Unite;
import com.stockchef.stockchefback.validation.PositiveQuantity;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * DTO pour les mouvements de stock (sorties/entrées)
 */
public record StockMovementRequest(
        
        @NotNull(message = "La quantité est requise")
        @PositiveQuantity(message = "La quantité doit être positive")
        @Digits(integer = 7, fraction = 3, message = "Format de quantité invalide")
        BigDecimal quantite,
        
        Unite unite, // Optionnel - si différent de l'unité du produit
        
        @NotBlank(message = "Le motif est requis")
        @Size(max = 500, message = "Le motif ne peut pas dépasser 500 caractères")
        String motif,
        
        Long menuId // Optionnel - pour lier à un menu
) {}