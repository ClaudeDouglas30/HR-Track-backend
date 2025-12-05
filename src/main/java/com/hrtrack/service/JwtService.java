package com.hrtrack.service;


import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;


import java.util.Map;
import java.util.function.Function;

@Service
public interface JwtService {

    /**
     * Extrait le nom d'utilisateur (Subject) du token.
     * @param token Le JWT
     * @return Le nom d'utilisateur
     */
    String extractUsername(String token);

    /**
     * Extrait une "claim" spécifique (par ex., expiration, subject, roles).
     * @param token Le JWT
     * @param claimsResolver La fonction pour résoudre la claim
     * @param <T> Le type de la claim
     * @return La valeur de la claim
     */
    <T> T extractClaim(String token, Function<io.jsonwebtoken.Claims, T> claimsResolver);

    /**
     * Génère un token avec des claims supplémentaires spécifiques.
     * @param extraClaims Claims personnalisées (par ex., roles)
     * @param userDetails Les détails de l'utilisateur
     * @return Le JWT généré
     */
    String generateToken(Map<String, Object> extraClaims, UserDetails userDetails);

    /**
     * Surcharge pour générer un token sans claims supplémentaires.
     * @param userDetails Les détails de l'utilisateur
     * @return Le JWT généré
     */
    String generateToken(UserDetails userDetails);

    /**
     * Vérifie si le token est valide (signature et expiration).
     * @param token Le JWT
     * @param userDetails Les détails de l'utilisateur
     * @return true si le token est valide, false sinon
     */
    boolean isTokenValid(String token, UserDetails userDetails);
}
