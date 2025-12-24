package com.hrtrack.domain.responses.employee;

import com.hrtrack.domain.dtos.EmployeeDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Réponse standardisée pour les statistiques des employés.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeStatisticsResponse {
    private boolean success;
    private String message;
    private Map<String, Object> statistics;
}

