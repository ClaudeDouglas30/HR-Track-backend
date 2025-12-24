package com.hrtrack.domain.responses.user;

import lombok.Builder;

import java.util.Map;

/**
 * Réponse pour les statistiques des utilisateurs.
 */
@Builder
public record StatisticsResponse(
        boolean success,
        String message,
        Map<String, Object> statistics
) {
}

