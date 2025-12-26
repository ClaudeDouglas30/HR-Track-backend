package com.hrtrack.domain.responses.conge;


import lombok.Builder;

@Builder
public record CreateResponse(
        Boolean success,
        String message
) {
}