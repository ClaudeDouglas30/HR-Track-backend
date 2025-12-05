package com.hrtrack.domain.requests.auth;

public record AuthenticationRequest(
        String email,
        String password
) {
}
