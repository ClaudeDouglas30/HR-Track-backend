package com.hrtrack.domain.responses.dashboard;

import com.hrtrack.domain.dtos.RHDashboardDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Réponse standardisée pour le dashboard RH.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RHDashboardResponse {
    private boolean success;
    private String message;
    private RHDashboardDto dashboard;
}

