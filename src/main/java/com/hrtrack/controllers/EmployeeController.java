package com.hrtrack.controllers;

import com.hrtrack.domain.responses.employee.EmployeeResponse;
import com.hrtrack.domain.responses.employee.EmployeeStatisticsResponse;
import com.hrtrack.domain.responses.employee.ListEmployeeResponse;
import com.hrtrack.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour la gestion des employés.
 * 
 * Endpoints disponibles :
 * - GET /api/v1/employees/list : Liste tous les employés
 * - GET /api/v1/employees/{id} : Récupère les détails d'un employé
 * - GET /api/v1/employees/statistics : Récupère les statistiques globales
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * Récupère la liste de tous les employés.
     * Accessible par ADMIN et RH.
     */
    @GetMapping("/list")
    public ResponseEntity<ListEmployeeResponse> getAllEmployees() {
        try {
            var employees = employeeService.getAllEmployees();

            return ResponseEntity.status(HttpStatus.OK).body(
                    ListEmployeeResponse.builder()
                            .success(true)
                            .message("Liste des employés récupérée avec succès")
                            .employees(employees)
                            .total(employees.size())
                            .build()
            );
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la liste des employés: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ListEmployeeResponse.builder()
                            .success(false)
                            .message("Erreur lors de la récupération de la liste des employés")
                            .employees(Collections.emptyList())
                            .total(0)
                            .build()
            );
        }
    }

    /**
     * Récupère les détails complets d'un employé par son ID.
     * Accessible par ADMIN et RH.
     */
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long id) {
        try {
            var employee = employeeService.getEmployeeById(id);

            return ResponseEntity.status(HttpStatus.OK).body(
                    EmployeeResponse.builder()
                            .success(true)
                            .message("Détails de l'employé récupérés avec succès")
                            .employee(employee)
                            .build()
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    EmployeeResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .employee(null)
                            .build()
            );
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'employé {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    EmployeeResponse.builder()
                            .success(false)
                            .message("Erreur lors de la récupération de l'employé")
                            .employee(null)
                            .build()
            );
        }
    }

    /**
     * Récupère les statistiques globales des employés.
     * Accessible par ADMIN et RH.
     */
    @GetMapping("/statistics")
    public ResponseEntity<EmployeeStatisticsResponse> getStatistics() {
        try {
            var statistics = employeeService.getStatistics();

            return ResponseEntity.status(HttpStatus.OK).body(
                    EmployeeStatisticsResponse.builder()
                            .success(true)
                            .message("Statistiques récupérées avec succès")
                            .statistics(statistics)
                            .build()
            );
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    EmployeeStatisticsResponse.builder()
                            .success(false)
                            .message("Erreur lors de la récupération des statistiques")
                            .statistics(Map.of())
                            .build()
            );
        }
    }
}

