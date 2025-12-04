package com.stockchef.stockchefback.model.inventory;

/**
 * Types d'unités pour regrouper les unités compatibles
 */
public enum UnitType {
    WEIGHT("Poids"),
    VOLUME("Volume"), 
    COUNT("Comptage");
    
    private final String description;
    
    UnitType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}