package com.hrtrack.service;

import com.hrtrack.domain.dtos.DashboardDto;

/**
 * Service pour la gestion du dashboard administrateur.
 */
public interface DashboardService {

    /**
     * Récupère toutes les données nécessaires pour le dashboard administrateur.
     * @return DashboardDto contenant toutes les statistiques et données pour les graphiques
     */
    DashboardDto getAdminDashboardData();
}

