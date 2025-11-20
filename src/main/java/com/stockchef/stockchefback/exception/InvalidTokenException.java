package com.stockchef.stockchefback.exception;

/**
 * Excepción para tokens JWT inválidos
 */
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
    
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}