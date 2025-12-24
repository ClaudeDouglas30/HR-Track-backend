package com.hrtrack.exception.handler;

import com.hrtrack.domain.responses.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Gestionnaire global des exceptions non gérées.
 * Intercepte toutes les exceptions non gérées par les handlers spécifiques.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gère les arguments invalides.
     */
    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Argument invalide : {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build());
    }

    /**
     * Gère les exceptions génériques non gérées.
     * Cette méthode doit être la dernière pour attraper toutes les exceptions non gérées.
     */
    @ExceptionHandler({Exception.class})
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Exception non gérée : {}", ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .success(false)
                        .message("Une erreur interne s'est produite. Veuillez réessayer plus tard.")
                        .build());
    }
}

