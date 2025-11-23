package com.stockchef.stockchefback.service;

import com.stockchef.stockchefback.dto.user.RegisterRequest;
import com.stockchef.stockchefback.dto.user.UserResponse;
import com.stockchef.stockchefback.model.User;
import com.stockchef.stockchefback.model.UserRole;
import com.stockchef.stockchefback.repository.UserRepository;
import com.stockchef.stockchefback.service.JwtService;
import com.stockchef.stockchefback.testutil.TestUuidHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests d'intégration pour UserService - Logique métier complète avec JWT réel
 * Conversion de unit tests à integration tests pour garantir la compatibilité production
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@DisplayName("Tests d'Intégration - UserService")
class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtService jwtService;

    private User testAdmin;
    private User testEmployee; 
    private String adminToken;
    private String employeeToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        
        LocalDateTime now = LocalDateTime.now();
        
        // Crear usuarios de test directamente siguiendo el patrón exitoso
        testAdmin = User.builder()
                .email("admin@test.com")
                .password(passwordEncoder.encode("admin123"))
                .firstName("Admin")
                .lastName("User")
                .role(UserRole.ROLE_ADMIN)
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .createdBy("system")
                .build();
        testAdmin = userRepository.save(testAdmin);
        
        testEmployee = User.builder()
                .email("employee@test.com")
                .password(passwordEncoder.encode("emp123"))
                .firstName("Employee")
                .lastName("User")
                .role(UserRole.ROLE_EMPLOYEE)
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .createdBy("system")
                .build();
        testEmployee = userRepository.save(testEmployee);
        
        // Generar tokens JWT para los tests
        adminToken = jwtService.generateToken(testAdmin);
        employeeToken = jwtService.generateToken(testEmployee);
    }

    // Tests pour l'enregistrement d'utilisateur

    @Test
    @DisplayName("INT: registerNewUser - Succès avec données valides")
    void shouldRegisterNewUser_WhenValidData() {
        // Given
        RegisterRequest newUserRequest = new RegisterRequest(
                "newuser@test.com",
                "password123", 
                "New",
                "User"
        );

        // When
        UserResponse result = userService.registerNewUser(newUserRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("newuser@test.com");
        assertThat(result.firstName()).isEqualTo("New");
        assertThat(result.lastName()).isEqualTo("User");
        assertThat(result.role()).isEqualTo(UserRole.ROLE_EMPLOYEE);
        assertThat(result.effectiveRole()).isEqualTo(UserRole.ROLE_EMPLOYEE);
        assertThat(result.isActive()).isTrue();
        assertThat(result.createdBy()).isEqualTo("system");
        
        // Verificar que se creó en la base de datos
        assertThat(userRepository.existsByEmail("newuser@test.com")).isTrue();
    }

    @Test
    @DisplayName("INT: registerNewUser - Exception si email existe déjà")
    void shouldThrowException_WhenEmailAlreadyExists() {
        // Given - el usuario admin ya existe
        RegisterRequest duplicateRequest = new RegisterRequest(
                testAdmin.getEmail(), // Email que ya existe
                "password123",
                "Duplicate",
                "User"
        );

        // When & Then
        assertThatThrownBy(() -> userService.registerNewUser(duplicateRequest))
                .hasMessageContaining("email")
                .hasMessageContaining("utilisé");
    }

    @Test
    @DisplayName("INT: registerNewUser - Mot de passe encodé correctement")
    void shouldEncodePassword_WhenRegisteringUser() {
        // Given
        RegisterRequest request = new RegisterRequest(
                "encoded@test.com",
                "plainpassword",
                "Test", 
                "User"
        );

        // When
        UserResponse result = userService.registerNewUser(request);

        // Then
        assertThat(result).isNotNull();
        
        // Verificar que el usuario puede hacer login con la contraseña original
        User savedUser = userRepository.findByEmail("encoded@test.com").orElseThrow();
        assertThat(passwordEncoder.matches("plainpassword", savedUser.getPassword())).isTrue();
    }

    // Tests pour la gestion des rôles effectifs

    @Test
    @DisplayName("INT: getEffectiveRole - Utilisateur actif garde son rôle")
    void shouldReturnActualRole_WhenUserIsActive() {
        // Given - testAdmin est actif avec ROLE_ADMIN
        assertThat(testAdmin.getIsActive()).isTrue();
        assertThat(testAdmin.getRole()).isEqualTo(UserRole.ROLE_ADMIN);

        // When
        UserRole effectiveRole = userService.getEffectiveRole(testAdmin);

        // Then
        assertThat(effectiveRole).isEqualTo(UserRole.ROLE_ADMIN);
    }

    @Test
    @DisplayName("INT: getEffectiveRole - Utilisateur inactif devient EMPLOYEE")
    void shouldReturnEmployeeRole_WhenUserIsInactive() {
        // Given - crear usuario inactivo
        LocalDateTime now = LocalDateTime.now();
        User inactiveChef = User.builder()
                .email("inactive@test.com")
                .password(passwordEncoder.encode("pass123"))
                .firstName("Inactive")
                .lastName("Chef")
                .role(UserRole.ROLE_CHEF)
                .isActive(false) // Usuario inactivo
                .createdAt(now)
                .updatedAt(now)
                .createdBy("system")
                .build();
        inactiveChef = userRepository.save(inactiveChef);

        // When
        UserRole effectiveRole = userService.getEffectiveRole(inactiveChef);

        // Then
        assertThat(effectiveRole).isEqualTo(UserRole.ROLE_EMPLOYEE);
    }

    // Tests pour la mise à jour des rôles

    @Test
    @DisplayName("INT: updateUserRole - DEVELOPER peut changer vers n'importe quel rôle")
    void shouldUpdateRole_WhenDeveloperChangesAnyRole() {
        // Given - crear un DEVELOPER en la base de datos
        LocalDateTime now = LocalDateTime.now();
        User developer = User.builder()
                .email("developer@test.com")
                .password(passwordEncoder.encode("dev123"))
                .firstName("Developer")
                .lastName("User")
                .role(UserRole.ROLE_DEVELOPER)
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .createdBy("system")
                .build();
        developer = userRepository.save(developer);

        // When - DEVELOPER promoviendo a EMPLOYEE a ADMIN
        UserResponse result = userService.updateUserRole(testEmployee.getId(), UserRole.ROLE_ADMIN, "Promotion", developer);

        // Then
        assertThat(result.role()).isEqualTo(UserRole.ROLE_ADMIN);
        
        // Verificar en base de datos
        User updatedUser = userRepository.findById(testEmployee.getId()).orElseThrow();
        assertThat(updatedUser.getRole()).isEqualTo(UserRole.ROLE_ADMIN);
    }

    @Test
    @DisplayName("INT: updateUserRole - ADMIN ne peut pas créer DEVELOPER")
    void shouldThrowException_WhenAdminTriesToCreateDeveloper() {
        // When & Then - Admin intenta promover a EMPLOYEE a DEVELOPER
        assertThatThrownBy(() -> userService.updateUserRole(testEmployee.getId(), UserRole.ROLE_DEVELOPER, "Non autorisé", testAdmin))
                .hasMessageContaining("administrateurs");

        // Verificar que el usuario no fue modificado
        User unchangedUser = userRepository.findById(testEmployee.getId()).orElseThrow();
        assertThat(unchangedUser.getRole()).isEqualTo(UserRole.ROLE_EMPLOYEE); // Sigue siendo EMPLOYEE
    }

    // Tests pour la mise à jour du statut

    @Test
    @DisplayName("INT: updateUserStatus - Désactiver utilisateur")
    void shouldDeactivateUser_WhenRequestedByAdmin() {
        // Given - testEmployee est actif
        assertThat(testEmployee.getIsActive()).isTrue();
        assertThat(testEmployee.getRole()).isEqualTo(UserRole.ROLE_EMPLOYEE);

        // When - Admin desactiva al employee
        UserResponse result = userService.updateUserStatus(testEmployee.getId(), false, "Suspension");

        // Then
        assertThat(result.isActive()).isFalse();
        assertThat(result.role()).isEqualTo(UserRole.ROLE_EMPLOYEE); // Rôle réel conservé
        assertThat(result.effectiveRole()).isEqualTo(UserRole.ROLE_EMPLOYEE); // Rôle effectif dégradé (EMPLOYEE inactivo = EMPLOYEE)

        // Verificar en base de datos
        User updatedUser = userRepository.findById(testEmployee.getId()).orElseThrow();
        assertThat(updatedUser.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("INT: updateUserStatus - Réactiver utilisateur restaure permissions")
    void shouldReactivateUser_AndRestoreRole() {
        // Given - crear un usuario inactivo CHEF
        LocalDateTime now = LocalDateTime.now();
        User inactiveChef = User.builder()
                .email("inactivechef@test.com")
                .password(passwordEncoder.encode("chef123"))
                .firstName("Inactive")
                .lastName("Chef")
                .role(UserRole.ROLE_CHEF)
                .isActive(false) // Inactivo
                .createdAt(now)
                .updatedAt(now)
                .createdBy("system")
                .build();
        inactiveChef = userRepository.save(inactiveChef);

        // When - Reactivar usuario
        UserResponse result = userService.updateUserStatus(inactiveChef.getId(), true, "Réactivation");

        // Then
        assertThat(result.isActive()).isTrue();
        assertThat(result.role()).isEqualTo(UserRole.ROLE_CHEF);
        assertThat(result.effectiveRole()).isEqualTo(UserRole.ROLE_CHEF); // Permissions restaurées

        // Verificar en base de datos
        User reactivatedUser = userRepository.findById(inactiveChef.getId()).orElseThrow();
        assertThat(reactivatedUser.getIsActive()).isTrue();
    }

    // Tests pour la récupération des utilisateurs

    @Test
    @DisplayName("INT: getAllUsers - Retourne tous les utilisateurs avec rôles effectifs")
    void shouldReturnAllUsers_WithEffectiveRoles() {
        // Given - ya tenemos testAdmin (activo) y testEmployee (activo)
        // Agregar un usuario inactivo
        LocalDateTime now = LocalDateTime.now();
        User inactiveAdmin = User.builder()
                .email("inactiveadmin@test.com")
                .password(passwordEncoder.encode("pass123"))
                .firstName("Inactive")
                .lastName("Admin")
                .role(UserRole.ROLE_ADMIN)
                .isActive(false) // Inactivo
                .createdAt(now)
                .updatedAt(now)
                .createdBy("system")
                .build();
        userRepository.save(inactiveAdmin);

        // When
        List<UserResponse> result = userService.getAllUsers();

        // Then
        assertThat(result).hasSizeGreaterThanOrEqualTo(3); // Al menos los 3 usuarios que creamos
        
        // Verificar que hay usuarios activos e inactivos
        boolean hasActiveUser = result.stream().anyMatch(user -> user.isActive() && user.role().equals(UserRole.ROLE_ADMIN));
        boolean hasInactiveUser = result.stream().anyMatch(user -> !user.isActive() && user.effectiveRole().equals(UserRole.ROLE_EMPLOYEE));
        
        assertThat(hasActiveUser).isTrue();
        assertThat(hasInactiveUser).isTrue();
    }

    @Test
    @DisplayName("INT: updateUserRole - Exception si utilisateur non trouvé")
    void shouldThrowException_WhenUserNotFound() {
        // When & Then - intentar actualizar rol de usuario inexistente
        assertThatThrownBy(() -> userService.updateUserRole("uuid-inexistente", UserRole.ROLE_CHEF, "test"))
                .hasMessageContaining("requiere");
    }
}