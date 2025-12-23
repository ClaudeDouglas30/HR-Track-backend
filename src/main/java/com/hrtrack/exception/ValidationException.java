package com.hrtrack.exception;

/**
 * Exception levée lorsqu'une validation de données échoue.
 * Contient un message d'erreur décrivant la raison de l'échec de la validation.
 */
public class ValidationException extends RuntimeException {
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

