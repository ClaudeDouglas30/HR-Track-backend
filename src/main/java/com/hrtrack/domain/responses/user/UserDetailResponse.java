package com.hrtrack.domain.responses.user;

import com.hrtrack.domain.dtos.UserDetailDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Réponse standardisée pour les détails complets d'un utilisateur.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDetailResponse {
    private boolean success;
    private String message;
    private UserDetailDto user;
}

