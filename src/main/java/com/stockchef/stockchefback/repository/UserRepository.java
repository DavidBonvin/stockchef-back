package com.stockchef.stockchefback.repository;

import com.stockchef.stockchefback.model.User;
import com.stockchef.stockchefback.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para la entidad User
 * Utiliza String (UUID) como tipo de ID para seguridad
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    /**
     * Busca un usuario por su email
     * @param email el email del usuario
     * @return Optional<User> el usuario si existe
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Verifica si existe un usuario con el email dado
     * @param email el email a verificar
     * @return true si existe, false si no
     */
    boolean existsByEmail(String email);

    /**
     * Busca usuarios por rol
     * @param role el rol a buscar
     * @return Lista de usuarios con ese rol
     */
    List<User> findByRole(UserRole role);

    /**
     * Busca usuarios por estado activo/inactivo
     * @param isActive el estado a buscar
     * @return Lista de usuarios con ese estado
     */
    List<User> findByIsActive(boolean isActive);

    /**
     * Busca usuarios por rol y estado
     * @param role el rol a buscar
     * @param isActive el estado a buscar
     * @return Lista de usuarios que coinciden con ambos criterios
     */
    List<User> findByRoleAndIsActive(UserRole role, boolean isActive);
}