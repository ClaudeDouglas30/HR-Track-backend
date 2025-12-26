package com.hrtrack.exception.handler;

import com.hrtrack.domain.responses.ErrorResponse;
import com.hrtrack.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Gestionnaire des exceptions de validation.
 * Intercepte les exceptions liées à la validation des données métier.
 */
@Slf4j
@ControllerAdvice
public class ValidationExceptionHandler {

    /**
     * Gère les erreurs de validation des données.
     */
    @ExceptionHandler({ValidationException.class})
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex) {
        log.warn("Erreur de validation : {}", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build());
    }
}