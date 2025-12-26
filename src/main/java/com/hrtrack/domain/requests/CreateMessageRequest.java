package com.hrtrack.domain.requests;

import lombok.*;

/**
 * Requête pour créer un nouveau message
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateMessageRequest {
    private Long receiverId;
    private String subject;
    private String content;
}