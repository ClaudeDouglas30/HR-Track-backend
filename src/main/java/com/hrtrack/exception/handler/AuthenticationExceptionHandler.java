package com.hrtrack.exception.handler;

import com.hrtrack.domain.responses.auth.AuthenticationResponse;
import com.hrtrack.domain.responses.auth.ForgotPasswordResponse;
import com.hrtrack.exception.InvalidTokenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Gestionnaire des exceptions liées à l'authentification.
 * Intercepte les exceptions levées lors des opérations d'authentification, de connexion et de réinitialisation de mot de passe.
 */
@Slf4j
@ControllerAdvice
public class AuthenticationExceptionHandler {

    /**
     * Gère les échecs d'authentification (mauvais email/mot de passe).
     */
    @ExceptionHandler({BadCredentialsException.class})
    public ResponseEntity<AuthenticationResponse> handleBadCredentialsException(BadCredentialsException ex) {
        log.warn("Échec d'authentification : {}", ex.getMessage());
        
        AuthenticationResponse response = AuthenticationResponse.builder()
                .success(false)
                .message("Échec de la connexion. Email ou mot de passe incorrect.")
                .token(null)
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Gère les comptes non validés (DisabledException).
     * Ceci est levé par Spring Security car isEnabled() dans User renvoie 'false'.
     */
    @ExceptionHandler({DisabledException.class})
    public ResponseEntity<AuthenticationResponse> handleDisabledException(DisabledException ex) {
        log.warn("Tentative de connexion avec un compte désactivé : {}", ex.getMessage());
        
        AuthenticationResponse response = AuthenticationResponse.builder()
                .success(false)
                .message("Compte non actif. Veuillez attendre la validation par l'administrateur.")
                .token(null)
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Gère les tokens de réinitialisation invalides ou expirés.
     */
    @ExceptionHandler({InvalidTokenException.class})
    public ResponseEntity<ForgotPasswordResponse> handleInvalidTokenException(InvalidTokenException ex) {
        log.warn("Token invalide ou expiré : {}", ex.getMessage());
        
        ForgotPasswordResponse response = ForgotPasswordResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Gère les utilisateurs non trouvés.
     */
    @ExceptionHandler({UsernameNotFoundException.class})
    public ResponseEntity<AuthenticationResponse> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        log.warn("Utilisateur non trouvé : {}", ex.getMessage());

        AuthenticationResponse response = AuthenticationResponse.builder()
                .success(false)
                .message("Utilisateur non trouvé. Veuillez vérifier vos identifiants.")
                .token(null)
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}