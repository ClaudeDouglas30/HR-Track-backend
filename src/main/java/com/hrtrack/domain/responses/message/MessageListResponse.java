package com.hrtrack.domain.responses.message;

import com.hrtrack.domain.dtos.MessageDto;
import lombok.*;

import java.util.List;

/**
 * Réponse pour une liste de messages
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageListResponse {
    private boolean success;
    private String message;
    private List<MessageDto> messages;
}