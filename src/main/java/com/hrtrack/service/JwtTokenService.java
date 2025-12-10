package com.hrtrack.service;

import com.hrtrack.domain.entities.User;

public interface JwtTokenService {

    /**
     * Permet de sauvegarder un token pour un utilisateur
     * @param user
     * @param jwtToken
     * @param refreshToken
     */
    void saveUserToken(User user, String jwtToken, String refreshToken);


    /**
     * Permet de revoker tous les tokens d'un utilisateurs
     * @param user
     */
    void revokeAllUserTokens(User user);

    /**
     * Verifie si un token est valide ou non
     *
     * @param jwtToken
     * @return
     */
    boolean isTokenValid(String jwtToken);


    /**
     * Verifie si un
     * @param refreshToken
     * @return
     */
    boolean isRefreshTokenValid(String refreshToken);


    /**
     * Permet de changer les tokens y compris le refreshToken
     *
     * @param user
     * @param jwtToken
     * @param refreshToken
     * @param  oldToken
     */
    void rotateUserTokens(User user, String jwtToken, String refreshToken, String oldToken);

}
