package com.stockchef.stockchefback.dto.reports;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class DashboardSummaryDTO {
    
    // KPIs Principales - Accessible para todos los roles
    private Integer totalProduitsActifs;
    private Integer alertesStock;
    private Integer produitsExpirantBientot; // Próximos 7 días
    private BigDecimal valeurStockTotal;
    
    // Datos del día
    private Integer menusPrepares;
    private Integer nouveauxProduits;
    private Integer mouvementsStock;
    
    // Tendencias (últimos 7 días)
    private List<String> topProduitsUtilises; // 5 más utilizados
    private List<WasteReportItemDTO> produitsGaspilles; // Desperdiciados hoy
    
    // Información financiera (solo para CHEF+)
    private BigDecimal coutIngredientsDuJour;
    private BigDecimal economiesPotentielles; // Por reducir desperdicio
    
    private LocalDate dateRapport;
    
    // Constructors
    public DashboardSummaryDTO() {
        this.dateRapport = LocalDate.now();
    }
    
    // Getters and Setters
    public Integer getTotalProduitsActifs() {
        return totalProduitsActifs;
    }
    
    public void setTotalProduitsActifs(Integer totalProduitsActifs) {
        this.totalProduitsActifs = totalProduitsActifs;
    }
    
    public Integer getAlertesStock() {
        return alertesStock;
    }
    
    public void setAlertesStock(Integer alertesStock) {
        this.alertesStock = alertesStock;
    }
    
    public Integer getProduitsExpirantBientot() {
        return produitsExpirantBientot;
    }
    
    public void setProduitsExpirantBientot(Integer produitsExpirantBientot) {
        this.produitsExpirantBientot = produitsExpirantBientot;
    }
    
    public BigDecimal getValeurStockTotal() {
        return valeurStockTotal;
    }
    
    public void setValeurStockTotal(BigDecimal valeurStockTotal) {
        this.valeurStockTotal = valeurStockTotal;
    }
    
    public Integer getMenusPrepares() {
        return menusPrepares;
    }
    
    public void setMenusPrepares(Integer menusPrepares) {
        this.menusPrepares = menusPrepares;
    }
    
    public Integer getNouveauxProduits() {
        return nouveauxProduits;
    }
    
    public void setNouveauxProduits(Integer nouveauxProduits) {
        this.nouveauxProduits = nouveauxProduits;
    }
    
    public Integer getMouvementsStock() {
        return mouvementsStock;
    }
    
    public void setMouvementsStock(Integer mouvementsStock) {
        this.mouvementsStock = mouvementsStock;
    }
    
    public List<String> getTopProduitsUtilises() {
        return topProduitsUtilises;
    }
    
    public void setTopProduitsUtilises(List<String> topProduitsUtilises) {
        this.topProduitsUtilises = topProduitsUtilises;
    }
    
    public List<WasteReportItemDTO> getProduitsGaspilles() {
        return produitsGaspilles;
    }
    
    public void setProduitsGaspilles(List<WasteReportItemDTO> produitsGaspilles) {
        this.produitsGaspilles = produitsGaspilles;
    }
    
    public BigDecimal getCoutIngredientsDuJour() {
        return coutIngredientsDuJour;
    }
    
    public void setCoutIngredientsDuJour(BigDecimal coutIngredientsDuJour) {
        this.coutIngredientsDuJour = coutIngredientsDuJour;
    }
    
    public BigDecimal getEconomiesPotentielles() {
        return economiesPotentielles;
    }
    
    public void setEconomiesPotentielles(BigDecimal economiesPotentielles) {
        this.economiesPotentielles = economiesPotentielles;
    }
    
    public LocalDate getDateRapport() {
        return dateRapport;
    }
    
    public void setDateRapport(LocalDate dateRapport) {
        this.dateRapport = dateRapport;
    }
}