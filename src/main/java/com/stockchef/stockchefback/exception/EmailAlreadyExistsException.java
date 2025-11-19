package com.stockchef.stockchefback.exception;

/**
 * Exception lancée lorsqu'un utilisateur tente de s'enregistrer
 * avec un email déjà existant dans la base de données
 */
public class EmailAlreadyExistsException extends RuntimeException {
    
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
    
    public EmailAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}