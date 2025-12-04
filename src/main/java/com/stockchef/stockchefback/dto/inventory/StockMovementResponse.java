package com.stockchef.stockchefback.dto.inventory;

import com.stockchef.stockchefback.model.inventory.TypeMouvement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO pour la réponse des mouvements de stock
 */
public record StockMovementResponse(
        Long id,
        String produitNom,
        TypeMouvement typeMouvement,
        BigDecimal quantite,
        BigDecimal quantiteApres,
        String motif,
        Long menuId,
        LocalDateTime dateMouvement,
        String utilisateur
) {}