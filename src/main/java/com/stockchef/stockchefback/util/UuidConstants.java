package com.stockchef.stockchefback.util;

import java.util.UUID;

/**
 * Constantes UUID pour l'application
 * Utilisées pour validation et tests
 */
public final class UuidConstants {

    // UUIDs fixes pour les tests et validation
    public static final String USER_1_UUID = "550e8400-e29b-41d4-a716-446655440001";
    public static final String USER_2_UUID = "550e8400-e29b-41d4-a716-446655440002";
    public static final String USER_3_UUID = "550e8400-e29b-41d4-a716-446655440003";
    public static final String ADMIN_UUID = "550e8400-e29b-41d4-a716-446655440010";
    public static final String DEVELOPER_UUID = "550e8400-e29b-41d4-a716-446655440020";
    public static final String MODERATOR_UUID = "550e8400-e29b-41d4-a716-446655440030";

    private UuidConstants() {
        // Utility class - no instantiation
    }

    /**
     * Génère un UUID de test reproductible
     */
    public static String generateTestUuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * Valide qu'un String est un UUID valide
     */
    public static boolean isValidUuid(String uuid) {
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Crée un UUID à partir d'un nombre pour les tests
     */
    public static String createTestUuid(int number) {
        return String.format("550e8400-e29b-41d4-a716-44665544%04d", number);
    }
}