package com.stockchef.stockchefback.repository.menu;

import com.stockchef.stockchefback.model.menu.Menu;
import com.stockchef.stockchefback.model.menu.StatutMenu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des menus avec queries optimisées
 */
@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    
    /**
     * Trouve tous les menus pour une date de service donnée
     */
    List<Menu> findByDateServiceOrderByNom(LocalDate dateService);
    
    /**
     * Trouve tous les menus par statut
     */
    List<Menu> findByStatutOrderByDateServiceDesc(StatutMenu statut);
    
    /**
     * Trouve tous les menus d'un chef responsable
     */
    List<Menu> findByChefResponsableOrderByDateServiceDesc(String chefResponsable);
    
    /**
     * Trouve les menus entre deux dates
     */
    List<Menu> findByDateServiceBetweenOrderByDateService(LocalDate dateDebut, LocalDate dateFin);
    
    /**
     * Trouve les menus confirmés pour une période (pour planning)
     */
    @Query("SELECT m FROM Menu m WHERE m.statut = :statut AND m.dateService BETWEEN :dateDebut AND :dateFin ORDER BY m.dateService, m.nom")
    List<Menu> findMenusConfirmesEntreDates(@Param("statut") StatutMenu statut, 
                                           @Param("dateDebut") LocalDate dateDebut, 
                                           @Param("dateFin") LocalDate dateFin);
    
    /**
     * Trouve un menu avec ses ingrédients chargés (fetch join pour éviter N+1)
     */
    @Query("SELECT m FROM Menu m LEFT JOIN FETCH m.ingredients WHERE m.id = :id")
    Optional<Menu> findByIdWithIngredients(@Param("id") Long id);
    
    /**
     * Trouve tous les menus avec leurs ingrédients pour une date
     */
    @Query("SELECT DISTINCT m FROM Menu m LEFT JOIN FETCH m.ingredients WHERE m.dateService = :dateService ORDER BY m.nom")
    List<Menu> findByDateServiceWithIngredients(@Param("dateService") LocalDate dateService);
    
    /**
     * Compte le nombre de menus par statut
     */
    @Query("SELECT m.statut, COUNT(m) FROM Menu m GROUP BY m.statut")
    List<Object[]> countMenusByStatut();
    
    /**
     * Trouve les menus qui utilisent un produit donné
     */
    @Query("SELECT DISTINCT m FROM Menu m JOIN m.ingredients mi WHERE mi.produit.id = :produitId")
    List<Menu> findMenusUtilisantProduit(@Param("produitId") Long produitId);
    
    /**
     * Vérifie si un produit est utilisé dans des menus confirmés
     */
    @Query("SELECT COUNT(m) > 0 FROM Menu m JOIN m.ingredients mi WHERE mi.produit.id = :produitId AND m.statut IN :statutsImpactants")
    boolean isProduitUtiliseDansMenusConfirmes(@Param("produitId") Long produitId, 
                                              @Param("statutsImpactants") List<StatutMenu> statutsImpactants);
    
    /**
     * Trouve les menus dont le coût dépasse un certain montant
     */
    @Query("SELECT m FROM Menu m WHERE m.coutTotalIngredients > :coutMaximal ORDER BY m.coutTotalIngredients DESC")
    List<Menu> findMenusAvecCoutEleve(@Param("coutMaximal") java.math.BigDecimal coutMaximal);
    
    /**
     * Calcule le coût moyen des menus par période
     */
    @Query("SELECT AVG(m.coutTotalIngredients) FROM Menu m WHERE m.dateService BETWEEN :dateDebut AND :dateFin AND m.statut = :statut")
    java.math.BigDecimal calculerCoutMoyenMenus(@Param("dateDebut") LocalDate dateDebut, 
                                              @Param("dateFin") LocalDate dateFin,
                                              @Param("statut") StatutMenu statut);
    
    /**
     * Trouve les menus en brouillon créés depuis plus de X jours
     */
    @Query("SELECT m FROM Menu m WHERE m.statut = 'BROUILLON' AND m.dateCreation < :dateLimite ORDER BY m.dateCreation")
    List<Menu> findMenusBrouillonAnciens(@Param("dateLimite") java.time.LocalDateTime dateLimite);
    
    /**
     * Recherche de menus par nom (recherche partielle, insensible à la casse)
     */
    @Query("SELECT m FROM Menu m WHERE LOWER(m.nom) LIKE LOWER(CONCAT('%', :nom, '%')) ORDER BY m.dateService DESC")
    List<Menu> rechercherParNom(@Param("nom") String nom);
    
    /**
     * Trouve les menus d'une date avec stock suffisant
     */
    @Query("""
        SELECT DISTINCT m FROM Menu m 
        LEFT JOIN FETCH m.ingredients mi 
        LEFT JOIN FETCH mi.produit p
        WHERE m.dateService = :dateService 
        AND NOT EXISTS (
            SELECT 1 FROM MenuIngredient mi2 
            WHERE mi2.menu = m 
            AND mi2.quantiteConvertieStockUnit > mi2.produit.quantiteStock
        )
        ORDER BY m.nom
        """)
    List<Menu> findMenusRealisablesParDate(@Param("dateService") LocalDate dateService);
    
    // === MÉTHODES AVEC PAGINATION POUR LE CONTROLLER ===
    
    /**
     * Recherche par nom avec pagination (insensible à la casse)
     */
    Page<Menu> findByNomContainingIgnoreCase(String nom, Pageable pageable);
    
    /**
     * Recherche par nom et plage de dates avec pagination
     */
    Page<Menu> findByNomContainingIgnoreCaseAndDateServiceBetween(String nom, LocalDate dateDebut, LocalDate dateFin, Pageable pageable);
    
    /**
     * Recherche par plage de dates avec pagination
     */
    Page<Menu> findByDateServiceBetween(LocalDate dateDebut, LocalDate dateFin, Pageable pageable);
    
    /**
     * Statistiques des menus par chef
     */
    @Query("""
        SELECT m.chefResponsable, 
               COUNT(m) as totalMenus,
               COUNT(CASE WHEN m.statut = 'CONFIRME' THEN 1 END) as menusConfirmes,
               COUNT(CASE WHEN m.statut = 'PREPARE' THEN 1 END) as menusPrepares,
               AVG(m.coutTotalIngredients) as coutMoyen
        FROM Menu m 
        WHERE m.dateService BETWEEN :dateDebut AND :dateFin
        GROUP BY m.chefResponsable
        ORDER BY totalMenus DESC
        """)
    List<Object[]> getStatistiquesParChef(@Param("dateDebut") LocalDate dateDebut, 
                                         @Param("dateFin") LocalDate dateFin);
    
    /**
     * Cuenta menús por estatus después de una fecha
     */
    @Query("SELECT COUNT(m) FROM Menu m WHERE m.statut = :statut AND m.dateService >= :startDate")
    Integer countByStatutAndDateServiceAfter(@Param("statut") StatutMenu statut, 
                                           @Param("startDate") LocalDateTime startDate);
    
    /**
     * Encuentra menús en un rango de fechas con estados específicos
     */
    @Query("SELECT m FROM Menu m WHERE m.dateService BETWEEN :startDate AND :endDate AND m.statut IN :statuts")
    List<Menu> findByDateServiceBetweenAndStatutIn(@Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate,
                                                  @Param("statuts") List<StatutMenu> statuts);
}