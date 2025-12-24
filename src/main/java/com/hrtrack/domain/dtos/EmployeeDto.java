package com.hrtrack.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour un employé (liste).
 * Contient les informations de base et les statistiques de base.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeDto {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private Integer nombreAbsences;
    private Integer nombreRetards;
}

