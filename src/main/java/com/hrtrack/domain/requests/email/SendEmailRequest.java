package com.hrtrack.domain.requests.email;

import lombok.Builder;


@Builder
public record SendEmailRequest(
        String to,
        String subject,
        String message,
        String nom,
        String prenom
){
}
