package com.hrtrack.domain.responses.user;

import com.hrtrack.domain.dtos.UserDto;
import lombok.Builder;

import java.util.List;

/**
 * Réponse pour une liste d'utilisateurs.
 */
@Builder
public record ListUserResponse(
        boolean success,
        String message,
        List<UserDto> users
) {
}

