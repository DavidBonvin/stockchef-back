package com.stockchef.stockchefback.exception;

/**
 * Exception lancée lorsqu'un utilisateur tente d'effectuer
 * une action pour laquelle il n'a pas les permissions suffisantes
 */
public class InsufficientPermissionsException extends RuntimeException {
    
    public InsufficientPermissionsException(String message) {
        super(message);
    }
    
    public InsufficientPermissionsException(String message, Throwable cause) {
        super(message, cause);
    }
}