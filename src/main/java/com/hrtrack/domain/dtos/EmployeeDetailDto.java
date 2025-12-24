package com.hrtrack.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO enrichi pour les détails complets d'un employé.
 * Contient toutes les informations nécessaires pour la page de détails.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeDetailDto {
    // Informations de base
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;

    // Statistiques
    private Integer nombreAbsences;
    private Integer nombreRetards;
    private Double heuresTravaillees;
    private Double frequencePonctualite;

    // Données pour graphiques
    private List<Map<String, Object>> frequenceAbsences; // [{mois: "Jan", nombre: 2}, ...]
    private List<Map<String, Object>> frequenceRetards;   // [{mois: "Jan", nombre: 3}, ...]
}

