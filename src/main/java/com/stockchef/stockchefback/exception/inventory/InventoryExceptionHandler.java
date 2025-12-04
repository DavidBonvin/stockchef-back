package com.stockchef.stockchefback.exception.inventory;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestionnaire global des exceptions pour l'API Inventory
 * Priorité plus élevée que GlobalExceptionHandler pour les contrôleurs inventory
 */
@RestControllerAdvice(basePackages = "com.stockchef.stockchefback.controller.inventory")
@Order(1) // Priorité élevée
public class InventoryExceptionHandler {
    
    /**
     * Gestion des erreurs de validation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = new ArrayList<>();
        
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
            "Erreurs de validation",
            "Les données fournies ne sont pas valides",
            HttpStatus.BAD_REQUEST.value(),
            LocalDateTime.now(),
            errors
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Gestion des erreurs de produit non trouvé
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleProduitNotFound(IllegalArgumentException ex) {
        
        // Vérifier si c'est une erreur de produit non trouvé
        if (ex.getMessage().contains("Produit non trouvé")) {
            ErrorResponse errorResponse = new ErrorResponse(
                "Produit non trouvé",
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now(),
                List.of()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        // Autres erreurs d'arguments (validation métier)
        ErrorResponse errorResponse = new ErrorResponse(
            "Données invalides",
            ex.getMessage(),
            HttpStatus.BAD_REQUEST.value(),
            LocalDateTime.now(),
            List.of()
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Gestion des erreurs de stock insuffisant
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleStockInsuffisant(IllegalStateException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            "Opération impossible",
            ex.getMessage(),
            HttpStatus.BAD_REQUEST.value(),
            LocalDateTime.now(),
            List.of()
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Gestion des erreurs générales
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
            "Erreur interne",
            "Une erreur inattendue s'est produite",
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            LocalDateTime.now(),
            List.of(ex.getMessage())
        );
        
        return ResponseEntity.internalServerError().body(errorResponse);
    }
    
    /**
     * DTO pour les réponses d'erreur
     */
    public record ErrorResponse(
        String title,
        String message,
        int status,
        LocalDateTime timestamp,
        List<String> errors
    ) {}
}