package com.hrtrack.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO enrichi pour les détails complets d'un utilisateur.
 * Contient toutes les informations nécessaires pour la page de détails.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDetailDto {
    // Informations de base de l'utilisateur
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String photoProfil;
    private String telephone;
    private com.hrtrack.domain.Role role;
    private boolean accountValidated;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Statistiques mensuelles
    private Integer joursPresents;
    private Integer joursAbsents;
    private Integer retards;
    private Double heuresTravaillees;
    private Double frequencePonctualite;

    // Pointage de la semaine
    private List<PointageDto> pointageSemaine;

    // Évolution de la ponctualité sur 6 mois
    private List<Map<String, Object>> evolutionPonctualite;

    // Demandes de congés récentes
    private List<CongeDto> demandesConges;
}

