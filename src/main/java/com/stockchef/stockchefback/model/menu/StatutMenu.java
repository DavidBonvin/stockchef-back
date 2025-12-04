package com.stockchef.stockchefback.model.menu;

/**
 * Énumération pour les statuts d'un menu
 */
public enum StatutMenu {
    /**
     * Menu en cours de création, peut être modifié
     */
    BROUILLON("Brouillon"),
    
    /**
     * Menu confirmé, stock décrémenté, ne peut plus être modifié
     */
    CONFIRME("Confirmé"),
    
    /**
     * Menu préparé et servi
     */
    PREPARE("Préparé"),
    
    /**
     * Menu annulé, stock restauré si nécessaire
     */
    ANNULE("Annulé");
    
    private final String libelle;
    
    StatutMenu(String libelle) {
        this.libelle = libelle;
    }
    
    public String getLibelle() {
        return libelle;
    }
    
    /**
     * Vérifie si le menu peut être modifié dans ce statut
     */
    public boolean peutEtreModifie() {
        return this == BROUILLON;
    }
    
    /**
     * Vérifie si le menu impacte le stock dans ce statut
     */
    public boolean impacteStock() {
        return this == CONFIRME || this == PREPARE;
    }
    
    @Override
    public String toString() {
        return libelle;
    }
}