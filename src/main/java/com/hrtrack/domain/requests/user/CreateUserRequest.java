package com.hrtrack.domain.requests.user;

import com.hrtrack.domain.Role;

/**
 * Requête pour créer un nouvel utilisateur (par l'admin).
 */
public record CreateUserRequest(
        String nom,
        String prenom,
        String email,
        String telephone,
        String password,
        Role role,
        String photoProfil
) {
}

