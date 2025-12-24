package com.hrtrack.domain.responses.user;

import com.hrtrack.domain.dtos.UserDto;
import lombok.Builder;

/**
 * Réponse pour une opération sur un utilisateur.
 */
@Builder
public record UserResponse(
        boolean success,
        String message,
        UserDto user
) {
}

