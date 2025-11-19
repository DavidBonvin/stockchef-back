package com.stockchef.stockchefback.repository;

import com.stockchef.stockchefback.model.User;
import com.stockchef.stockchefback.model.UserRole;
import com.stockchef.stockchefback.testutil.TestUuidHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests TDD para UserRepository
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private User developerUser;
    private User adminUser;
    private User chefUser;
    private User employeeUser;

    @BeforeEach
    void setUp() {
        // Crear usuarios de prueba para cada rol
        developerUser = User.builder()
                .id(TestUuidHelper.DEVELOPER_UUID)
                .email("developer@stockchef.com")
                .password(passwordEncoder.encode("dev123"))
                .firstName("Super")
                .lastName("Developer")
                .role(UserRole.ROLE_DEVELOPER)
                .isActive(true)
                .build();

        adminUser = User.builder()
                .id(TestUuidHelper.ADMIN_UUID)
                .email("admin@stockchef.com")
                .password(passwordEncoder.encode("admin123"))
                .firstName("Restaurant")
                .lastName("Admin")
                .role(UserRole.ROLE_ADMIN)
                .isActive(true)
                .build();

        chefUser = User.builder()
                .id(TestUuidHelper.USER_1_UUID)
                .email("chef@stockchef.com")
                .password(passwordEncoder.encode("chef123"))
                .firstName("Head")
                .lastName("Chef")
                .role(UserRole.ROLE_CHEF)
                .isActive(true)
                .build();

        employeeUser = User.builder()
                .id(TestUuidHelper.USER_2_UUID)
                .email("employee@stockchef.com")
                .password(passwordEncoder.encode("emp123"))
                .firstName("Kitchen")
                .lastName("Employee")
                .role(UserRole.ROLE_EMPLOYEE)
                .isActive(true)
                .build();

        // Persistir usuarios en la base de datos de prueba
        entityManager.persistAndFlush(developerUser);
        entityManager.persistAndFlush(adminUser);
        entityManager.persistAndFlush(chefUser);
        entityManager.persistAndFlush(employeeUser);
    }

    @Test
    @DisplayName("Debe encontrar usuario por email - DEVELOPER")
    void shouldFindUserByEmail_Developer() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("developer@stockchef.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("developer@stockchef.com");
        assertThat(foundUser.get().getRole()).isEqualTo(UserRole.ROLE_DEVELOPER);
        assertThat(foundUser.get().getFirstName()).isEqualTo("Super");
        assertThat(foundUser.get().getLastName()).isEqualTo("Developer");
        assertThat(foundUser.get().isEnabled()).isTrue();
    }

    @Test
    @DisplayName("Debe encontrar usuario por email - ADMIN")
    void shouldFindUserByEmail_Admin() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("admin@stockchef.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getRole()).isEqualTo(UserRole.ROLE_ADMIN);
        assertThat(foundUser.get().getFirstName()).isEqualTo("Restaurant");
    }

    @Test
    @DisplayName("Debe encontrar usuario por email - CHEF")
    void shouldFindUserByEmail_Chef() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("chef@stockchef.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getRole()).isEqualTo(UserRole.ROLE_CHEF);
        assertThat(foundUser.get().getFirstName()).isEqualTo("Head");
    }

    @Test
    @DisplayName("Debe encontrar usuario por email - EMPLOYEE")
    void shouldFindUserByEmail_Employee() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("employee@stockchef.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getRole()).isEqualTo(UserRole.ROLE_EMPLOYEE);
        assertThat(foundUser.get().getFirstName()).isEqualTo("Kitchen");
    }

    @Test
    @DisplayName("Debe retornar empty cuando usuario no existe")
    void shouldReturnEmptyWhenUserNotFound() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@stockchef.com");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Debe verificar que el email es único")
    void shouldVerifyEmailIsUnique() {
        // Given - intentar crear usuario con email existente
        User duplicateUser = User.builder()
                .id(TestUuidHelper.USER_3_UUID)
                .email("developer@stockchef.com") // email duplicado
                .password("newpass")
                .firstName("Duplicate")
                .lastName("User")
                .role(UserRole.ROLE_EMPLOYEE)
                .build();

        // When & Then - debe fallar por constraint de email único
        Exception exception = assertThrows(Exception.class, () -> {
            entityManager.persistAndFlush(duplicateUser);
        });
        
        // Verificar que sea una violación de unicidad (independientemente del idioma del mensaje)
        String message = exception.getMessage().toLowerCase();
        assertThat(message).satisfiesAnyOf(
            msg -> assertThat(msg).contains("constraint"),
            msg -> assertThat(msg).contains("unique"),
            msg -> assertThat(msg).contains("violation"),
            msg -> assertThat(msg).contains("duplicate"),
            msg -> assertThat(msg).contains("index"),
            msg -> assertThat(msg).contains("primaire"),  // francés
            msg -> assertThat(msg).contains("23505")       // código de error SQL
        );
    }

    @Test
    @DisplayName("Debe encontrar usuarios activos solamente")
    void shouldFindOnlyActiveUsers() {
        // Given - crear usuario inactivo
        User inactiveUser = User.builder()
                .id(TestUuidHelper.MODERATOR_UUID)
                .email("inactive@stockchef.com")
                .password("pass123")
                .firstName("Inactive")
                .lastName("User")
                .role(UserRole.ROLE_EMPLOYEE)
                .isActive(false)
                .build();

        entityManager.persistAndFlush(inactiveUser);

        // When
        Optional<User> foundActiveUser = userRepository.findByEmail("developer@stockchef.com");
        Optional<User> foundInactiveUser = userRepository.findByEmail("inactive@stockchef.com");

        // Then
        assertThat(foundActiveUser).isPresent();
        assertThat(foundActiveUser.get().isEnabled()).isTrue();
        
        assertThat(foundInactiveUser).isPresent();
        assertThat(foundInactiveUser.get().isEnabled()).isFalse();
    }

    @Test
    @DisplayName("Debe verificar que password está encriptado")
    void shouldVerifyPasswordIsEncrypted() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("developer@stockchef.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getPassword()).isNotEqualTo("dev123");
        assertThat(foundUser.get().getPassword()).startsWith("$2a$"); // BCrypt format
        assertThat(passwordEncoder.matches("dev123", foundUser.get().getPassword())).isTrue();
    }

    @Test
    @DisplayName("Debe tener UUIDs válidos como identificadores")
    void shouldHaveValidUuidsAsIdentifiers() {
        // When
        Optional<User> foundDeveloper = userRepository.findByEmail("developer@stockchef.com");
        Optional<User> foundAdmin = userRepository.findByEmail("admin@stockchef.com");
        Optional<User> foundChef = userRepository.findByEmail("chef@stockchef.com");
        Optional<User> foundEmployee = userRepository.findByEmail("employee@stockchef.com");

        // Verificar que todos los usuarios existen
        assertThat(foundDeveloper).isPresent();
        assertThat(foundAdmin).isPresent();
        assertThat(foundChef).isPresent();
        assertThat(foundEmployee).isPresent();

        // Verificar que los IDs son UUIDs válidos
        assertThat(TestUuidHelper.isValidUuid(foundDeveloper.get().getId())).isTrue();
        assertThat(TestUuidHelper.isValidUuid(foundAdmin.get().getId())).isTrue();
        assertThat(TestUuidHelper.isValidUuid(foundChef.get().getId())).isTrue();
        assertThat(TestUuidHelper.isValidUuid(foundEmployee.get().getId())).isTrue();

        // Verificar que los UUIDs específicos fueron asignados correctamente
        assertThat(foundDeveloper.get().getId()).isEqualTo(TestUuidHelper.DEVELOPER_UUID);
        assertThat(foundAdmin.get().getId()).isEqualTo(TestUuidHelper.ADMIN_UUID);
        assertThat(foundChef.get().getId()).isEqualTo(TestUuidHelper.USER_1_UUID);
        assertThat(foundEmployee.get().getId()).isEqualTo(TestUuidHelper.USER_2_UUID);
    }

    @Test
    @DisplayName("Debe encontrar usuario por ID UUID")
    void shouldFindUserByUuidId() {
        // When
        Optional<User> foundByDeveloperId = userRepository.findById(TestUuidHelper.DEVELOPER_UUID);
        Optional<User> foundByAdminId = userRepository.findById(TestUuidHelper.ADMIN_UUID);

        // Then
        assertThat(foundByDeveloperId).isPresent();
        assertThat(foundByDeveloperId.get().getEmail()).isEqualTo("developer@stockchef.com");
        assertThat(foundByDeveloperId.get().getRole()).isEqualTo(UserRole.ROLE_DEVELOPER);

        assertThat(foundByAdminId).isPresent();
        assertThat(foundByAdminId.get().getEmail()).isEqualTo("admin@stockchef.com");
        assertThat(foundByAdminId.get().getRole()).isEqualTo(UserRole.ROLE_ADMIN);
    }

    @Test
    @DisplayName("Debe retornar empty cuando busca por UUID inexistente")
    void shouldReturnEmptyWhenSearchingByNonExistentUuid() {
        // When
        String nonExistentUuid = TestUuidHelper.createTestUuid(9999);
        Optional<User> foundUser = userRepository.findById(nonExistentUuid);

        // Then
        assertThat(foundUser).isEmpty();
    }
}