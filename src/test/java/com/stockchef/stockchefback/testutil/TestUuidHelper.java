package com.stockchef.stockchefback.testutil;

import com.stockchef.stockchefback.util.UuidConstants;

/**
 * Utilités pour les tests UUID
 * Fournit des UUIDs fixes pour les tests reproductibles
 */
public class TestUuidHelper {

    // UUIDs fixes pour les tests reproductibles - délégués vers UuidConstants
    public static final String USER_1_UUID = UuidConstants.USER_1_UUID;
    public static final String USER_2_UUID = UuidConstants.USER_2_UUID;
    public static final String USER_3_UUID = UuidConstants.USER_3_UUID;
    public static final String ADMIN_UUID = UuidConstants.ADMIN_UUID;
    public static final String DEVELOPER_UUID = UuidConstants.DEVELOPER_UUID;
    public static final String MODERATOR_UUID = UuidConstants.MODERATOR_UUID;

    /**
     * Génère un UUID de test reproductible
     */
    public static String generateTestUuid() {
        return UuidConstants.generateTestUuid();
    }

    /**
     * Valide qu'un String est un UUID valide
     */
    public static boolean isValidUuid(String uuid) {
        return UuidConstants.isValidUuid(uuid);
    }

    /**
     * Crée un UUID à partir d'un nombre pour les tests
     */
    public static String createTestUuid(int number) {
        return UuidConstants.createTestUuid(number);
    }
}