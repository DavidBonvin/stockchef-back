package com.stockchef.stockchefback.repository.inventory;

import com.stockchef.stockchefback.model.inventory.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository pour la gestion des produits
 * Méthodes pour les opérations CRUD et requêtes métier
 */
@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {
    
    /**
     * Trouve tous les produits non supprimés
     */
    @Query("SELECT p FROM Produit p WHERE p.deleted = false OR p.deleted IS NULL")
    List<Produit> findAll();
    
    /**
     * Trouve les produits par nom (recherche insensible à la casse)
     */
    @Query("SELECT p FROM Produit p WHERE p.deleted = false AND LOWER(p.nom) LIKE LOWER(CONCAT('%', :nom, '%'))")
    List<Produit> findByNomContainingIgnoreCase(@Param("nom") String nom);
    
    /**
     * Trouve les produits en dessous du seuil d'alerte
     */
    @Query("SELECT p FROM Produit p WHERE p.deleted = false AND p.quantiteStock < p.seuilAlerte")
    List<Produit> findProduitsUnderAlertThreshold();
    
    /**
     * Trouve les produits qui expirent dans X jours ou sont déjà expirés
     */
    @Query("SELECT p FROM Produit p WHERE p.deleted = false AND p.datePeremption IS NOT NULL " +
           "AND p.datePeremption <= :dateLimit")
    List<Produit> findProduitsExpiringWithinDays(@Param("dateLimit") LocalDate dateLimit);
    
    /**
     * Trouve les produits qui expirent dans X jours
     */
    default List<Produit> findProduitsExpiringWithinDays(int days) {
        LocalDate dateLimit = LocalDate.now().plusDays(days);
        return findProduitsExpiringWithinDays(dateLimit);
    }
    
    /**
     * Met à jour la quantité de stock d'un produit
     */
    @Modifying
    @Query("UPDATE Produit p SET p.quantiteStock = :nouvelleQuantite WHERE p.id = :produitId")
    int updateQuantiteStock(@Param("produitId") Long produitId, @Param("nouvelleQuantite") BigDecimal nouvelleQuantite);
    
    /**
     * Trouve les produits par unité
     */
    @Query("SELECT p FROM Produit p WHERE p.deleted = false AND p.unite = :unite")
    List<Produit> findByUnite(@Param("unite") String unite);
    
    /**
     * Compte les produits actifs (non supprimés)
     */
    @Query("SELECT COUNT(p) FROM Produit p WHERE p.deleted = false OR p.deleted IS NULL")
    Integer countByDeletedFalse();
    
    /**
     * Compte les produits en alerte de stock
     */
    @Query("SELECT COUNT(p) FROM Produit p WHERE p.deleted = false AND p.quantiteStock < p.seuilAlerte")
    Integer countProduitsWithLowStock();
    
    /**
     * Compte les produits expirant dans X jours
     */
    @Query("SELECT COUNT(p) FROM Produit p WHERE p.deleted = false AND p.datePeremption IS NOT NULL " +
           "AND p.datePeremption <= :dateLimit")
    Integer countProduitsExpiringInDays(@Param("dateLimit") LocalDate dateLimit);
    
    /**
     * Métodos sobrecargados para facilitar el uso
     */
    default Integer countProduitsExpiringInDays(int days) {
        LocalDate dateLimit = LocalDate.now().plusDays(days);
        return countProduitsExpiringInDays(dateLimit);
    }
    
    /**
     * Encuentra productos que han expirado en un período específico
     */
    @Query("SELECT p FROM Produit p WHERE p.deleted = false AND p.datePeremption IS NOT NULL " +
           "AND p.datePeremption >= :startDate AND p.datePeremption <= :endDate")
    List<Produit> findExpiredProductsInPeriod(@Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);
    
    /**
     * Encuentra productos en alerte de stock
     */
    @Query("SELECT p FROM Produit p WHERE p.deleted = false AND p.quantiteStock < p.seuilAlerte")
    List<Produit> findProduitsWithLowStock();
    
    /**
     * Cuenta productos creados después de una fecha
     */
    @Query("SELECT COUNT(p) FROM Produit p WHERE p.deleted = false AND p.dateEntree >= :startDate")
    Integer countByDateEntreeAfter(@Param("startDate") LocalDateTime startDate);
}