package com.stockchef.stockchefback.dto.reports;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InventoryReportDTO {
    
    // Resumen general de inventario
    private Integer totalProduits;
    private BigDecimal valeurTotalStock;
    private Integer produitsEnAlerte;
    private Integer produitsExpires;
    private BigDecimal valeurProduitsPerdus;
    private LocalDateTime dateGeneration;
    
    // Constructors
    public InventoryReportDTO() {}
    
    public InventoryReportDTO(Integer totalProduits, BigDecimal valeurTotalStock, 
                            Integer produitsEnAlerte, Integer produitsExpires,
                            BigDecimal valeurProduitsPerdus) {
        this.totalProduits = totalProduits;
        this.valeurTotalStock = valeurTotalStock;
        this.produitsEnAlerte = produitsEnAlerte;
        this.produitsExpires = produitsExpires;
        this.valeurProduitsPerdus = valeurProduitsPerdus;
        this.dateGeneration = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Integer getTotalProduits() {
        return totalProduits;
    }
    
    public void setTotalProduits(Integer totalProduits) {
        this.totalProduits = totalProduits;
    }
    
    public BigDecimal getValeurTotalStock() {
        return valeurTotalStock;
    }
    
    public void setValeurTotalStock(BigDecimal valeurTotalStock) {
        this.valeurTotalStock = valeurTotalStock;
    }
    
    public Integer getProduitsEnAlerte() {
        return produitsEnAlerte;
    }
    
    public void setProduitsEnAlerte(Integer produitsEnAlerte) {
        this.produitsEnAlerte = produitsEnAlerte;
    }
    
    public Integer getProduitsExpires() {
        return produitsExpires;
    }
    
    public void setProduitsExpires(Integer produitsExpires) {
        this.produitsExpires = produitsExpires;
    }
    
    public BigDecimal getValeurProduitsPerdus() {
        return valeurProduitsPerdus;
    }
    
    public void setValeurProduitsPerdus(BigDecimal valeurProduitsPerdus) {
        this.valeurProduitsPerdus = valeurProduitsPerdus;
    }
    
    public LocalDateTime getDateGeneration() {
        return dateGeneration;
    }
    
    public void setDateGeneration(LocalDateTime dateGeneration) {
        this.dateGeneration = dateGeneration;
    }
}