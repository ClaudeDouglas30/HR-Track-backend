package com.hrtrack.domain.responses.conge;

import com.hrtrack.domain.dtos.JustificatifDto;
import lombok.Builder;

import java.util.List;

@Builder
public record ListJustificatifResponse(
        boolean success,
        String message,
        List<JustificatifDto> justificatifs
) {
}

