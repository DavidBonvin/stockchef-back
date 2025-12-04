package com.stockchef.stockchefback.repository.menu;

import com.stockchef.stockchefback.model.menu.Menu;
import com.stockchef.stockchefback.model.menu.MenuIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des ingrédients de menu
 */
@Repository
public interface MenuIngredientRepository extends JpaRepository<MenuIngredient, Long> {
    
    /**
     * Trouve tous les ingrédients d'un menu
     */
    List<MenuIngredient> findByMenuOrderByProduitNom(Menu menu);
    
    /**
     * Trouve tous les ingrédients d'un menu par ID
     */
    @Query("SELECT mi FROM MenuIngredient mi LEFT JOIN FETCH mi.produit WHERE mi.menu.id = :menuId ORDER BY mi.produit.nom")
    List<MenuIngredient> findByMenuIdWithProduit(@Param("menuId") Long menuId);
    
    /**
     * Trouve un ingrédient spécifique dans un menu
     */
    Optional<MenuIngredient> findByMenuAndProduitId(Menu menu, Long produitId);
    
    /**
     * Trouve tous les menus qui utilisent un produit donné
     */
    @Query("SELECT mi FROM MenuIngredient mi LEFT JOIN FETCH mi.menu WHERE mi.produit.id = :produitId")
    List<MenuIngredient> findByProduitId(@Param("produitId") Long produitId);
    
    /**
     * Calcule la quantité totale d'un produit utilisée dans tous les menus confirmés
     */
    @Query("""
        SELECT COALESCE(SUM(mi.quantiteConvertieStockUnit), 0) 
        FROM MenuIngredient mi 
        WHERE mi.produit.id = :produitId 
        AND mi.menu.statut IN ('CONFIRME', 'PREPARE')
        """)
    BigDecimal calculateTotalQuantiteUtiliseeConfirmee(@Param("produitId") Long produitId);
    
    /**
     * Trouve les ingrédients avec stock insuffisant pour un menu
     */
    @Query("""
        SELECT mi FROM MenuIngredient mi 
        LEFT JOIN FETCH mi.produit p
        WHERE mi.menu.id = :menuId 
        AND mi.quantiteConvertieStockUnit > p.quantiteStock
        """)
    List<MenuIngredient> findIngredientsAvecStockInsuffisant(@Param("menuId") Long menuId);
    
    /**
     * Supprime tous les ingrédients d'un menu
     */
    void deleteByMenuId(Long menuId);
    
    /**
     * Compte le nombre d'ingrédients par menu
     */
    @Query("SELECT mi.menu.id, COUNT(mi) FROM MenuIngredient mi GROUP BY mi.menu.id")
    List<Object[]> countIngredientsByMenu();
    
    /**
     * Trouve les ingrédients les plus utilisés
     */
    @Query("""
        SELECT mi.produit.id, 
               mi.produit.nom, 
               COUNT(mi) as nombreUtilisations,
               SUM(mi.quantiteConvertieStockUnit) as quantiteTotale
        FROM MenuIngredient mi 
        GROUP BY mi.produit.id, mi.produit.nom 
        ORDER BY nombreUtilisations DESC
        """)
    List<Object[]> findIngredientsLesPlusUtilises();
    
    /**
     * Trouve les ingrédients d'un menu avec leur coût calculé
     */
    @Query("""
        SELECT mi FROM MenuIngredient mi 
        LEFT JOIN FETCH mi.produit 
        WHERE mi.menu.id = :menuId 
        ORDER BY mi.coutIngredient DESC
        """)
    List<MenuIngredient> findByMenuIdOrderByCout(@Param("menuId") Long menuId);
    
    /**
     * Calcule le coût total des ingrédients pour un menu
     */
    @Query("SELECT COALESCE(SUM(mi.coutIngredient), 0) FROM MenuIngredient mi WHERE mi.menu.id = :menuId")
    BigDecimal calculateCoutTotalMenu(@Param("menuId") Long menuId);
    
    /**
     * Vérifie s'il existe des conflits de stock pour plusieurs menus à la même date
     */
    @Query("""
        SELECT mi.produit.id, 
               mi.produit.nom,
               mi.produit.quantiteStock,
               SUM(mi.quantiteConvertieStockUnit) as quantiteTotaleRequise
        FROM MenuIngredient mi 
        WHERE mi.menu.dateService = :dateService 
        AND mi.menu.statut = 'CONFIRME'
        GROUP BY mi.produit.id, mi.produit.nom, mi.produit.quantiteStock
        HAVING SUM(mi.quantiteConvertieStockUnit) > mi.produit.quantiteStock
        """)
    List<Object[]> detecterConflitsStockParDate(@Param("dateService") java.time.LocalDate dateService);
    
    /**
     * Mise à jour en batch du coût des ingrédients
     */
    @Query("UPDATE MenuIngredient mi SET mi.coutIngredient = mi.quantiteConvertieStockUnit * mi.produit.prixUnitaire WHERE mi.menu.id = :menuId")
    int updateCoutIngredientsMenu(@Param("menuId") Long menuId);
}