package com.stockchef.stockchefback.dto.inventory;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO pour la mise à jour d'un produit existant
 */
public record ProduitUpdateRequest(
        
        @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
        String nom,
        
        @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à 0")
        @Digits(integer = 8, fraction = 2, message = "Format de prix invalide")
        BigDecimal prixUnitaire,
        
        @DecimalMin(value = "0.0", message = "Le seuil d'alerte doit être positif ou nul")
        @Digits(integer = 7, fraction = 3, message = "Format de seuil invalide")
        BigDecimal seuilAlerte,
        
        LocalDate datePeremption,
        
        @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
        String description
) {}