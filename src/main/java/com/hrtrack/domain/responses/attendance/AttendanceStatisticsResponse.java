package com.hrtrack.domain.responses.attendance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Réponse standardisée pour les statistiques de présence.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceStatisticsResponse {
    private boolean success;
    private String message;
    private Map<String, Object> statistics;
}

