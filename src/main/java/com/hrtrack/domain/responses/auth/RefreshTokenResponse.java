package com.hrtrack.domain.responses.auth;

import lombok.Builder;

@Builder
public record RefreshTokenResponse(
        boolean success,
        String message,
        String token,
        String refreshToken
) {
}