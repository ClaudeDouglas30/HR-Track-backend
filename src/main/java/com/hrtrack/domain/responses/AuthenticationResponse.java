package com.hrtrack.domain.responses;

import com.hrtrack.domain.dtos.UserDto;
import lombok.Builder;

@Builder
public record AuthenticationResponse(
        boolean success,
        String message,
        String token,
        String refreshToken,
        UserDto user
) {
}
