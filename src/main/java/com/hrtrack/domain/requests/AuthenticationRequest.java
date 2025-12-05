package com.hrtrack.domain.requests;

public record AuthenticationRequest(
        String email,
        String password
) {
}
