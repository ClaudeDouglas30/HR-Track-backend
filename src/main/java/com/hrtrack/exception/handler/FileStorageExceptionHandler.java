package com.hrtrack.exception.handler;

import com.hrtrack.domain.responses.ErrorResponse;
import com.hrtrack.exception.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Gestionnaire des exceptions liées au stockage de fichiers.
 * Intercepte les exceptions levées lors des opérations de stockage, lecture ou suppression de fichiers.
 */
@Slf4j
@ControllerAdvice
public class FileStorageExceptionHandler {

    /**
     * Gère les erreurs de stockage de fichiers.
     */
    @ExceptionHandler({FileStorageException.class})
    public ResponseEntity<ErrorResponse> handleFileStorageException(FileStorageException ex) {
        log.error("Erreur de stockage de fichier : {}", ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .success(false)
                        .message("Erreur lors du stockage du fichier : " + ex.getMessage())
                        .build());
    }
}