package com.hrtrack.service;

import com.hrtrack.domain.entities.User;
import com.hrtrack.domain.requests.AuthenticationRequest;
import com.hrtrack.domain.requests.RegisterRequest;
import com.hrtrack.domain.responses.AuthenticationResponse;

public interface AuthService {

    /**
     * Enregistre un nouvel utilisateur. Le compte sera non validé par défaut.
     * @param request Les données d'inscription de l'utilisateur.
     * @return L'objet User créé.
     */
    User register(RegisterRequest request);

    /**
     * Authentifie l'utilisateur, vérifie la validation du compte et génère un JWT.
     * @param request Les crédentiels de connexion.
     * @return L'objet de réponse contenant le JWT.
     */
    AuthenticationResponse authenticate(AuthenticationRequest request);
}
