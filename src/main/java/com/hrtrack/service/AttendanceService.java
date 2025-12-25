package com.hrtrack.service;

import com.hrtrack.domain.dtos.AttendanceDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service pour la gestion des présences et absences des employés.
 */
public interface AttendanceService {

    /**
     * Récupère la liste des présences/absences de tous les employés.
     * @param date Date pour filtrer les pointages (optionnel, si null, utilise la date actuelle)
     * @return Liste des données de présence pour chaque employé
     */
    List<AttendanceDto> getAllAttendances(LocalDate date);

    /**
     * Récupère les statistiques globales des présences/absences.
     * @param date Date pour filtrer les pointages (optionnel, si null, utilise la date actuelle)
     * @return Map contenant les statistiques (totalAbsences, totalRetards, totalEmployees)
     */
    Map<String, Object> getStatistics(LocalDate date);
}

