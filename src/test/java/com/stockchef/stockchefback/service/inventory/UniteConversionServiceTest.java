package com.stockchef.stockchefback.service.inventory;

import com.stockchef.stockchefback.model.inventory.Unite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests TDD pour UniteConversionService
 * Phase RED - Tests pour les conversions d'unités
 */
@DisplayName("UniteConversionService - Tests TDD")
class UniteConversionServiceTest {
    
    private UniteConversionService uniteConversionService;
    
    @BeforeEach
    void setUp() {
        uniteConversionService = new UniteConversionService();
    }
    
    @ParameterizedTest
    @CsvSource({
        "1.0, KILOGRAMME, GRAMME, 1000.0",
        "2.5, KILOGRAMME, GRAMME, 2500.0",
        "0.5, KILOGRAMME, GRAMME, 500.0",
        "1500.0, GRAMME, KILOGRAMME, 1.5",
        "500.0, GRAMME, KILOGRAMME, 0.5"
    })
    @DisplayName("Should convert weight units correctly")
    void shouldConvertWeightUnitsCorrectly(String quantiteStr, Unite source, Unite cible, String expectedStr) {
        // Given
        BigDecimal quantite = new BigDecimal(quantiteStr);
        BigDecimal expected = new BigDecimal(expectedStr);
        
        // When
        BigDecimal result = uniteConversionService.convertir(quantite, source, cible);
        
        // Then
        assertThat(result).isEqualTo(expected);
    }
    
    @ParameterizedTest
    @CsvSource({
        "1.0, LITRE, MILLILITRE, 1000.0",
        "2.5, LITRE, MILLILITRE, 2500.0",
        "0.25, LITRE, MILLILITRE, 250.0",
        "1500.0, MILLILITRE, LITRE, 1.5",
        "750.0, MILLILITRE, LITRE, 0.75"
    })
    @DisplayName("Should convert volume units correctly")
    void shouldConvertVolumeUnitsCorrectly(String quantiteStr, Unite source, Unite cible, String expectedStr) {
        // Given
        BigDecimal quantite = new BigDecimal(quantiteStr);
        BigDecimal expected = new BigDecimal(expectedStr);
        
        // When
        BigDecimal result = uniteConversionService.convertir(quantite, source, cible);
        
        // Then
        assertThat(result).isEqualTo(expected);
    }
    
    @Test
    @DisplayName("Should return same value for identical units")
    void shouldReturnSameValueForIdenticalUnits() {
        // Given
        BigDecimal quantite = new BigDecimal("5.5");
        
        // When & Then
        assertThat(uniteConversionService.convertir(quantite, Unite.KILOGRAMME, Unite.KILOGRAMME))
                .isEqualTo(quantite);
        assertThat(uniteConversionService.convertir(quantite, Unite.LITRE, Unite.LITRE))
                .isEqualTo(quantite);
        assertThat(uniteConversionService.convertir(quantite, Unite.UNITE, Unite.UNITE))
                .isEqualTo(quantite);
    }
    
    @Test
    @DisplayName("Should handle count units without conversion")
    void shouldHandleCountUnitsWithoutConversion() {
        // Given
        BigDecimal quantite = new BigDecimal("10");
        
        // When & Then
        assertThat(uniteConversionService.convertir(quantite, Unite.UNITE, Unite.PIECE))
                .isEqualTo(quantite);
        assertThat(uniteConversionService.convertir(quantite, Unite.PIECE, Unite.UNITE))
                .isEqualTo(quantite);
    }
    
    @Test
    @DisplayName("Should throw exception when converting incompatible units")
    void shouldThrowExceptionWhenConvertingIncompatibleUnits() {
        // Given
        BigDecimal quantite = new BigDecimal("1.0");
        
        // When & Then
        assertThatThrownBy(() -> uniteConversionService.convertir(quantite, Unite.KILOGRAMME, Unite.LITRE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Impossible de convertir")
                .hasMessageContaining("KILOGRAMME")
                .hasMessageContaining("LITRE");
        
        assertThatThrownBy(() -> uniteConversionService.convertir(quantite, Unite.GRAMME, Unite.UNITE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Impossible de convertir")
                .hasMessageContaining("GRAMME")
                .hasMessageContaining("UNITE");
        
        assertThatThrownBy(() -> uniteConversionService.convertir(quantite, Unite.LITRE, Unite.PIECE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Impossible de convertir")
                .hasMessageContaining("LITRE")
                .hasMessageContaining("PIECE");
    }
    
    @Test
    @DisplayName("Should throw exception for null parameters")
    void shouldThrowExceptionForNullParameters() {
        // Given
        BigDecimal quantite = new BigDecimal("1.0");
        
        // When & Then
        assertThatThrownBy(() -> uniteConversionService.convertir(null, Unite.KILOGRAMME, Unite.GRAMME))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La quantité ne peut pas être nulle");
        
        assertThatThrownBy(() -> uniteConversionService.convertir(quantite, null, Unite.GRAMME))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Les unités ne peuvent pas être nulles");
        
        assertThatThrownBy(() -> uniteConversionService.convertir(quantite, Unite.KILOGRAMME, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Les unités ne peuvent pas être nulles");
    }
    
    @Test
    @DisplayName("Should throw exception for negative quantities")
    void shouldThrowExceptionForNegativeQuantities() {
        // Given
        BigDecimal quantiteNegative = new BigDecimal("-1.0");
        
        // When & Then
        assertThatThrownBy(() -> uniteConversionService.convertir(quantiteNegative, Unite.KILOGRAMME, Unite.GRAMME))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La quantité doit être positive ou nulle");
    }
    
    @Test
    @DisplayName("Should handle zero quantity")
    void shouldHandleZeroQuantity() {
        // Given
        BigDecimal zero = BigDecimal.ZERO;
        
        // When
        BigDecimal result = uniteConversionService.convertir(zero, Unite.KILOGRAMME, Unite.GRAMME);
        
        // Then
        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }
    
    @Test
    @DisplayName("Should handle very small quantities with precision")
    void shouldHandleVerySmallQuantitiesWithPrecision() {
        // Given - Test pour s'assurer que la précision est maintenue
        BigDecimal smallQuantity = new BigDecimal("0.001"); // 1 gramme
        
        // When
        BigDecimal result = uniteConversionService.convertir(smallQuantity, Unite.KILOGRAMME, Unite.GRAMME);
        
        // Then
        assertThat(result).isEqualTo(new BigDecimal("1.000"));
    }
    
    @Test
    @DisplayName("Should check if units are compatible")
    void shouldCheckIfUnitsAreCompatible() {
        // When & Then - Test des méthodes utilitaires
        assertThat(uniteConversionService.sontCompatibles(Unite.KILOGRAMME, Unite.GRAMME)).isTrue();
        assertThat(uniteConversionService.sontCompatibles(Unite.LITRE, Unite.MILLILITRE)).isTrue();
        assertThat(uniteConversionService.sontCompatibles(Unite.UNITE, Unite.PIECE)).isTrue();
        
        assertThat(uniteConversionService.sontCompatibles(Unite.KILOGRAMME, Unite.LITRE)).isFalse();
        assertThat(uniteConversionService.sontCompatibles(Unite.GRAMME, Unite.UNITE)).isFalse();
        assertThat(uniteConversionService.sontCompatibles(Unite.MILLILITRE, Unite.PIECE)).isFalse();
    }
}