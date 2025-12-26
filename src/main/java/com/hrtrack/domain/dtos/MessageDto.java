package com.hrtrack.domain.dtos;

import lombok.*;

/**
 * DTO pour les messages
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageDto {
    private Long id;
    private String messageUuid;
    private UserSimpleDto sender;
    private UserSimpleDto receiver;
    private String subject;
    private String content;
    private String dateEnvoi;
    private boolean isRead;
    private String createdAt;
    private String updatedAt;
}