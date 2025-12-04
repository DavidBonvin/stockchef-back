package com.stockchef.stockchefback.model.inventory;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entité représentant un produit en stock
 * Core entity pour la gestion des stocks
 */
@Entity
@Table(name = "produits")
@EntityListeners(AuditingEntityListener.class)
public class Produit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Le nom du produit est requis")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    @Column(nullable = false, length = 100)
    private String nom;
    
    @NotNull(message = "La quantité est requise")
    @DecimalMin(value = "0.0", message = "La quantité doit être positive ou nulle")
    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantiteStock;
    
    @NotNull(message = "L'unité est requise")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Unite unite;
    
    @NotNull(message = "Le prix unitaire est requis")
    @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prixUnitaire;
    
    @NotNull(message = "Le seuil d'alerte est requis")
    @DecimalMin(value = "0.0", message = "Le seuil d'alerte doit être positif ou nul")
    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal seuilAlerte;
    
    @Column(name = "date_peremption")
    private LocalDate datePeremption;
    
    @CreatedDate
    @Column(name = "date_entree", nullable = false)
    private LocalDateTime dateEntree;
    
    @LastModifiedDate
    @Column(name = "last_modified")
    private LocalDateTime lastModified;
    
    @Column(name = "deleted")
    private Boolean deleted = Boolean.FALSE;
    
    // Constructeurs
    public Produit() {}
    
    public Produit(String nom, BigDecimal quantiteStock, Unite unite, 
                  BigDecimal prixUnitaire, BigDecimal seuilAlerte) {
        this.nom = nom;
        this.quantiteStock = quantiteStock;
        this.unite = unite;
        this.prixUnitaire = prixUnitaire;
        this.seuilAlerte = seuilAlerte;
    }
    
    // Méthodes métier
    
    /**
     * Vérifie si le produit est en dessous du seuil d'alerte
     */
    public boolean isUnderAlertThreshold() {
        return quantiteStock.compareTo(seuilAlerte) < 0;
    }
    
    /**
     * Vérifie si le produit est périmé
     */
    public boolean isExpired() {
        return datePeremption != null && datePeremption.isBefore(LocalDate.now());
    }
    
    /**
     * Vérifie si suffisamment de stock disponible
     */
    public boolean hasSufficientStock(BigDecimal quantiteDemandee) {
        return quantiteStock.compareTo(quantiteDemandee) >= 0;
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public BigDecimal getQuantiteStock() { return quantiteStock; }
    public void setQuantiteStock(BigDecimal quantiteStock) { this.quantiteStock = quantiteStock; }
    
    public Unite getUnite() { return unite; }
    public void setUnite(Unite unite) { this.unite = unite; }
    
    public BigDecimal getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(BigDecimal prixUnitaire) { this.prixUnitaire = prixUnitaire; }
    
    public BigDecimal getSeuilAlerte() { return seuilAlerte; }
    public void setSeuilAlerte(BigDecimal seuilAlerte) { this.seuilAlerte = seuilAlerte; }
    
    public LocalDate getDatePeremption() { return datePeremption; }
    public void setDatePeremption(LocalDate datePeremption) { this.datePeremption = datePeremption; }
    
    public LocalDateTime getDateEntree() { return dateEntree; }
    public void setDateEntree(LocalDateTime dateEntree) { this.dateEntree = dateEntree; }
    
    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }
    
    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }
}