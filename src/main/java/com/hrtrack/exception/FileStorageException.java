package com.hrtrack.exception;

/**
 * Exception levée lors d'erreurs liées au stockage de fichiers.
 */
public class FileStorageException extends RuntimeException {
    
    public FileStorageException(String message) {
        super(message);
    }
    
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}

