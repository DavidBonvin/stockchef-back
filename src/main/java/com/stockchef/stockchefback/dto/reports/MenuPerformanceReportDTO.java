package com.stockchef.stockchefback.dto.reports;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class MenuPerformanceReportDTO {
    
    private Long menuId;
    private String menuNom;
    private Integer nombrePreparations; // Cuántas veces se ha preparado
    private BigDecimal coutMoyenIngredients;
    private BigDecimal margeMoyenne;
    private BigDecimal profitabiliteTotal; // Ganancia total generada
    private LocalDateTime dernierePreparation;
    private List<String> ingredientsPlusUtilises; // Top ingredientes
    
    // Constructors
    public MenuPerformanceReportDTO() {}
    
    public MenuPerformanceReportDTO(Long menuId, String menuNom, Integer nombrePreparations,
                                   BigDecimal coutMoyenIngredients, BigDecimal margeMoyenne,
                                   BigDecimal profitabiliteTotal) {
        this.menuId = menuId;
        this.menuNom = menuNom;
        this.nombrePreparations = nombrePreparations;
        this.coutMoyenIngredients = coutMoyenIngredients;
        this.margeMoyenne = margeMoyenne;
        this.profitabiliteTotal = profitabiliteTotal;
    }
    
    // Getters and Setters
    public Long getMenuId() {
        return menuId;
    }
    
    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }
    
    public String getMenuNom() {
        return menuNom;
    }
    
    public void setMenuNom(String menuNom) {
        this.menuNom = menuNom;
    }
    
    public Integer getNombrePreparations() {
        return nombrePreparations;
    }
    
    public void setNombrePreparations(Integer nombrePreparations) {
        this.nombrePreparations = nombrePreparations;
    }
    
    public BigDecimal getCoutMoyenIngredients() {
        return coutMoyenIngredients;
    }
    
    public void setCoutMoyenIngredients(BigDecimal coutMoyenIngredients) {
        this.coutMoyenIngredients = coutMoyenIngredients;
    }
    
    public BigDecimal getMargeMoyenne() {
        return margeMoyenne;
    }
    
    public void setMargeMoyenne(BigDecimal margeMoyenne) {
        this.margeMoyenne = margeMoyenne;
    }
    
    public BigDecimal getProfitabiliteTotal() {
        return profitabiliteTotal;
    }
    
    public void setProfitabiliteTotal(BigDecimal profitabiliteTotal) {
        this.profitabiliteTotal = profitabiliteTotal;
    }
    
    public LocalDateTime getDernierePreparation() {
        return dernierePreparation;
    }
    
    public void setDernierePreparation(LocalDateTime dernierePreparation) {
        this.dernierePreparation = dernierePreparation;
    }
    
    public List<String> getIngredientsPlusUtilises() {
        return ingredientsPlusUtilises;
    }
    
    public void setIngredientsPlusUtilises(List<String> ingredientsPlusUtilises) {
        this.ingredientsPlusUtilises = ingredientsPlusUtilises;
    }
}