package com.hrtrack.domain.responses.employee;

import com.hrtrack.domain.dtos.EmployeeDetailDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Réponse standardisée pour une opération sur un employé.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeResponse {
    private boolean success;
    private String message;
    private EmployeeDetailDto employee;
}

