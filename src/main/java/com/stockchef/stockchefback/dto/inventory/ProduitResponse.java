package com.stockchef.stockchefback.dto.inventory;

import com.stockchef.stockchefback.model.inventory.Unite;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO pour la réponse contenant les informations d'un produit
 */
public record ProduitResponse(
        Long id,
        String nom,
        BigDecimal quantiteStock,
        Unite unite,
        BigDecimal prixUnitaire,
        BigDecimal seuilAlerte,
        LocalDate datePeremption,
        LocalDateTime dateEntree,
        LocalDateTime lastModified,
        boolean isUnderAlertThreshold,
        boolean isExpired,
        String description
) {}