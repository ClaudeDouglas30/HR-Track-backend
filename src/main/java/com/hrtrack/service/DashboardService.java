package com.hrtrack.service;

import com.hrtrack.domain.dtos.DashboardDto;
import com.hrtrack.domain.dtos.RHDashboardDto;

/**
 * Service pour la gestion du dashboard administrateur et RH.
 */
public interface DashboardService {

    /**
     * Récupère toutes les données nécessaires pour le dashboard administrateur.
     * @return DashboardDto contenant toutes les statistiques et données pour les graphiques
     */
    DashboardDto getAdminDashboardData();

    /**
     * Récupère toutes les données nécessaires pour le dashboard RH.
     * @return RHDashboardDto contenant toutes les statistiques et données pour les graphiques
     */
    RHDashboardDto getRHDashboardData();
}

