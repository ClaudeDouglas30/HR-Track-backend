package com.hrtrack.domain.dtos;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.hrtrack.domain.CongeStatus;
import com.hrtrack.domain.serializers.CongeStatusSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO pour les demandes de congés utilisées dans le frontend RH/ADMIN.
 * Correspond à la structure LeaveRequest du frontend.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeaveRequestDto {
    private Long id;
    
    // Informations de l'employé
    private EmployeeInfoDto employe;
    
    // Informations du type de congé
    private TypeCongeDto typeConge;
    
    // Informations de la demande
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Integer duree; // Nombre de jours
    @JsonSerialize(using = CongeStatusSerializer.class)
    private CongeStatus statut; // Sérialisé en PENDING, APPROVED, REJECTED pour le frontend
    private LocalDateTime dateDemande;
    private String commentaire; // Commentaire de l'employé (optionnel)
    private String motifRefus; // Motif du refus (optionnel, seulement si refusé)
}

