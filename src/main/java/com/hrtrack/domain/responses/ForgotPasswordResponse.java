package com.hrtrack.domain.responses;


import lombok.Builder;

@Builder
public record ForgotPasswordResponse(
        boolean success,
        String message
) {
}
