package com.stockchef.stockchefback.exception;

/**
 * Exception lancée lorsqu'un utilisateur n'a pas l'autorisation pour effectuer une opération
 * spécifique sur une ressource (différent de l'authentification de base)
 */
public class UnauthorizedUserException extends RuntimeException {

    public UnauthorizedUserException(String message) {
        super(message);
    }

    public UnauthorizedUserException(String message, Throwable cause) {
        super(message, cause);
    }
}