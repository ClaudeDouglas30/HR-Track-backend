package com.hrtrack.service;

import com.hrtrack.domain.entities.User;
import com.hrtrack.domain.requests.auth.AuthenticationRequest;
import com.hrtrack.domain.requests.auth.RegisterRequest;
import com.hrtrack.domain.responses.auth.AuthenticationResponse;
import com.hrtrack.domain.responses.auth.RefreshTokenResponse;
import com.hrtrack.exception.EmailingException;
import jakarta.mail.MessagingException;

public interface AuthenticationService {

    /**
     * Enregistre un nouvel utilisateur. Le compte sera non validé par défaut.
     * @param request Les données d'inscription de l'utilisateur.
     * @return L'objet User créé.
     * @throws MessagingException
     * @throws EmailingException
     */
    User register(RegisterRequest request) throws MessagingException, EmailingException;

    /**
     * Authentifie l'utilisateur, vérifie la validation du compte et génère un JWT.
     * @param request Les crédentiels de connexion.
     * @return L'objet de réponse contenant le JWT.
     */
    AuthenticationResponse authenticate(AuthenticationRequest request);


    /**
     * Permet de gérer la logique de génération d'un token
     * @param email
     */
    void createPasswordResetTokenForUser(String email, String platform) throws MessagingException, EmailingException;

    /**
     * Permet de reinitialiser un mot de passe
     * @param token
     * @param password
     * @throws MessagingException
     * @throws EmailingException
     */
    void resetPasswordForUser(String token, String password) throws MessagingException, EmailingException;


    RefreshTokenResponse refreshToken(String token);
}
