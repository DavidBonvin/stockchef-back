package com.stockchef.stockchefback.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour valider qu'une quantité est positive
 */
@Documented
@Constraint(validatedBy = PositiveQuantityValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PositiveQuantity {
    String message() default "La quantité doit être positive";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}