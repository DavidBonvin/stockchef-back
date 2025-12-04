package com.stockchef.stockchefback.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

/**
 * Validador pour vérifier qu'une quantité est positive
 */
public class PositiveQuantityValidator implements ConstraintValidator<PositiveQuantity, BigDecimal> {
    
    @Override
    public void initialize(PositiveQuantity constraintAnnotation) {
        // Rien à initialiser
    }
    
    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Laisser @NotNull gérer les valeurs nulles
        }
        
        return value.compareTo(BigDecimal.ZERO) > 0;
    }
}