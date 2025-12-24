package com.hrtrack.utils;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utilitaire pour générer des tokens sécurisés.
 */
public class TokenGenerator {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    /**
     * Génère un token sécurisé de 32 caractères (24 bytes en base64).
     * Le token est cryptographiquement sûr et URL-safe.
     * 
     * @return Un token de 32 caractères
     */
    public static String generateSecureToken() {
        // Générer 24 bytes aléatoires pour obtenir 32 caractères en base64 (sans padding)
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        
        // Encoder en base64 URL-safe sans padding pour obtenir exactement 32 caractères
        String token = base64Encoder.encodeToString(randomBytes);
        
        // S'assurer que le token fait exactement 32 caractères
        // Base64 encode 24 bytes = 32 caractères (sans padding)
        return token.substring(0, Math.min(32, token.length()));
    }

    /**
     * Génère un token alphanumérique de 32 caractères (alternative).
     * 
     * @return Un token de 32 caractères alphanumériques
     */
    public static String generateAlphanumericToken() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder token = new StringBuilder(32);
        
        for (int i = 0; i < 32; i++) {
            token.append(characters.charAt(secureRandom.nextInt(characters.length())));
        }
        
        return token.toString();
    }
}

