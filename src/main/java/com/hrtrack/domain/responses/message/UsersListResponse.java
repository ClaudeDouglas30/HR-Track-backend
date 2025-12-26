package com.hrtrack.domain.responses.message;

import com.hrtrack.domain.dtos.UserSimpleDto;
import lombok.*;

import java.util.List;

/**
 * Réponse pour la liste de tous les utilisateurs
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsersListResponse {
    private boolean success;
    private String message;
    private List<UserSimpleDto> users;
}