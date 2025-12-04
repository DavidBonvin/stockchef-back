package com.stockchef.stockchefback.dto.inventory;

import com.stockchef.stockchefback.model.inventory.Unite;
import com.stockchef.stockchefback.validation.PositiveQuantity;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO pour la création d'un nouveau produit
 */
public record ProduitCreateRequest(
        
        @NotBlank(message = "Le nom du produit est requis")
        @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
        String nom,
        
        @NotNull(message = "La quantité initiale est requise")
        @DecimalMin(value = "0.0", message = "La quantité doit être positive ou nulle")
        @Digits(integer = 7, fraction = 3, message = "Format de quantité invalide")
        BigDecimal quantiteInitiale,
        
        @NotNull(message = "L'unité est requise")
        Unite unite,
        
        @NotNull(message = "Le prix unitaire est requis")
        @PositiveQuantity(message = "Le prix doit être positif")
        @Digits(integer = 8, fraction = 2, message = "Format de prix invalide")
        BigDecimal prixUnitaire,
        
        @NotNull(message = "Le seuil d'alerte est requis")
        @PositiveQuantity(message = "Le seuil d'alerte doit être positif")
        @Digits(integer = 7, fraction = 3, message = "Format de seuil invalide")
        BigDecimal seuilAlerte,
        
        @Future(message = "La date de péremption doit être dans le futur")
        LocalDate datePeremption,
        
        @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
        String description
) {}