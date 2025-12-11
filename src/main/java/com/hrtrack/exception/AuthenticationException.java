package com.hrtrack.exception;

import com.hrtrack.domain.responses.auth.AuthenticationResponse;
import com.hrtrack.domain.responses.auth.ForgotPasswordResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AuthenticationException extends Exception {

    /**
     * Gère les échecs d'authentification (mauvais email/mot de passe).
     */
    @ExceptionHandler({BadCredentialsException.class})
    public ResponseEntity<AuthenticationResponse> handleAuthenticationException(
            BadCredentialsException ex) {

        AuthenticationResponse response = AuthenticationResponse.builder()
                .success(false)
                .message("Échec de la connexion. Email ou mot de passe incorrect.")
                .token(null)
                .build();

        return new ResponseEntity<AuthenticationResponse>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Gère les comptes non validés (DisabledException).
     * Ceci est levé par Spring Security car isEnabled() dans votre User renvoie 'false'.
     */
    @ExceptionHandler({DisabledException.class})
    public ResponseEntity<AuthenticationResponse> handleDisabledException(
            DisabledException ex) {

        AuthenticationResponse response = AuthenticationResponse.builder()
                .success(false)
                .message("Compte non actif. Veuillez attendre la validation par l'administrateur.")
                .token(null)
                .build();

        return new ResponseEntity<AuthenticationResponse>(response, HttpStatus.FORBIDDEN);
    }

    /**
     * Gère les tokens de réinitialisation invalides ou expirés.
     */
    @ExceptionHandler({InvalidTokenException.class})
    public ResponseEntity<ForgotPasswordResponse> handleInvalidTokenException(
            InvalidTokenException ex) {

        ForgotPasswordResponse response = ForgotPasswordResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<ForgotPasswordResponse>(response, HttpStatus.BAD_REQUEST);
    }
}
