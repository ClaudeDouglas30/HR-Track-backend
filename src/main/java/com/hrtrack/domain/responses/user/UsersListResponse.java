package com.hrtrack.domain.responses.user;

import com.hrtrack.domain.dtos.UserDto;
import lombok.Builder;

import java.util.List;

@Builder
public record UsersListResponse(
        Boolean success,
        String message,
        List<UserDto> users
) {
}