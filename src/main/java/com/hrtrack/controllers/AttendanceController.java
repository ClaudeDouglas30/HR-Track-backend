package com.hrtrack.controllers;

import com.hrtrack.domain.dtos.AttendanceDto;
import com.hrtrack.domain.responses.attendance.AttendanceStatisticsResponse;
import com.hrtrack.domain.responses.attendance.ListAttendanceResponse;
import com.hrtrack.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour la gestion des présences et absences des employés.
 * 
 * Endpoints disponibles :
 * - GET /api/v1/attendances/list : Liste les présences/absences de tous les employés
 * - GET /api/v1/attendances/statistics : Récupère les statistiques globales
 * 
 * Les deux endpoints acceptent un paramètre optionnel "date" pour filtrer par date.
 * Si aucune date n'est fournie, la date actuelle est utilisée.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    /**
     * Récupère la liste des présences/absences de tous les employés.
     * Accessible par ADMIN et RH.
     * 
     * @param date Date pour filtrer les pointages (optionnel, format: yyyy-MM-dd)
     * @return Liste des données de présence pour chaque employé
     */
    @GetMapping("/list")
    public ResponseEntity<ListAttendanceResponse> getAllAttendances(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<AttendanceDto> attendances = attendanceService.getAllAttendances(date);

            return ResponseEntity.status(HttpStatus.OK).body(
                    ListAttendanceResponse.builder()
                            .success(true)
                            .message("Liste des présences récupérée avec succès")
                            .attendances(attendances)
                            .total(attendances.size())
                            .build()
            );
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la liste des présences: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ListAttendanceResponse.builder()
                            .success(false)
                            .message("Erreur lors de la récupération de la liste des présences")
                            .attendances(Collections.emptyList())
                            .total(0)
                            .build()
            );
        }
    }

    /**
     * Récupère les statistiques globales des présences/absences.
     * Accessible par ADMIN et RH.
     * 
     * @param date Date pour filtrer les pointages (optionnel, format: yyyy-MM-dd)
     * @return Statistiques globales (totalAbsences, totalRetards, totalEmployees)
     */
    @GetMapping("/statistics")
    public ResponseEntity<AttendanceStatisticsResponse> getStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            Map<String, Object> statistics = attendanceService.getStatistics(date);

            return ResponseEntity.status(HttpStatus.OK).body(
                    AttendanceStatisticsResponse.builder()
                            .success(true)
                            .message("Statistiques récupérées avec succès")
                            .statistics(statistics)
                            .build()
            );
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    AttendanceStatisticsResponse.builder()
                            .success(false)
                            .message("Erreur lors de la récupération des statistiques")
                            .statistics(Map.of())
                            .build()
            );
        }
    }
}

