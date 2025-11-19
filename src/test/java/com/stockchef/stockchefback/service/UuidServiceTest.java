package com.stockchef.stockchefback.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests pour UuidService
 * Vérifie la génération et validation des UUID
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Tests UuidService")
class UuidServiceTest {

    @Autowired
    private UuidService uuidService;

    @Test
    @DisplayName("Génération UUID - Doit créer des UUIDs uniques")
    void shouldGenerateUniqueUuids() {
        // When
        String uuid1 = uuidService.generateUuid();
        String uuid2 = uuidService.generateUuid();
        
        // Then
        assertThat(uuid1).isNotNull();
        assertThat(uuid2).isNotNull();
        assertThat(uuid1).isNotEqualTo(uuid2);
        
        // Vérifier le format UUID
        assertThat(UUID.fromString(uuid1)).isNotNull();
        assertThat(UUID.fromString(uuid2)).isNotNull();
    }

    @Test
    @DisplayName("Validation UUID - Doit valider correctement les UUIDs")
    void shouldValidateUuids() {
        // Given
        String validUuid = UUID.randomUUID().toString();
        String invalidUuid = "not-a-valid-uuid";
        String emptyString = "";
        String nullString = null;
        
        // When & Then
        assertThat(uuidService.isValidUuid(validUuid)).isTrue();
        assertThat(uuidService.isValidUuid(invalidUuid)).isFalse();
        assertThat(uuidService.isValidUuid(emptyString)).isFalse();
        assertThat(uuidService.isValidUuid(nullString)).isFalse();
    }

    @Test
    @DisplayName("Parse UUID - Doit convertir String en UUID")
    void shouldParseValidUuid() {
        // Given
        String uuidString = UUID.randomUUID().toString();
        
        // When
        UUID parsed = uuidService.parseUuid(uuidString);
        
        // Then
        assertThat(parsed).isNotNull();
        assertThat(parsed.toString()).isEqualTo(uuidString);
    }

    @Test
    @DisplayName("Parse UUID invalide - Doit lever exception")
    void shouldThrowExceptionForInvalidUuid() {
        // Given
        String invalidUuid = "invalid-uuid";
        
        // When & Then
        assertThatThrownBy(() -> uuidService.parseUuid(invalidUuid))
                .isInstanceOf(IllegalArgumentException.class);
    }
}