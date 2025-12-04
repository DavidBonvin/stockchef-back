package com.stockchef.stockchefback.model.inventory;

/**
 * Enumération des unités de mesure supportées dans le système
 * Utilisé pour les produits et les conversions
 */
public enum Unite {
    // Unités de poids
    KILOGRAMME("kg", UnitType.WEIGHT),
    GRAMME("g", UnitType.WEIGHT),
    
    // Unités de volume
    LITRE("L", UnitType.VOLUME),
    MILLILITRE("ml", UnitType.VOLUME),
    
    // Unités de comptage
    UNITE("unité", UnitType.COUNT),
    PIECE("pièce", UnitType.COUNT);
    
    private final String symbol;
    private final UnitType type;
    
    Unite(String symbol, UnitType type) {
        this.symbol = symbol;
        this.type = type;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public UnitType getType() {
        return type;
    }
    
    /**
     * Vérifie si cette unité est compatible avec une autre pour conversion
     */
    public boolean isCompatibleWith(Unite other) {
        return this.type == other.type;
    }
}