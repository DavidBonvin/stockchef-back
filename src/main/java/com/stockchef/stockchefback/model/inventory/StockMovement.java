package com.stockchef.stockchefback.model.inventory;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité pour l'audit trail des mouvements de stock
 * Obligatoire pour la traçabilité et la conformité
 */
@Entity
@Table(name = "stock_movements")
@EntityListeners(AuditingEntityListener.class)
public class StockMovement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;
    
    @NotNull(message = "Le type de mouvement est requis")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeMouvement typeMouvement;
    
    @NotNull(message = "La quantité est requise")
    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantite; // Positif pour ENTREE, négatif pour SORTIE
    
    @NotNull(message = "L'unité est requise")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Unite unite; // Unité utilisée pour ce mouvement
    
    @NotNull(message = "La quantité après mouvement est requise")
    @Column(nullable = false, precision = 10, scale = 3, name = "quantite_apres")
    private BigDecimal quantiteApres; // Stock après le mouvement
    
    @Column(length = 500)
    private String motif; // Raison du mouvement
    
    @Column(name = "menu_id") // Optionnel - pour lier à un menu
    private Long menuId;
    
    @CreatedDate
    @Column(name = "date_mouvement", nullable = false)
    private LocalDateTime dateMouvement;
    
    @CreatedBy
    @Column(name = "utilisateur", length = 100)
    private String utilisateur; // Qui a fait le mouvement
    
    // Constructeurs
    public StockMovement() {}
    
    public StockMovement(Produit produit, TypeMouvement typeMouvement, 
                        BigDecimal quantite, Unite unite, BigDecimal quantiteApres, String motif) {
        this.produit = produit;
        this.typeMouvement = typeMouvement;
        this.quantite = quantite;
        this.unite = unite;
        this.quantiteApres = quantiteApres;
        this.motif = motif;
    }
    
    /**
     * Factory method pour créer un mouvement de sortie
     */
    public static StockMovement createSortie(Produit produit, BigDecimal quantiteSortie, Unite unite,
                                           BigDecimal nouveauStock, String motif, Long menuId) {
        StockMovement movement = new StockMovement(
            produit, 
            TypeMouvement.SORTIE, 
            quantiteSortie.negate(), // Négatif pour sortie
            unite,
            nouveauStock, 
            motif
        );
        movement.setDateMouvement(LocalDateTime.now());
        movement.setMenuId(menuId);
        return movement;
    }
    
    /**
     * Factory method pour créer un mouvement d'entrée
     */
    public static StockMovement createEntree(Produit produit, BigDecimal quantiteEntree, Unite unite,
                                           BigDecimal nouveauStock, String motif) {
        StockMovement movement = new StockMovement(
            produit, 
            TypeMouvement.ENTREE, 
            quantiteEntree, // Positif pour entrée
            unite,
            nouveauStock, 
            motif
        );
        movement.setDateMouvement(LocalDateTime.now());
        return movement;
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Produit getProduit() { return produit; }
    public void setProduit(Produit produit) { this.produit = produit; }
    
    public TypeMouvement getTypeMouvement() { return typeMouvement; }
    public void setTypeMouvement(TypeMouvement typeMouvement) { this.typeMouvement = typeMouvement; }
    
    public BigDecimal getQuantite() { return quantite; }
    public void setQuantite(BigDecimal quantite) { this.quantite = quantite; }
    
    public Unite getUnite() { return unite; }
    public void setUnite(Unite unite) { this.unite = unite; }
    
    public BigDecimal getQuantiteApres() { return quantiteApres; }
    public void setQuantiteApres(BigDecimal quantiteApres) { this.quantiteApres = quantiteApres; }
    
    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
    
    public Long getMenuId() { return menuId; }
    public void setMenuId(Long menuId) { this.menuId = menuId; }
    
    public LocalDateTime getDateMouvement() { return dateMouvement; }
    public void setDateMouvement(LocalDateTime dateMouvement) { this.dateMouvement = dateMouvement; }
    
    public String getUtilisateur() { return utilisateur; }
    public void setUtilisateur(String utilisateur) { this.utilisateur = utilisateur; }
}