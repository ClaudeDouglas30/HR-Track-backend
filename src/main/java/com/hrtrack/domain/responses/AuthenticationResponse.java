package com.hrtrack.domain.responses;

import lombok.Builder;

@Builder
public record AuthenticationResponse(
        String token
) {
}
