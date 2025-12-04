package com.stockchef.stockchefback.service.inventory;

import com.stockchef.stockchefback.model.inventory.Unite;
import com.stockchef.stockchefback.model.inventory.UnitType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service pour la conversion entre unités de mesure
 * Gère les conversions de poids, volume et quantités
 */
@Service
public class UniteConversionService {
    
    /**
     * Convertit une quantité d'une unité vers une autre
     * 
     * @param quantite La quantité à convertir
     * @param source L'unité source
     * @param cible L'unité cible
     * @return La quantité convertie
     * @throws IllegalArgumentException si les unités sont incompatibles ou les paramètres invalides
     */
    public BigDecimal convertir(BigDecimal quantite, Unite source, Unite cible) {
        validateParameters(quantite, source, cible);
        
        // Si les unités sont identiques, retourner la quantité telle quelle
        if (source == cible) {
            return quantite;
        }
        
        // Vérifier la compatibilité des unités
        if (!sontCompatibles(source, cible)) {
            throw new IllegalArgumentException(
                String.format("Impossible de convertir de %s vers %s - types d'unités incompatibles", 
                            source, cible)
            );
        }
        
        // Effectuer la conversion selon le type d'unité
        return switch (source.getType()) {
            case WEIGHT -> convertirPoids(quantite, source, cible);
            case VOLUME -> convertirVolume(quantite, source, cible);
            case COUNT -> quantite; // Les unités de comptage sont toujours 1:1
        };
    }
    
    /**
     * Vérifie si deux unités sont compatibles pour la conversion
     */
    public boolean sontCompatibles(Unite source, Unite cible) {
        return source.getType() == cible.getType();
    }
    
    /**
     * Convertit les unités de poids
     */
    private BigDecimal convertirPoids(BigDecimal quantite, Unite source, Unite cible) {
        // Convertir d'abord vers les grammes (unité de base)
        BigDecimal quantiteEnGrammes = switch (source) {
            case KILOGRAMME -> quantite.multiply(new BigDecimal("1000"));
            case GRAMME -> quantite;
            default -> throw new IllegalArgumentException("Unité de poids non supportée: " + source);
        };
        
        // Puis convertir vers l'unité cible
        return switch (cible) {
            case KILOGRAMME -> quantiteEnGrammes.divide(new BigDecimal("1000"), 3, RoundingMode.HALF_UP);
            case GRAMME -> quantiteEnGrammes;
            default -> throw new IllegalArgumentException("Unité de poids non supportée: " + cible);
        };
    }
    
    /**
     * Convertit les unités de volume
     */
    private BigDecimal convertirVolume(BigDecimal quantite, Unite source, Unite cible) {
        // Convertir d'abord vers les millilitres (unité de base)
        BigDecimal quantiteEnMillilitres = switch (source) {
            case LITRE -> quantite.multiply(new BigDecimal("1000"));
            case MILLILITRE -> quantite;
            default -> throw new IllegalArgumentException("Unité de volume non supportée: " + source);
        };
        
        // Puis convertir vers l'unité cible
        return switch (cible) {
            case LITRE -> quantiteEnMillilitres.divide(new BigDecimal("1000"), 3, RoundingMode.HALF_UP);
            case MILLILITRE -> quantiteEnMillilitres;
            default -> throw new IllegalArgumentException("Unité de volume non supportée: " + cible);
        };
    }
    
    /**
     * Valide les paramètres d'entrée
     */
    private void validateParameters(BigDecimal quantite, Unite source, Unite cible) {
        if (quantite == null) {
            throw new IllegalArgumentException("La quantité ne peut pas être nulle");
        }
        
        if (source == null || cible == null) {
            throw new IllegalArgumentException("Les unités ne peuvent pas être nulles");
        }
        
        if (quantite.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La quantité doit être positive ou nulle");
        }
    }
}