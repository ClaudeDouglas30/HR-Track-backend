package com.hrtrack.domain.responses.auth;


import lombok.Builder;

@Builder
public record ForgotPasswordResponse(
        boolean success,
        String message
) {
}
