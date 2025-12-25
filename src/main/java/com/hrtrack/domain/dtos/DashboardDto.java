package com.hrtrack.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO pour les données du dashboard administrateur.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardDto {
    // Statistiques générales
    private Integer totalUsers;
    private Integer totalAdmins;
    private Integer totalRH;
    private Integer totalEmployees;
    private Integer newUsersThisMonth; // Nouveaux utilisateurs ce mois

    // Données pour graphiques
    private List<Map<String, Object>> userRoleDistribution; // [{name: "Employés", value: 234, color: "#0366fc"}, ...]
    private List<Map<String, Object>> dailyAttendanceData; // [{jour: "Lun", absences: 8, retards: 12}, ...]
}

