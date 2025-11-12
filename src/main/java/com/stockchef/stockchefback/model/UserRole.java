package com.stockchef.stockchefback.model;

/**
 * Roles de usuario en el sistema StockChef
 */
public enum UserRole {
    /**
     * Desarrollador - Super admin con acceso completo al sistema
     */
    ROLE_DEVELOPER,
    
    /**
     * Administrador - Gestión completa del restaurante
     */
    ROLE_ADMIN,
    
    /**
     * Chef - Acceso a inventario, recetas y planificación de menús
     */
    ROLE_CHEF,
    
    /**
     * Empleado - Acceso básico a consulta de inventario
     */
    ROLE_EMPLOYEE
}