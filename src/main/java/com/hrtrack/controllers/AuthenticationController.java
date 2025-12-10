package com.hrtrack.controllers;



import com.hrtrack.domain.entities.User;
import com.hrtrack.domain.requests.auth.*;
import com.hrtrack.domain.responses.AuthenticationResponse;
import com.hrtrack.domain.responses.ForgotPasswordResponse;
import com.hrtrack.domain.responses.RefreshTokenResponse;
import com.hrtrack.domain.responses.RegisterResponse;
import com.hrtrack.exception.EmailingException;
import com.hrtrack.service.AuthenticationService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



/**
 * Cette classe contient le controller d'authentification,
 * d'inscription, de réinitialisation de mot de passe
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) throws MessagingException, EmailingException {
        User user = authenticationService.register(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new RegisterResponse(
                            false,
                            "Votre inscription a échoué, veuillez ressayer."
                    )
            );
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new RegisterResponse(
                        true,
                        "Inscription réussie. Compte en attente de validation par l'administrateur."
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
        AuthenticationResponse authenticationResponse = authenticationService.authenticate(request);
        return ResponseEntity.ok(authenticationResponse);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) throws MessagingException, EmailingException {

        String email = request.email();
        authenticationService.createPasswordResetTokenForUser(email);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ForgotPasswordResponse(
                        true,
                        "Un email vous a été envoyé"
                )
        );
    }


    @PostMapping("/reset-password")
    public ResponseEntity<ForgotPasswordResponse> resetPassword(@RequestBody ResetPasswordRequest request) 
            throws MessagingException, EmailingException {

        String token = request.token();
        String password = request.password();

        authenticationService.resetPasswordForUser(token, password);
        return ResponseEntity.ok(
                new ForgotPasswordResponse(
                        true,
                        "Votre mot de passe a été réinitialisé avec succès."
                )
        );
    }


    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponse> refreshToken(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Header Authorization invalide ou manquant");
        }

        String token = authHeader.substring(7);
        RefreshTokenResponse response = authenticationService.refreshToken(token);

        return ResponseEntity.status(response.success() ? HttpStatus.CREATED : HttpStatus.UNAUTHORIZED)
                .body(
                        response
        );
    }

}
