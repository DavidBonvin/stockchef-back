package com.stockchef.stockchefback.dto.reports;

import java.math.BigDecimal;

public class WasteReportItemDTO {
    
    private Long produitId;
    private String produitNom;
    private BigDecimal quantitePerdUE;
    private String unite;
    private BigDecimal valeurPerdue;
    private String motif; // "Expiré", "Gâté", "Erreur préparation", etc.
    
    // Constructors
    public WasteReportItemDTO() {}
    
    public WasteReportItemDTO(Long produitId, String produitNom, BigDecimal quantitePerdue, 
                             String unite, BigDecimal valeurPerdue, String motif) {
        this.produitId = produitId;
        this.produitNom = produitNom;
        this.quantitePerdUE = quantitePerdue;
        this.unite = unite;
        this.valeurPerdue = valeurPerdue;
        this.motif = motif;
    }
    
    // Getters and Setters
    public Long getProduitId() {
        return produitId;
    }
    
    public void setProduitId(Long produitId) {
        this.produitId = produitId;
    }
    
    public String getProduitNom() {
        return produitNom;
    }
    
    public void setProduitNom(String produitNom) {
        this.produitNom = produitNom;
    }
    
    public BigDecimal getQuantitePerdUE() {
        return quantitePerdUE;
    }
    
    public void setQuantitePerdUE(BigDecimal quantitePerdue) {
        this.quantitePerdUE = quantitePerdue;
    }
    
    public String getUnite() {
        return unite;
    }
    
    public void setUnite(String unite) {
        this.unite = unite;
    }
    
    public BigDecimal getValeurPerdue() {
        return valeurPerdue;
    }
    
    public void setValeurPerdue(BigDecimal valeurPerdue) {
        this.valeurPerdue = valeurPerdue;
    }
    
    public String getMotif() {
        return motif;
    }
    
    public void setMotif(String motif) {
        this.motif = motif;
    }
}