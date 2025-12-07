package com.hrtrack.domain.requests.auth;

public record ResetPasswordRequest(
        String token,
        String password
) {
}
