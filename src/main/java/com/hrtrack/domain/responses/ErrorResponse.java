package com.hrtrack.domain.responses;

import lombok.Builder;

/**
 * Réponse standard pour les erreurs de l'application.
 */
@Builder
public record ErrorResponse(
        boolean success,
        String message
) {
}