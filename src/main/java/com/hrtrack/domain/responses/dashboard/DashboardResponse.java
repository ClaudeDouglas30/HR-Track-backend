package com.hrtrack.domain.responses.dashboard;

import com.hrtrack.domain.dtos.DashboardDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Réponse standardisée pour le dashboard.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardResponse {
    private boolean success;
    private String message;
    private DashboardDto dashboard;
}

