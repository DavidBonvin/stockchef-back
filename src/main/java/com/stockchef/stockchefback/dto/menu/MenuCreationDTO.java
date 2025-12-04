package com.stockchef.stockchefback.dto.menu;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO pour la création et mise à jour d'un menu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuCreationDTO {
    
    @NotBlank(message = "Le nom du menu est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String nom;
    
    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    private String description;
    
    @NotNull(message = "La date de service est obligatoire")
    @FutureOrPresent(message = "La date de service doit être aujourd'hui ou dans le futur")
    private LocalDate dateService;
    
    @NotNull(message = "Le prix de vente est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix de vente doit être positif")
    @Digits(integer = 8, fraction = 2, message = "Format de prix invalide (max 8 chiffres avant la virgule, 2 après)")
    private BigDecimal prixVente;
}