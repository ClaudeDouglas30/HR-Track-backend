package com.hrtrack.domain.responses.typeConge;

import com.hrtrack.domain.dtos.TypeCongeDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Réponse standardisée pour une opération sur un type de congé.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TypeCongeResponse {
    private boolean success;
    private String message;
    private TypeCongeDto typeConge;
}

