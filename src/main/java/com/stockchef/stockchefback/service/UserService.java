package com.stockchef.stockchefback.service;

import com.stockchef.stockchefback.dto.user.RegisterRequest;
import com.stockchef.stockchefback.dto.user.UpdateUserRequest;
import com.stockchef.stockchefback.dto.user.UserResponse;
import com.stockchef.stockchefback.exception.EmailAlreadyExistsException;
import com.stockchef.stockchefback.exception.InsufficientPermissionsException;
import com.stockchef.stockchefback.exception.UnauthorizedUserException;
import com.stockchef.stockchefback.exception.UserNotFoundException;
import com.stockchef.stockchefback.model.User;
import com.stockchef.stockchefback.model.UserRole;
import com.stockchef.stockchefback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de usuarios
 * Contiene toda la lógica de negocio para registro,
 * gestión de roles y estado activo/inactivo
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Enregistre un nouvel utilisateur avec le rôle EMPLOYEE par défaut
     * Accessible publiquement
     */
    public UserResponse registerNewUser(RegisterRequest request) {
        log.info("Tentative d'enregistrement pour email: {}", request.email());

        // Vérifier que l'email n'existe pas déjà
        if (userRepository.findByEmail(request.email()).isPresent()) {
            log.warn("Tentative d'enregistrement avec email déjà existant: {}", request.email());
            throw new EmailAlreadyExistsException("Un utilisateur avec cet email existe déjà");
        }

        // Créer le nouvel utilisateur
        User newUser = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(UserRole.ROLE_EMPLOYEE) // Rôle par défaut
                .isActive(true) // Actif par défaut
                .createdAt(LocalDateTime.now())
                .createdBy("system")
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("Nouvel utilisateur créé: {} avec ID: {}", savedUser.getEmail(), savedUser.getId());

        return convertToUserResponse(savedUser);
    }

    /**
     * Retorna todos los usuarios con sus roles efectivos
     * Accesible únicamente a ADMIN y DEVELOPER
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.info("Récupération de tous les utilisateurs");
        
        return userRepository.findAll().stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca un usuario por su email
     * Usado para obtener el perfil del usuario autenticado
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        log.info("Búsqueda de usuario por email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con email: " + email));
        
        log.info("Usuario encontrado: {} (ID: {})", user.getEmail(), user.getId());
        return convertToUserResponse(user);
    }

    /**
     * Met à jour le rôle d'un utilisateur
     * Accessible uniquement aux ADMIN et DEVELOPER avec restrictions
     */
    public UserResponse updateUserRole(String userId, UserRole newRole, String reason, User requester) {
        log.info("Mise à jour du rôle pour utilisateur ID: {} vers {} par {}", 
                userId, newRole, requester.getEmail());

        // Vérifier les permissions
        validateRoleChangePermissions(requester, newRole);

        // Trouver l'utilisateur
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé avec l'ID: " + userId));

        // Mettre à jour le rôle
        UserRole oldRole = user.getRole();
        user.setRole(newRole);
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        
        log.info("Rôle mis à jour pour {}: {} -> {} par {}, raison: {}", 
                user.getEmail(), oldRole, newRole, requester.getEmail(), reason);

        return convertToUserResponse(updatedUser);
    }

    /**
     * Surcharge pour les tests - utilise l'utilisateur requester passé en paramètre
     */
    public UserResponse updateUserRole(String userId, UserRole newRole, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé avec l'ID: " + userId));
        
        user.setRole(newRole);
        user.setUpdatedAt(LocalDateTime.now());
        
        User updatedUser = userRepository.save(user);
        return convertToUserResponse(updatedUser);
    }

    /**
     * Met à jour le statut actif/inactif d'un utilisateur
     * Accessible uniquement aux ADMIN et DEVELOPER
     */
    public UserResponse updateUserStatus(String userId, Boolean active, String reason) {
        log.info("Mise à jour du statut pour utilisateur ID: {} vers {} pour raison: {}", 
                userId, active ? "actif" : "inactif", reason);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé avec l'ID: " + userId));

        boolean wasActive = user.getIsActive();
        user.setIsActive(active);
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        
        log.info("Statut mis à jour pour {}: {} -> {}, raison: {}", 
                user.getEmail(), wasActive ? "actif" : "inactif", 
                active ? "actif" : "inactif", reason);

        return convertToUserResponse(updatedUser);
    }

    /**
     * Retourne le rôle effectif d'un utilisateur
     * Si l'utilisateur est inactif, il a les permissions d'EMPLOYEE
     */
    public UserRole getEffectiveRole(User user) {
        if (!user.getIsActive()) {
            return UserRole.ROLE_EMPLOYEE;
        }
        return user.getRole();
    }

    /**
     * Met à jour la dernière connexion d'un utilisateur
     */
    public void updateLastLogin(User user) {
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("Última conexión actualizada para: {}", user.getEmail());
    }

    /**
     * Convertit une entité User en UserResponse avec le rôle effectif
     */
    private UserResponse convertToUserResponse(User user) {
        UserRole effectiveRole = getEffectiveRole(user);
        
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getFirstName() + " " + user.getLastName(),
                user.getRole(),
                effectiveRole,
                user.getIsActive(),
                user.getCreatedAt(),
                user.getLastLoginAt(),
                user.getCreatedBy()
        );
    }

    /**
     * Valide les permissions pour changer un rôle
     */
    private void validateRoleChangePermissions(User requester, UserRole newRole) {
        // DEVELOPER peut changer vers n'importe quel rôle
        if (requester.getRole() == UserRole.ROLE_DEVELOPER) {
            return;
        }

        // ADMIN peut changer vers tous les rôles sauf DEVELOPER
        if (requester.getRole() == UserRole.ROLE_ADMIN && newRole != UserRole.ROLE_DEVELOPER) {
            return;
        }

        // Sinon, accès refusé
        if (requester.getRole() == UserRole.ROLE_ADMIN && newRole == UserRole.ROLE_DEVELOPER) {
            throw new InsufficientPermissionsException("Un ADMIN ne peut pas créer un DEVELOPER");
        }

        throw new InsufficientPermissionsException("Permissions insuffisantes pour changer ce rôle");
    }

    /**
     * Actualiza la información personal de un usuario (firstName, lastName, email)
     * Reglas de autorización:
     * - Todos los roles pueden modificar su propia información
     * - DEVELOPER y ADMIN pueden modificar información de otros usuarios
     * - EMPLOYEE y CHEF solo pueden modificar su propia información
     */
    public UserResponse updateUser(String userId, UpdateUserRequest request, String currentUserEmail) {
        log.info("Solicitud de actualización para usuario {} por {}", userId, currentUserEmail);

        // Buscar el usuario a actualizar
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        // Buscar el usuario que hace la petición
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new UserNotFoundException("Usuario autenticado no encontrado"));

        // Verificar permisos de autorización
        validateUpdatePermissions(targetUser, currentUser);

        // Verificar que el email no esté ya en uso por otro usuario
        if (request.email() != null && !request.email().equals(targetUser.getEmail())) {
            userRepository.findByEmail(request.email())
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(targetUser.getId())) {
                            throw new EmailAlreadyExistsException("El email ya está en uso");
                        }
                    });
        }

        // Actualizar los campos solicitados
        if (request.firstName() != null) {
            targetUser.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            targetUser.setLastName(request.lastName());
        }
        if (request.email() != null) {
            targetUser.setEmail(request.email());
        }

        // Establecer fecha de actualización
        targetUser.setUpdatedAt(LocalDateTime.now());

        // Guardar cambios
        User savedUser = userRepository.save(targetUser);

        log.info("Usuario actualizado exitosamente: {} (ID: {})", savedUser.getEmail(), savedUser.getId());

        return convertToUserResponse(savedUser);
    }

    /**
     * Valida los permisos para actualizar información de usuario
     * @param targetUser Usuario que se va a actualizar
     * @param currentUser Usuario que hace la petición
     */
    private void validateUpdatePermissions(User targetUser, User currentUser) {
        // Si es el mismo usuario, permitir siempre
        if (targetUser.getId().equals(currentUser.getId())) {
            log.info("Usuario {} actualizando su propia información", currentUser.getEmail());
            return;
        }

        // Solo DEVELOPER y ADMIN pueden modificar otros usuarios
        UserRole currentRole = getEffectiveRole(currentUser);
        if (currentRole != UserRole.ROLE_DEVELOPER && currentRole != UserRole.ROLE_ADMIN) {
            log.warn("Usuario {} ({}) intentó modificar usuario {} sin permisos", 
                    currentUser.getEmail(), currentRole, targetUser.getId());
            throw new UnauthorizedUserException("No tienes permisos para modificar este usuario");
        }

        log.info("Usuario {} ({}) autorizado para modificar usuario {}", 
                currentUser.getEmail(), currentRole, targetUser.getId());
    }
}