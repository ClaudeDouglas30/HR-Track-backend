package com.hrtrack.domain.responses.message;

import com.hrtrack.domain.dtos.UserSimpleDto;
import lombok.*;

import java.util.List;

/**
 * Réponse pour la liste des émetteurs
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendersListResponse {
    private boolean success;
    private String message;
    private List<UserSimpleDto> senders;
}