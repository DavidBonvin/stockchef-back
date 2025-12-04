package com.stockchef.stockchefback.model.menu;

import com.stockchef.stockchefback.model.inventory.Produit;
import com.stockchef.stockchefback.model.inventory.Unite;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant un menu avec ses ingrédients
 * Coordiné avec le système de stock pour décrémenter automatiquement
 */
@Entity
@Table(name = "menus", 
       indexes = {
           @Index(name = "idx_menu_date_service", columnList = "dateService"),
           @Index(name = "idx_menu_statut", columnList = "statut"),
           @Index(name = "idx_menu_chef", columnList = "chefResponsable")
       })
public class Menu {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Le nom du menu ne peut pas être vide")
    @Size(max = 100, message = "Le nom du menu ne peut pas dépasser 100 caractères")
    @Column(nullable = false, length = 100)
    private String nom;
    
    @Column(length = 500)
    private String description;
    
    @NotNull(message = "La date de service est obligatoire")
    @Future(message = "La date de service doit être dans le futur")
    @Column(nullable = false)
    private LocalDate dateService;
    
    @NotNull(message = "Le nombre de portions est obligatoire")
    @Positive(message = "Le nombre de portions doit être positif")
    @Column(nullable = false)
    private Integer nombrePortions;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal prixVente;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutMenu statut = StatutMenu.BROUILLON;
    
    @NotBlank(message = "Le chef responsable est obligatoire")
    @Column(nullable = false, length = 100)
    private String chefResponsable;
    
    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MenuIngredient> ingredients = new ArrayList<>();
    
    // Métadonnées d'audit
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();
    
    @Column
    private LocalDateTime dateModification;
    
    @Column(length = 100)
    private String dernierModificateurEmail;
    
    // Métadonnées de coût (calculées)
    @Column(precision = 10, scale = 2)
    private BigDecimal coutTotalIngredients;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal margePercentage;
    
    // Constructeurs
    public Menu() {}
    
    public Menu(String nom, String description, LocalDate dateService, Integer nombrePortions) {
        this.nom = nom;
        this.description = description;
        this.dateService = dateService;
        this.nombrePortions = nombrePortions;
        this.dateCreation = LocalDateTime.now();
    }
    
    // Factory method pour création
    public static Menu creerMenu(String nom, String description, LocalDate dateService, 
                                Integer nombrePortions, String chefResponsable) {
        Menu menu = new Menu(nom, description, dateService, nombrePortions);
        menu.setChefResponsable(chefResponsable);
        return menu;
    }
    
    /**
     * Ajoute un ingrédient au menu avec la quantité nécessaire
     */
    public MenuIngredient ajouterIngredient(Produit produit, BigDecimal quantiteNecessaire, 
                                          String notes) {
        return ajouterIngredient(produit, quantiteNecessaire, produit.getUnite(), notes);
    }
    
    /**
     * Ajoute un ingrédient au menu avec la quantité nécessaire et unité spécifique
     */
    public MenuIngredient ajouterIngredient(Produit produit, BigDecimal quantiteNecessaire,
                                          Unite uniteUtilisee, String notes) {
        // Vérifier si l'ingrédient existe déjà
        MenuIngredient existant = this.ingredients.stream()
            .filter(ingredient -> ingredient.getProduit().getId().equals(produit.getId()))
            .findFirst()
            .orElse(null);
            
        if (existant != null) {
            // Mettre à jour la quantité existante
            BigDecimal nouvelleQuantite = existant.getQuantiteNecessaire().add(quantiteNecessaire);
            existant.setQuantiteNecessaire(nouvelleQuantite);
            existant.setUniteUtilisee(uniteUtilisee);
            existant.setNotes(notes);
            existant.calculerQuantiteConvertie();
            existant.calculerCoutIngredient(); // Recalcular conversión y costo
            return existant;
        } else {
            // Créer un nouveau ingrédient
            MenuIngredient nouvelIngredient = new MenuIngredient(this, produit, quantiteNecessaire, uniteUtilisee, notes);
            this.ingredients.add(nouvelIngredient);
            return nouvelIngredient;
        }
    }
    
    /**
     * Supprime un ingrédient du menu
     */
    public void supprimerIngredient(Long produitId) {
        this.ingredients.removeIf(ingredient -> 
            ingredient.getProduit().getId().equals(produitId));
    }
    
    /**
     * Calcule le coût total des ingrédients pour ce menu
     */
    public BigDecimal calculerCoutTotal() {
        BigDecimal coutTotal = this.ingredients.stream()
            .map(MenuIngredient::calculerCoutIngredient)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        this.coutTotalIngredients = coutTotal;
        return coutTotal;
    }
    
    /**
     * Vérifie si le menu peut être préparé avec le stock actuel
     */
    public boolean peutEtrePrepare() {
        return this.ingredients.stream()
            .allMatch(MenuIngredient::stockSuffisant);
    }
    
    /**
     * Retourne la liste des ingrédients manquants
     */
    public List<MenuIngredient> getIngredientsMAnquants() {
        return this.ingredients.stream()
            .filter(ingredient -> !ingredient.stockSuffisant())
            .toList();
    }
    
    /**
     * Valide si le menu peut passer au statut CONFIRME (validation de base)
     * La vérification du stock est gérée séparément pour des messages d'erreur détaillés
     */
    public boolean peutEtreConfirme() {
        return this.statut == StatutMenu.BROUILLON && 
               !this.ingredients.isEmpty();
    }
    
    /**
     * Confirme le menu et décrémente le stock
     * Cette méthode doit être appelée dans une transaction
     */
    public void confirmer() {
        if (!peutEtreConfirme()) {
            throw new IllegalStateException("Le menu ne peut pas être confirmé dans son état actuel");
        }
        this.statut = StatutMenu.CONFIRME;
        this.dateModification = LocalDateTime.now();
    }
    
    /**
     * Annule le menu confirmé
     */
    public void annuler() {
        if (this.statut == StatutMenu.PREPARE) {
            throw new IllegalStateException("Impossible d'annuler un menu déjà préparé");
        }
        this.statut = StatutMenu.ANNULE;
        this.dateModification = LocalDateTime.now();
    }
    
    /**
     * Marque le menu comme préparé
     */
    public void marquerCommePrepare() {
        if (this.statut != StatutMenu.CONFIRME) {
            throw new IllegalStateException("Seul un menu confirmé peut être marqué comme préparé");
        }
        this.statut = StatutMenu.PREPARE;
        this.dateModification = LocalDateTime.now();
    }
    
    /**
     * Calcule la marge bénéficiaire si le prix de vente est défini
     */
    public BigDecimal calculerMarge() {
        if (this.prixVente == null || this.coutTotalIngredients == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal marge = this.prixVente.subtract(this.coutTotalIngredients);
        if (this.prixVente.compareTo(BigDecimal.ZERO) > 0) {
            this.margePercentage = marge.divide(this.prixVente, 4, RoundingMode.HALF_UP)
                                      .multiply(new BigDecimal("100"));
        }
        return marge;
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { 
        this.nom = nom; 
        this.dateModification = LocalDateTime.now();
    }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { 
        this.description = description;
        this.dateModification = LocalDateTime.now();
    }
    
    public LocalDate getDateService() { return dateService; }
    public void setDateService(LocalDate dateService) { 
        this.dateService = dateService;
        this.dateModification = LocalDateTime.now();
    }
    
    public Integer getNombrePortions() { return nombrePortions; }
    public void setNombrePortions(Integer nombrePortions) { 
        this.nombrePortions = nombrePortions;
        this.dateModification = LocalDateTime.now();
    }
    
    public BigDecimal getPrixVente() { return prixVente; }
    public void setPrixVente(BigDecimal prixVente) { 
        this.prixVente = prixVente;
        this.dateModification = LocalDateTime.now();
    }
    
    public StatutMenu getStatut() { return statut; }
    public void setStatut(StatutMenu statut) { 
        this.statut = statut;
        this.dateModification = LocalDateTime.now();
    }
    
    public String getChefResponsable() { return chefResponsable; }
    public void setChefResponsable(String chefResponsable) { this.chefResponsable = chefResponsable; }
    
    public List<MenuIngredient> getIngredients() { return ingredients; }
    public void setIngredients(List<MenuIngredient> ingredients) { this.ingredients = ingredients; }
    
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    
    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }
    
    public String getDernierModificateurEmail() { return dernierModificateurEmail; }
    public void setDernierModificateurEmail(String dernierModificateurEmail) { 
        this.dernierModificateurEmail = dernierModificateurEmail; 
    }
    
    public BigDecimal getCoutTotalIngredients() { return coutTotalIngredients; }
    public void setCoutTotalIngredients(BigDecimal coutTotalIngredients) { 
        this.coutTotalIngredients = coutTotalIngredients; 
    }
    
    public BigDecimal getMargePercentage() { return margePercentage; }
    public void setMargePercentage(BigDecimal margePercentage) { this.margePercentage = margePercentage; }
    
    @Override
    public String toString() {
        return String.format("Menu{id=%d, nom='%s', dateService=%s, portions=%d, statut=%s}", 
                           id, nom, dateService, nombrePortions, statut);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Menu menu = (Menu) obj;
        return id != null && id.equals(menu.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}