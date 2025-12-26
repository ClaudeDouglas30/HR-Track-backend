package com.hrtrack.domain.responses.message;

import lombok.*;

/**
 * Réponse pour le nombre de messages non lus
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UnreadCountResponse {
    private boolean success;
    private String message;
    private int count;
}