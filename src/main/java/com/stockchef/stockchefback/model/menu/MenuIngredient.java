package com.stockchef.stockchefback.model.menu;

import com.stockchef.stockchefback.model.inventory.Produit;
import com.stockchef.stockchefback.model.inventory.Unite;
import com.stockchef.stockchefback.service.inventory.UniteConversionService;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Entité représentant un ingrédient dans un menu avec sa quantité nécessaire
 * Lien entre Menu et Produit avec logique métier de calcul de coût
 */
@Entity
@Table(name = "menu_ingredients",
       indexes = {
           @Index(name = "idx_menu_ingredient_menu", columnList = "menu_id"),
           @Index(name = "idx_menu_ingredient_produit", columnList = "produit_id")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_menu_produit", columnNames = {"menu_id", "produit_id"})
       })
public class MenuIngredient {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_id", nullable = false, foreignKey = @ForeignKey(name = "fk_menu_ingredient_menu"))
    private Menu menu;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produit_id", nullable = false, foreignKey = @ForeignKey(name = "fk_menu_ingredient_produit"))
    private Produit produit;
    
    @NotNull(message = "La quantité nécessaire est obligatoire")
    @Positive(message = "La quantité nécessaire doit être positive")
    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantiteNecessaire;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Unite uniteUtilisee;
    
    @Column(length = 255)
    private String notes;
    
    // Métadonnées calculées pour performance
    @Column(precision = 10, scale = 2)
    private BigDecimal coutIngredient;
    
    @Column(precision = 10, scale = 3)
    private BigDecimal quantiteConvertieStockUnit;
    
    // Constructeurs
    public MenuIngredient() {}
    
    public MenuIngredient(Menu menu, Produit produit, BigDecimal quantiteNecessaire, String notes) {
        this.menu = menu;
        this.produit = produit;
        this.quantiteNecessaire = quantiteNecessaire;
        this.uniteUtilisee = produit.getUnite(); // Par défaut, même unité que le produit
        this.notes = notes;
        this.calculerQuantiteConvertie();
        this.calculerCoutIngredient();
    }
    
    public MenuIngredient(Menu menu, Produit produit, BigDecimal quantiteNecessaire, 
                         Unite uniteUtilisee, String notes) {
        this.menu = menu;
        this.produit = produit;
        this.quantiteNecessaire = quantiteNecessaire;
        this.uniteUtilisee = uniteUtilisee;
        this.notes = notes;
        this.calculerQuantiteConvertie();
        this.calculerCoutIngredient();
    }
    
    /**
     * Calcule le coût de cet ingrédient basé sur la quantité nécessaire et le prix unitaire
     */
    public BigDecimal calculerCoutIngredient() {
        if (this.produit == null || this.quantiteNecessaire == null) {
            return BigDecimal.ZERO;
        }
        
        // Convertir la quantité dans l'unité du produit si nécessaire
        BigDecimal quantiteEnUniteStock = this.quantiteConvertieStockUnit != null 
            ? this.quantiteConvertieStockUnit 
            : this.quantiteNecessaire;
        
        // Calculer le coût: quantité * prix unitaire
        BigDecimal cout = quantiteEnUniteStock.multiply(this.produit.getPrixUnitaire());
        this.coutIngredient = cout.setScale(2, RoundingMode.HALF_UP);
        return this.coutIngredient;
    }
    
    /**
     * Calcule la quantité convertie dans l'unité de stock du produit
     * Cette méthode nécessite l'injection du UniteConversionService
     */
    public void calculerQuantiteConvertie() {
        if (this.uniteUtilisee.equals(this.produit.getUnite())) {
            // Même unité, pas de conversion nécessaire
            this.quantiteConvertieStockUnit = this.quantiteNecessaire;
        } else {
            // Note: La conversion sera faite par le service MenuService
            // qui a accès au UniteConversionService
            this.quantiteConvertieStockUnit = this.quantiteNecessaire;
        }
    }
    
    /**
     * Définit la quantité convertie après calcul par le service
     */
    public void setQuantiteConvertieCalculee(BigDecimal quantiteConvertie) {
        this.quantiteConvertieStockUnit = quantiteConvertie;
        this.calculerCoutIngredient(); // Recalculer le coût avec la nouvelle quantité
    }
    
    /**
     * Vérifie si le stock du produit est suffisant pour la quantité nécessaire
     */
    public boolean stockSuffisant() {
        if (this.produit == null || this.quantiteConvertieStockUnit == null) {
            return false;
        }
        
        return this.produit.hasSufficientStock(this.quantiteConvertieStockUnit);
    }
    
    /**
     * Calcule la quantité manquante si le stock est insuffisant
     */
    public BigDecimal getQuantiteManquante() {
        if (stockSuffisant()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal stockDisponible = this.produit.getQuantiteStock();
        BigDecimal quantiteRequise = this.quantiteConvertieStockUnit;
        
        return quantiteRequise.subtract(stockDisponible);
    }
    
    /**
     * Vérifie si cet ingrédient nécessite une conversion d'unité
     */
    public boolean necessiteConversion() {
        return !this.uniteUtilisee.equals(this.produit.getUnite());
    }
    
    /**
     * Retourne la quantité dans l'unité de stock pour la décrémentation
     */
    public BigDecimal getQuantitePourStock() {
        return this.quantiteConvertieStockUnit != null 
            ? this.quantiteConvertieStockUnit 
            : this.quantiteNecessaire;
    }
    
    /**
     * Génère une description lisible de cet ingrédient
     */
    public String getDescriptionIngredient() {
        return String.format("%s %s de %s", 
                           this.quantiteNecessaire, 
                           this.uniteUtilisee.getSymbol(),
                           this.produit.getNom());
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Menu getMenu() { return menu; }
    public void setMenu(Menu menu) { this.menu = menu; }
    
    public Produit getProduit() { return produit; }
    public void setProduit(Produit produit) { 
        this.produit = produit;
        this.calculerQuantiteConvertie();
        this.calculerCoutIngredient();
    }
    
    public BigDecimal getQuantiteNecessaire() { return quantiteNecessaire; }
    public void setQuantiteNecessaire(BigDecimal quantiteNecessaire) { 
        this.quantiteNecessaire = quantiteNecessaire;
        this.calculerQuantiteConvertie();
        this.calculerCoutIngredient();
    }
    
    public Unite getUniteUtilisee() { return uniteUtilisee; }
    public void setUniteUtilisee(Unite uniteUtilisee) { 
        this.uniteUtilisee = uniteUtilisee;
        this.calculerQuantiteConvertie();
        this.calculerCoutIngredient();
    }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public BigDecimal getCoutIngredient() { return coutIngredient; }
    public void setCoutIngredient(BigDecimal coutIngredient) { this.coutIngredient = coutIngredient; }
    
    public BigDecimal getQuantiteConvertieStockUnit() { return quantiteConvertieStockUnit; }
    public void setQuantiteConvertieStockUnit(BigDecimal quantiteConvertieStockUnit) { 
        this.quantiteConvertieStockUnit = quantiteConvertieStockUnit; 
    }
    
    @Override
    public String toString() {
        return String.format("MenuIngredient{produit='%s', quantite=%s %s, cout=%s}", 
                           produit != null ? produit.getNom() : "null", 
                           quantiteNecessaire, 
                           uniteUtilisee != null ? uniteUtilisee.getSymbol() : "null",
                           coutIngredient);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MenuIngredient that = (MenuIngredient) obj;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}