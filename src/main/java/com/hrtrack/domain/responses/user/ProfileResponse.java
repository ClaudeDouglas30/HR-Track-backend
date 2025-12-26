package com.hrtrack.domain.responses.user;

import com.hrtrack.domain.dtos.UserDto;
import lombok.Builder;

@Builder
public record ProfileResponse(
        Boolean success,
        String message,
        UserDto user
) {
}