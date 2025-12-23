package com.hrtrack.domain.responses.conge;

import com.hrtrack.domain.dtos.JustificatifDto;
import lombok.Builder;

@Builder
public record JustificatifResponse(
        boolean success,
        String message,
        JustificatifDto justificatif
) {
}

