package com.hrtrack.domain.responses.auth;

public record RegisterResponse(
        boolean success,
        String message
) {
}
