package com.stockchef.stockchefback.repository;

import com.stockchef.stockchefback.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para la entidad User
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
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
}