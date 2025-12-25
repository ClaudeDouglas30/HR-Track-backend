package com.hrtrack.domain.responses.attendance;

import com.hrtrack.domain.dtos.AttendanceDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Réponse standardisée pour une opération sur une présence.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceResponse {
    private boolean success;
    private String message;
    private AttendanceDto attendance;
}

