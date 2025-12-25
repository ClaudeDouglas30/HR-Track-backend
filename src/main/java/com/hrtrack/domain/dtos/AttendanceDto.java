package com.hrtrack.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour les données de présence/absence d'un employé.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceDto {
    private Long employeeId;
    private String nom;
    private String prenom;
    private String email;
    private Integer nombreAbsences;
    private Integer nombreRetards;
}

