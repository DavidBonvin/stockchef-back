package com.stockchef.stockchefback.exception;

/**
 * Exception pour tokens JWT invalides
 */
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
    
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}