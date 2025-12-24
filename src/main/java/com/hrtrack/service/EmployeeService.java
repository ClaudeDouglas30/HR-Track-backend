package com.hrtrack.service;

import com.hrtrack.domain.dtos.EmployeeDto;
import com.hrtrack.domain.dtos.EmployeeDetailDto;

import java.util.List;
import java.util.Map;

/**
 * Service pour la gestion des employés.
 */
public interface EmployeeService {

    /**
     * Récupère la liste de tous les employés (utilisateurs avec le rôle EMPLOYEE).
     * @return Liste de tous les employés avec leurs statistiques de base
     */
    List<EmployeeDto> getAllEmployees();

    /**
     * Récupère les détails complets d'un employé par son ID.
     * @param employeeId L'ID de l'employé
     * @return Les détails complets de l'employé avec statistiques et graphiques
     */
    EmployeeDetailDto getEmployeeById(Long employeeId);

    /**
     * Récupère les statistiques globales des employés.
     * @return Map contenant les statistiques (total, absences, retards, moyennes, top 5, etc.)
     */
    Map<String, Object> getStatistics();
}

