package com.hrtrack.domain.responses;

public record RegisterResponse(
        boolean success,
        String message
) {
}
