package com.stockchef.stockchefback.exception;

/**
 * Excepción lanzada cuando un usuario no tiene autorización para realizar una operación
 * específica sobre un recurso (diferente de la autenticación básica)
 */
public class UnauthorizedUserException extends RuntimeException {

    public UnauthorizedUserException(String message) {
        super(message);
    }

    public UnauthorizedUserException(String message, Throwable cause) {
        super(message, cause);
    }
}