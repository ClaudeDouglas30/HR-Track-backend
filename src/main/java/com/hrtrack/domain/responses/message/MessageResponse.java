package com.hrtrack.domain.responses.message;

import com.hrtrack.domain.dtos.MessageDto;
import lombok.*;

/**
 * Réponse pour une opération sur un message unique
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageResponse {
    private boolean success;
    private String message;
    private MessageDto data;
}