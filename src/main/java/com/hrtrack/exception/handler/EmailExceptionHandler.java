package com.hrtrack.exception.handler;

import com.hrtrack.domain.responses.ErrorResponse;
import com.hrtrack.exception.EmailingException;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Gestionnaire des exceptions liées à l'envoi d'emails.
 * Intercepte les exceptions levées lors des opérations d'envoi d'emails.
 */
@Slf4j
@ControllerAdvice
public class EmailExceptionHandler {

    /**
     * Gère les erreurs d'envoi d'email personnalisées.
     */
    @ExceptionHandler({EmailingException.class})
    public ResponseEntity<ErrorResponse> handleEmailingException(EmailingException ex) {
        log.error("Erreur d'envoi d'email : {}", ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .success(false)
                        .message("Erreur lors de l'envoi de l'email : " + ex.getMessage())
                        .build());
    }

    /**
     * Gère les erreurs de messagerie JavaMail.
     */
    @ExceptionHandler({MessagingException.class})
    public ResponseEntity<ErrorResponse> handleMessagingException(MessagingException ex) {
        log.error("Erreur de messagerie : {}", ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .success(false)
                        .message("Erreur lors de l'envoi de l'email. Veuillez réessayer plus tard.")
                        .build());
    }
}