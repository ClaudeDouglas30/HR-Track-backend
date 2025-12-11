package com.hrtrack.domain.responses.conge;

import com.hrtrack.domain.dtos.CongeDto;
import lombok.Builder;

@Builder
public record CongeResponse(
        boolean success,
        CongeDto conge
) {
}
