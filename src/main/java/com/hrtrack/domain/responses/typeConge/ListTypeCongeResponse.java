package com.hrtrack.domain.responses.typeConge;

import com.hrtrack.domain.dtos.TypeCongeDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Réponse standardisée pour la liste des types de congés.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ListTypeCongeResponse {
    private boolean success;
    private String message;
    private List<TypeCongeDto> typeConges;
    private Integer total;
}

