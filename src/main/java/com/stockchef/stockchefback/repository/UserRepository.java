package com.stockchef.stockchefback.repository;

import com.stockchef.stockchefback.model.User;
import com.stockchef.stockchefback.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité User
 * Utilise String (UUID) comme type d'ID pour la sécurité
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    /**
     * Recherche un utilisateur par son email
     * @param email l'email de l'utilisateur
     * @return Optional<User> l'utilisateur s'il existe
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Vérifie si un utilisateur existe avec l'email donné
     * @param email l'email à vérifier
     * @return true s'il existe, false sinon
     */
    boolean existsByEmail(String email);

    /**
     * Recherche les utilisateurs par rôle
     * @param role le rôle à rechercher
     * @return Liste des utilisateurs avec ce rôle
     */
    List<User> findByRole(UserRole role);

    /**
     * Recherche les utilisateurs par état actif/inactif
     * @param isActive l'état à rechercher
     * @return Liste des utilisateurs avec cet état
     */
    List<User> findByIsActive(boolean isActive);

    /**
     * Recherche les utilisateurs par rôle et état
     * @param role le rôle à rechercher
     * @param isActive l'état à rechercher
     * @return Liste des utilisateurs qui correspondent aux deux critères
     */
    List<User> findByRoleAndIsActive(UserRole role, boolean isActive);
}