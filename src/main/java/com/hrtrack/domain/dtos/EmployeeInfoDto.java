package com.hrtrack.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO simplifié pour les informations de l'employé dans une demande de congé.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeInfoDto {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private Integer nombreAbsences;
    private Integer nombreRetards;
}

