package com.hrtrack.domain.requests.auth;


import lombok.Builder;

@Builder
public record RefreshTokenRequest(
        String refreshToken
) {
}
