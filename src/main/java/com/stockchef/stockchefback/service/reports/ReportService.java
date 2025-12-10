package com.stockchef.stockchefback.service.reports;

import com.stockchef.stockchefback.dto.reports.DashboardSummaryDTO;
import com.stockchef.stockchefback.dto.reports.InventoryReportDTO;
import com.stockchef.stockchefback.dto.reports.MenuPerformanceReportDTO;
import com.stockchef.stockchefback.dto.reports.WasteReportItemDTO;
import com.stockchef.stockchefback.model.inventory.Produit;
import com.stockchef.stockchefback.model.inventory.StockMovement;
import com.stockchef.stockchefback.model.inventory.TypeMouvement;
import com.stockchef.stockchefback.model.menu.Menu;
import com.stockchef.stockchefback.model.menu.StatutMenu;
import com.stockchef.stockchefback.repository.inventory.ProduitRepository;
import com.stockchef.stockchefback.repository.inventory.StockMovementRepository;
import com.stockchef.stockchefback.repository.menu.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private ProduitRepository produitRepository;
    
    @Autowired
    private StockMovementRepository stockMovementRepository;
    
    @Autowired
    private MenuRepository menuRepository;

    /**
     * Genera el resumen del dashboard principal
     */
    public DashboardSummaryDTO generateDashboardSummary() {
        DashboardSummaryDTO dashboard = new DashboardSummaryDTO();
        
        // KPIs básicos
        dashboard.setTotalProduitsActifs(produitRepository.countByDeletedFalse());
        dashboard.setAlertesStock(produitRepository.countProduitsWithLowStock());
        dashboard.setProduitsExpirantBientot(produitRepository.countProduitsExpiringInDays(7));
        dashboard.setValeurStockTotal(calculateTotalStockValue());
        
        // Actividades del día
        LocalDate today = LocalDate.now();
        dashboard.setMenusPrepares(menuRepository.countByStatutAndDateServiceAfter(
            StatutMenu.CONFIRME, today.atStartOfDay()));
        dashboard.setNouveauxProduits(produitRepository.countByDateEntreeAfter(today.atStartOfDay()));
        dashboard.setMouvementsStock(stockMovementRepository.countByDateMouvementAfter(today.atStartOfDay()));
        
        // Top productos utilizados (últimos 7 días)
        dashboard.setTopProduitsUtilises(getTopUsedProducts(today.minusDays(7), today, 5));
        
        // Productos desperdiciados hoy
        dashboard.setProduitsGaspilles(getWasteReport(today, today));
        
        // Información financiera (se puede ocultar según el rol en el frontend)
        dashboard.setCoutIngredientsDuJour(calculateDailyCosts(today));
        dashboard.setEconomiesPotentielles(calculatePotentialSavings());
        
        return dashboard;
    }

    /**
     * Genera reporte general de inventario
     */
    public InventoryReportDTO generateInventoryReport() {
        List<Produit> produits = produitRepository.findAll();
        
        int totalProduits = produits.size();
        BigDecimal valeurTotal = calculateTotalStockValue();
        int produitsAlerte = produitRepository.countProduitsWithLowStock();
        int produitsExpires = produitRepository.countProduitsExpiringInDays(0);
        BigDecimal valeurPerdue = calculateWastedValue(LocalDate.now().minusDays(30), LocalDate.now());
        
        return new InventoryReportDTO(totalProduits, valeurTotal, produitsAlerte, 
                                    produitsExpires, valeurPerdue);
    }

    /**
     * Obtiene reporte de desperdicio para un período
     */
    public List<WasteReportItemDTO> getWasteReport(LocalDate startDate, LocalDate endDate) {
        List<WasteReportItemDTO> wasteItems = new ArrayList<>();
        
        // Productos expirados en el período
        List<Produit> expiredProducts = produitRepository.findExpiredProductsInPeriod(
            startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        
        for (Produit produit : expiredProducts) {
            if (produit.getQuantiteStock().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal valeurPerdue = produit.getQuantiteStock().multiply(produit.getPrixUnitaire());
                
                WasteReportItemDTO wasteItem = new WasteReportItemDTO(
                    produit.getId(),
                    produit.getNom(),
                    produit.getQuantiteStock(),
                    produit.getUnite().toString(),
                    valeurPerdue,
                    "Producto expirado"
                );
                wasteItems.add(wasteItem);
            }
        }
        
        // Aquí se podrían agregar otros tipos de desperdicio
        // (movimientos de stock con motivos como "dañado", "error", etc.)
        
        return wasteItems;
    }

    /**
     * Obtiene el reporte de performance de menús
     */
    public List<MenuPerformanceReportDTO> getMenuPerformanceReport(LocalDate startDate, LocalDate endDate) {
        List<Menu> menus = menuRepository.findByDateServiceBetweenAndStatutIn(
            startDate.atStartOfDay(), 
            endDate.atTime(23, 59, 59),
            List.of(StatutMenu.CONFIRME, StatutMenu.PREPARE)
        );
        
        return menus.stream()
            .collect(Collectors.groupingBy(Menu::getNom))
            .entrySet().stream()
            .map(entry -> {
                String menuNom = entry.getKey();
                List<Menu> menuInstances = entry.getValue();
                
                // Calcular estadísticas del menú
                int nombrePreparations = menuInstances.size();
                BigDecimal coutMoyen = menuInstances.stream()
                    .map(Menu::getCoutTotalIngredients)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(nombrePreparations), 2, BigDecimal.ROUND_HALF_UP);
                
                BigDecimal margeMoyenne = menuInstances.stream()
                    .map(m -> m.getPrixVente().subtract(m.getCoutTotalIngredients()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(nombrePreparations), 2, BigDecimal.ROUND_HALF_UP);
                
                BigDecimal profitabiliteTotal = menuInstances.stream()
                    .map(m -> m.getPrixVente().subtract(m.getCoutTotalIngredients()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                Menu firstMenu = menuInstances.get(0);
                MenuPerformanceReportDTO report = new MenuPerformanceReportDTO(
                    firstMenu.getId(), menuNom, nombrePreparations, 
                    coutMoyen, margeMoyenne, profitabiliteTotal);
                
                report.setDernierePreparation(menuInstances.stream()
                    .map(Menu::getDateCreation)
                    .max(LocalDateTime::compareTo)
                    .orElse(null));
                
                return report;
            })
            .collect(Collectors.toList());
    }

    /**
     * Obtiene los productos más utilizados
     */
    public List<String> getTopUsedProducts(LocalDate startDate, LocalDate endDate, int limit) {
        List<StockMovement> movements = stockMovementRepository.findByDateMouvementBetweenAndTypeMouvement(
            startDate.atStartOfDay(), 
            endDate.atTime(23, 59, 59),
            TypeMouvement.SORTIE
        );
        
        return movements.stream()
            .collect(Collectors.groupingBy(
                m -> m.getProduit().getNom(),
                Collectors.summingDouble(m -> m.getQuantite().doubleValue())
            ))
            .entrySet().stream()
            .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
            .limit(limit)
            .map(e -> e.getKey() + " (" + String.format("%.2f", e.getValue()) + " unités)")
            .collect(Collectors.toList());
    }

    /**
     * Obtiene reporte de alertas actuales
     */
    public InventoryReportDTO getCurrentAlertsReport() {
        int alertesStock = produitRepository.countProduitsWithLowStock();
        int produitsExpirant = produitRepository.countProduitsExpiringInDays(3);
        BigDecimal valeurAlertes = calculateAlertStockValue();
        
        return new InventoryReportDTO(0, BigDecimal.ZERO, alertesStock, produitsExpirant, valeurAlertes);
    }

    // Métodos auxiliares privados
    
    private BigDecimal calculateTotalStockValue() {
        List<Produit> produits = produitRepository.findAll();
        return produits.stream()
            .map(p -> p.getQuantiteStock().multiply(p.getPrixUnitaire()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private BigDecimal calculateDailyCosts(LocalDate date) {
        List<StockMovement> movements = stockMovementRepository.findByDateMouvementBetweenAndTypeMouvement(
            date.atStartOfDay(), 
            date.atTime(23, 59, 59),
            TypeMouvement.SORTIE
        );
        
        return movements.stream()
            .map(m -> m.getQuantite().multiply(m.getProduit().getPrixUnitaire()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private BigDecimal calculatePotentialSavings() {
        // Estimación simple: 10% de los productos en alerta representan ahorros potenciales
        BigDecimal alertValue = calculateAlertStockValue();
        return alertValue.multiply(BigDecimal.valueOf(0.10));
    }
    
    private BigDecimal calculateWastedValue(LocalDate startDate, LocalDate endDate) {
        List<WasteReportItemDTO> wasteItems = getWasteReport(startDate, endDate);
        return wasteItems.stream()
            .map(WasteReportItemDTO::getValeurPerdue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private BigDecimal calculateAlertStockValue() {
        List<Produit> alertProducts = produitRepository.findProduitsWithLowStock();
        return alertProducts.stream()
            .map(p -> p.getQuantiteStock().multiply(p.getPrixUnitaire()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}