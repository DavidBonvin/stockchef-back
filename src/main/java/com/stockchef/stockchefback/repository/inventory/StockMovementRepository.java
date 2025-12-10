package com.stockchef.stockchefback.repository.inventory;

import com.stockchef.stockchefback.model.inventory.Produit;
import com.stockchef.stockchefback.model.inventory.StockMovement;
import com.stockchef.stockchefback.model.inventory.TypeMouvement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository pour la gestion des mouvements de stock
 * Audit trail et historique des transactions
 */
@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    
    /**
     * Trouve tous les mouvements d'un produit, triés par date décroissante
     */
    List<StockMovement> findByProduitOrderByDateMouvementDesc(Produit produit);
    
    /**
     * Trouve les mouvements d'un produit par type
     */
    List<StockMovement> findByProduitAndTypeMouvement(Produit produit, TypeMouvement typeMouvement);
    
    /**
     * Trouve les mouvements liés à un menu spécifique
     */
    List<StockMovement> findByMenuId(Long menuId);
    
    /**
     * Trouve les mouvements entre deux dates
     */
    List<StockMovement> findByDateMouvementBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Calcule le total des quantités par produit et type de mouvement
     */
    @Query("SELECT COALESCE(SUM(sm.quantite), 0) FROM StockMovement sm " +
           "WHERE sm.produit.id = :produitId AND sm.typeMouvement = :typeMouvement")
    BigDecimal getTotalQuantiteByProduitAndType(@Param("produitId") Long produitId, 
                                               @Param("typeMouvement") TypeMouvement typeMouvement);
    
    /**
     * Trouve les derniers mouvements pour un produit (limite)
     */
    @Query("SELECT sm FROM StockMovement sm WHERE sm.produit = :produit " +
           "ORDER BY sm.dateMouvement DESC")
    List<StockMovement> findRecentMovementsByProduit(@Param("produit") Produit produit);
    
    /**
     * Trouve les mouvements par utilisateur
     */
    List<StockMovement> findByUtilisateurOrderByDateMouvementDesc(String utilisateur);
    
    /**
     * Trouve les mouvements d'un produit pour une période donnée
     */
    @Query("SELECT sm FROM StockMovement sm WHERE sm.produit.id = :produitId " +
           "AND sm.dateMouvement BETWEEN :startDate AND :endDate " +
           "ORDER BY sm.dateMouvement ASC")
    List<StockMovement> findByProduitAndDateRange(@Param("produitId") Long produitId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);
    
    /**
     * Encuentra movimientos por tipo y rango de fechas
     */
    @Query("SELECT sm FROM StockMovement sm WHERE sm.typeMouvement = :typeMouvement " +
           "AND sm.dateMouvement BETWEEN :startDate AND :endDate " +
           "ORDER BY sm.dateMouvement DESC")
    List<StockMovement> findByDateMouvementBetweenAndTypeMouvement(@Param("startDate") LocalDateTime startDate,
                                                                 @Param("endDate") LocalDateTime endDate,
                                                                 @Param("typeMouvement") TypeMouvement typeMouvement);
    
    /**
     * Cuenta movimientos después de una fecha específica
     */
    @Query("SELECT COUNT(sm) FROM StockMovement sm WHERE sm.dateMouvement >= :startDate")
    Integer countByDateMouvementAfter(@Param("startDate") LocalDateTime startDate);
}