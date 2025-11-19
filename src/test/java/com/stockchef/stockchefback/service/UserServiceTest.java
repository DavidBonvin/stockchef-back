package com.stockchef.stockchefback.service;

import com.stockchef.stockchefback.dto.user.RegisterRequest;
import com.stockchef.stockchefback.dto.user.UserResponse;
import com.stockchef.stockchefback.exception.EmailAlreadyExistsException;
import com.stockchef.stockchefback.exception.InsufficientPermissionsException;
import com.stockchef.stockchefback.exception.UserNotFoundException;
import com.stockchef.stockchefback.model.User;
import com.stockchef.stockchefback.model.UserRole;
import com.stockchef.stockchefback.repository.UserRepository;
import com.stockchef.stockchefback.testutil.TestUuidHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests TDD pour UserService - Logique métier
 * RED -> GREEN -> REFACTOR
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests TDD - UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private RegisterRequest validRegisterRequest;
    private User existingUser;
    private User newUser;

    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest(
                "nouveau@test.com",
                "password123",
                "Jean",
                "Dupont"
        );

        existingUser = User.builder()
                .id(TestUuidHelper.USER_1_UUID)
                .email("existing@test.com")
                .password("encodedPassword")
                .firstName("Marie")
                .lastName("Martin")
                .role(UserRole.ROLE_CHEF)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        newUser = User.builder()
                .id(TestUuidHelper.USER_2_UUID)
                .email("nouveau@test.com")
                .password("encodedPassword123")
                .firstName("Jean")
                .lastName("Dupont")
                .role(UserRole.ROLE_EMPLOYEE) // Par défaut
                .isActive(true) // Par défaut
                .createdAt(LocalDateTime.now())
                .createdBy("system")
                .build();
    }

    // Tests pour l'enregistrement d'utilisateur

    @Test
    @DisplayName("RED: registerNewUser - Succès avec données valides")
    void shouldRegisterNewUser_WhenValidData() {
        // Given
        when(userRepository.findByEmail(validRegisterRequest.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(validRegisterRequest.password())).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // When
        UserResponse result = userService.registerNewUser(validRegisterRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo(validRegisterRequest.email());
        assertThat(result.firstName()).isEqualTo(validRegisterRequest.firstName());
        assertThat(result.lastName()).isEqualTo(validRegisterRequest.lastName());
        assertThat(result.role()).isEqualTo(UserRole.ROLE_EMPLOYEE);
        assertThat(result.effectiveRole()).isEqualTo(UserRole.ROLE_EMPLOYEE);
        assertThat(result.isActive()).isTrue();
        assertThat(result.createdBy()).isEqualTo("system");

        // Vérifications des interactions
        verify(userRepository).findByEmail(validRegisterRequest.email());
        verify(passwordEncoder).encode(validRegisterRequest.password());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("RED: registerNewUser - Exception si email existe déjà")
    void shouldThrowException_WhenEmailAlreadyExists() {
        // Given
        when(userRepository.findByEmail(validRegisterRequest.email())).thenReturn(Optional.of(existingUser));

        // When & Then
        assertThatThrownBy(() -> userService.registerNewUser(validRegisterRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Un utilisateur avec cet email existe déjà");

        // Vérifications - ne doit pas encoder ni sauvegarder
        verify(userRepository).findByEmail(validRegisterRequest.email());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("RED: registerNewUser - Mot de passe encodé correctement")
    void shouldEncodePassword_WhenRegisteringUser() {
        // Given
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // When
        userService.registerNewUser(validRegisterRequest);

        // Then
        verify(passwordEncoder).encode("password123");
        
        // Vérifier que le user sauvé a le mot de passe encodé
        verify(userRepository).save(argThat(user -> 
                "encodedPassword123".equals(user.getPassword())
        ));
    }

    // Tests pour la gestion des rôles effectifs

    @Test
    @DisplayName("RED: getEffectiveRole - Utilisateur actif garde son rôle")
    void shouldReturnActualRole_WhenUserIsActive() {
        // Given
        User activeChef = User.builder()
                .role(UserRole.ROLE_CHEF)
                .isActive(true)
                .build();

        // When
        UserRole effectiveRole = userService.getEffectiveRole(activeChef);

        // Then
        assertThat(effectiveRole).isEqualTo(UserRole.ROLE_CHEF);
    }

    @Test
    @DisplayName("RED: getEffectiveRole - Utilisateur inactif devient EMPLOYEE")
    void shouldReturnEmployeeRole_WhenUserIsInactive() {
        // Given
        User inactiveAdmin = User.builder()
                .role(UserRole.ROLE_ADMIN)
                .isActive(false)
                .build();

        // When
        UserRole effectiveRole = userService.getEffectiveRole(inactiveAdmin);

        // Then
        assertThat(effectiveRole).isEqualTo(UserRole.ROLE_EMPLOYEE);
    }

    // Tests pour la mise à jour des rôles

    @Test
    @DisplayName("RED: updateUserRole - DEVELOPER peut changer vers n'importe quel rôle")
    void shouldUpdateRole_WhenDeveloperChangesAnyRole() {
        // Given
        User targetUser = User.builder()
                .id(TestUuidHelper.USER_1_UUID)
                .email("target@test.com")
                .role(UserRole.ROLE_EMPLOYEE)
                .isActive(true)
                .build();
                
        User requester = User.builder()
                .role(UserRole.ROLE_DEVELOPER)
                .build();

        when(userRepository.findById(TestUuidHelper.USER_1_UUID)).thenReturn(Optional.of(targetUser));
        when(userRepository.save(any(User.class))).thenReturn(targetUser);

        // When
        UserResponse result = userService.updateUserRole(TestUuidHelper.USER_1_UUID, UserRole.ROLE_ADMIN, "Promotion", requester);

        // Then
        assertThat(result.role()).isEqualTo(UserRole.ROLE_ADMIN);
        verify(userRepository).save(argThat(user -> 
                UserRole.ROLE_ADMIN.equals(user.getRole())
        ));
    }

    @Test
    @DisplayName("RED: updateUserRole - ADMIN ne peut pas créer DEVELOPER")
    void shouldThrowException_WhenAdminTriesToCreateDeveloper() {
        // Given
        User requester = User.builder()
                .role(UserRole.ROLE_ADMIN)
                .build();

        // When & Then
        assertThatThrownBy(() -> userService.updateUserRole(TestUuidHelper.USER_1_UUID, UserRole.ROLE_DEVELOPER, "Non autorisé", requester))
                .isInstanceOf(InsufficientPermissionsException.class)
                .hasMessage("Un ADMIN ne peut pas créer un DEVELOPER");

        verify(userRepository, never()).save(any());
    }

    // Tests pour la mise à jour du statut

    @Test
    @DisplayName("RED: updateUserStatus - Désactiver utilisateur")
    void shouldDeactivateUser_WhenRequestedByAdmin() {
        // Given
        User targetUser = User.builder()
                .id(TestUuidHelper.USER_1_UUID)
                .role(UserRole.ROLE_CHEF)
                .isActive(true)
                .build();

        when(userRepository.findById(TestUuidHelper.USER_1_UUID)).thenReturn(Optional.of(targetUser));
        when(userRepository.save(any(User.class))).thenReturn(targetUser);

        // When
        UserResponse result = userService.updateUserStatus(TestUuidHelper.USER_1_UUID, false, "Suspension");

        // Then
        assertThat(result.isActive()).isFalse();
        assertThat(result.role()).isEqualTo(UserRole.ROLE_CHEF); // Rôle réel conservé
        assertThat(result.effectiveRole()).isEqualTo(UserRole.ROLE_EMPLOYEE); // Rôle effectif dégradé

        verify(userRepository).save(argThat(user -> !user.getIsActive()));
    }

    @Test
    @DisplayName("RED: updateUserStatus - Réactiver utilisateur restaure permissions")
    void shouldReactivateUser_AndRestoreRole() {
        // Given
        User inactiveUser = User.builder()
                .id(TestUuidHelper.USER_1_UUID)
                .role(UserRole.ROLE_CHEF)
                .isActive(false)
                .build();

        when(userRepository.findById(TestUuidHelper.USER_1_UUID)).thenReturn(Optional.of(inactiveUser));
        when(userRepository.save(any(User.class))).thenReturn(inactiveUser);

        // When
        UserResponse result = userService.updateUserStatus(TestUuidHelper.USER_1_UUID, true, "Réactivation");

        // Then
        assertThat(result.isActive()).isTrue();
        assertThat(result.role()).isEqualTo(UserRole.ROLE_CHEF);
        assertThat(result.effectiveRole()).isEqualTo(UserRole.ROLE_CHEF); // Permissions restaurées

        verify(userRepository).save(argThat(user -> user.getIsActive()));
    }

    // Tests pour la récupération des utilisateurs

    @Test
    @DisplayName("RED: getAllUsers - Retourne tous les utilisateurs avec rôles effectifs")
    void shouldReturnAllUsers_WithEffectiveRoles() {
        // Given
        User activeUser = User.builder()
                .id(TestUuidHelper.USER_1_UUID)
                .email("active@test.com")
                .role(UserRole.ROLE_CHEF)
                .isActive(true)
                .build();
                
        User inactiveUser = User.builder()
                .id(TestUuidHelper.USER_2_UUID)
                .email("inactive@test.com")
                .role(UserRole.ROLE_ADMIN)
                .isActive(false)
                .build();

        when(userRepository.findAll()).thenReturn(List.of(activeUser, inactiveUser));

        // When
        List<UserResponse> result = userService.getAllUsers();

        // Then
        assertThat(result).hasSize(2);
        
        // Utilisateur actif
        UserResponse activeResponse = result.get(0);
        assertThat(activeResponse.role()).isEqualTo(UserRole.ROLE_CHEF);
        assertThat(activeResponse.effectiveRole()).isEqualTo(UserRole.ROLE_CHEF);
        assertThat(activeResponse.isActive()).isTrue();
        
        // Utilisateur inactif
        UserResponse inactiveResponse = result.get(1);
        assertThat(inactiveResponse.role()).isEqualTo(UserRole.ROLE_ADMIN);
        assertThat(inactiveResponse.effectiveRole()).isEqualTo(UserRole.ROLE_EMPLOYEE);
        assertThat(inactiveResponse.isActive()).isFalse();
    }

    @Test
    @DisplayName("RED: findById - Exception si utilisateur non trouvé")
    void shouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findById("nonexistent-uuid")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUserRole("nonexistent-uuid", UserRole.ROLE_CHEF, "test"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("Utilisateur non trouvé avec l'ID: nonexistent-uuid");
    }
}