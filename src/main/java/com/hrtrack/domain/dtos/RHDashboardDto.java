package com.hrtrack.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO pour les données du dashboard RH.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RHDashboardDto {
    // Statistiques générales
    private Integer demandesEnAttente;
    private Integer demandesEnAttenteHier; // Pour calculer l'évolution
    private Integer totalEmployees;
    private Integer congesCeMois;
    private Integer congesCetteSemaine; // Pour calculer l'évolution
    private Integer pointagesAujourdhui;
    private Double pourcentagePresence; // Pourcentage de présence aujourd'hui

    // Données pour graphiques
    private List<Map<String, Object>> dailyAttendanceData; // [{jour: "Lun", absences: 8, retards: 12}, ...]
    private List<Map<String, Object>> leaveTypeDistribution; // [{name: "Congés annuels", value: 45, color: "#0366fc"}, ...]
    private List<Map<String, Object>> monthlyStats; // [{name: "Jan", demandes: 45, approuvées: 38}, ...]
}

