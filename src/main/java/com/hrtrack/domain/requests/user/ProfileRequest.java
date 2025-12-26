package com.hrtrack.domain.requests.user;

public record ProfileRequest(
        String nom,
        String prenom,
        String email,
        String telephone

) {
}