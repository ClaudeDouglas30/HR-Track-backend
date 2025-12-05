package com.hrtrack.domain.requests.auth;


public record RegisterRequest(
        String nom,
        String prenom,
        String email,
        String telephone,
        String password
) {}
