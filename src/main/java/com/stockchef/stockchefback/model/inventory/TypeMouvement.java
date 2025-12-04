package com.stockchef.stockchefback.model.inventory;

/**
 * Types de mouvements de stock pour l'audit trail
 */
public enum TypeMouvement {
    ENTREE("Entrée de stock"),
    SORTIE("Sortie de stock"),
    INVENTAIRE("Ajustement inventaire"),
    AJUSTEMENT("Correction manuelle"),
    PEREMPTION("Produit périmé");
    
    private final String description;
    
    TypeMouvement(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}