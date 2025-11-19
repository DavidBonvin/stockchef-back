package com.stockchef.stockchefback.service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.UUID;

/**
 * Service pour génération et gestion des UUID
 * Utilise UUID v4 (aléatoire) pour maximum de sécurité
 */
@Service
@Slf4j
public class UuidService {
    
    /**
     * Génère un nouvel UUID unique
     * @return UUID sous forme de String
     */
    public String generateUuid() {
        String uuid = UUID.randomUUID().toString();
        log.info("UUID generado: {}", uuid);
        return uuid;
    }
    
    /**
     * Valide qu'une chaîne est un UUID valide
     * @param uuidString la chaîne à valider
     * @return true si c'est un UUID valide
     */
    public boolean isValidUuid(String uuidString) {
        if (uuidString == null || uuidString.trim().isEmpty()) {
            return false;
        }
        
        try {
            UUID.fromString(uuidString.trim());
            return true;
        } catch (IllegalArgumentException e) {
            log.warn("UUID invalide: {}", uuidString);
            return false;
        }
    }
    
    /**
     * Convertit un UUID String en objet UUID
     * @param uuidString la chaîne UUID
     * @return objet UUID
     * @throws IllegalArgumentException si l'UUID n'est pas valide
     */
    public UUID parseUuid(String uuidString) {
        return UUID.fromString(uuidString);
    }
}