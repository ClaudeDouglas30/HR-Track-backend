package com.hrtrack.domain.requests.auth;

/**
 * Requête pour demander la réinitialisation du mot de passe.
 * 
 * @param email L'email de l'utilisateur
 * @param platform La plateforme (WEB ou MOBILE) - optionnel, par défaut WEB
 */
public record ForgotPasswordRequest(
        String email,
        String platform
) {
    public ForgotPasswordRequest(String email) {
        this(email, "WEB");
    }
}
