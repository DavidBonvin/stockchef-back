package com.stockchef.stockchefback.exception;

/**
 * Exception lancée lorsqu'un utilisateur n'est pas trouvé
 * dans la base de données
 */
public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}