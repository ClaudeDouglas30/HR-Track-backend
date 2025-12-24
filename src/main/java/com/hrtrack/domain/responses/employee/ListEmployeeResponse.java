package com.hrtrack.domain.responses.employee;

import com.hrtrack.domain.dtos.EmployeeDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Réponse standardisée pour la liste des employés.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ListEmployeeResponse {
    private boolean success;
    private String message;
    private List<EmployeeDto> employees;
    private Integer total;
}

