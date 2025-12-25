package com.hrtrack.controllers;

import com.hrtrack.domain.responses.dashboard.DashboardResponse;
import com.hrtrack.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur pour le dashboard administrateur.
 * 
 * Endpoints disponibles :
 * - GET /api/v1/dashboard/admin : Récupère toutes les données du dashboard admin
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Récupère toutes les données nécessaires pour le dashboard administrateur.
     * Accessible par ADMIN uniquement.
     * 
     * @return DashboardResponse contenant toutes les statistiques et données pour les graphiques
     */
    @GetMapping("/admin")
    public ResponseEntity<DashboardResponse> getAdminDashboard() {
        try {
            var dashboardData = dashboardService.getAdminDashboardData();

            return ResponseEntity.status(HttpStatus.OK).body(
                    DashboardResponse.builder()
                            .success(true)
                            .message("Données du dashboard récupérées avec succès")
                            .dashboard(dashboardData)
                            .build()
            );
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des données du dashboard: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    DashboardResponse.builder()
                            .success(false)
                            .message("Erreur lors de la récupération des données du dashboard")
                            .dashboard(null)
                            .build()
            );
        }
    }
}

