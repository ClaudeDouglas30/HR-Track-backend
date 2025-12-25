package com.hrtrack.domain.responses.attendance;

import com.hrtrack.domain.dtos.AttendanceDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Réponse standardisée pour la liste des présences/absences.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ListAttendanceResponse {
    private boolean success;
    private String message;
    private List<AttendanceDto> attendances;
    private Integer total;
}

