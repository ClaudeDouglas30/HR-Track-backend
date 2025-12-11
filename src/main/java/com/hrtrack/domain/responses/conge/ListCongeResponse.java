package com.hrtrack.domain.responses.conge;

import com.hrtrack.domain.dtos.CongeDto;
import lombok.Builder;

import java.util.List;


@Builder
public record ListCongeResponse(
        boolean success,
        List<CongeDto> conges
) {
}
