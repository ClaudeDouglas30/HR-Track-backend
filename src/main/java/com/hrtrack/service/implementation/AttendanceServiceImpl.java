package com.hrtrack.service.implementation;

import com.hrtrack.domain.PointageStatus;
import com.hrtrack.domain.Role;
import com.hrtrack.domain.dtos.AttendanceDto;
import com.hrtrack.domain.entities.Pointage;
import com.hrtrack.domain.entities.User;
import com.hrtrack.repository.PointageRepository;
import com.hrtrack.repository.UserRepository;
import com.hrtrack.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service d'implémentation pour la gestion des présences et absences.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final UserRepository userRepository;
    private final PointageRepository pointageRepository;

    /**
     * Récupère la liste des présences/absences de tous les employés.
     * 
     * @param date Date pour filtrer les pointages (optionnel, si null, utilise la date actuelle)
     * @return Liste des données de présence pour chaque employé
     */
    @Override
    @Transactional(readOnly = true)
    public List<AttendanceDto> getAllAttendances(LocalDate date) {
        // Si aucune date n'est fournie, utiliser la date actuelle
        if (date == null) {
            date = LocalDate.now();
        }

        // Récupérer tous les employés (utilisateurs avec le rôle EMPLOYEE)
        List<User> employees = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.EMPLOYEE)
                .toList();

        // Convertir la date en Date pour la requête
        Date startDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(date.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

        return employees.stream()
                .map(employee -> {
                    // Récupérer les pointages de l'employé pour la date spécifiée
                    List<Pointage> pointages = pointageRepository.findByUserAndDateBetween(
                            employee, startDate, endDate);

                    // Calculer les absences (pointages sans heure d'arrivée ni de départ)
                    long absences = pointages.stream()
                            .filter(p -> p.getHeureArrivee() == null && p.getHeureDepart() == null)
                            .count();

                    // Calculer les retards
                    long retards = pointages.stream()
                            .filter(p -> p.getStatut() == PointageStatus.RETARD)
                            .count();

                    return AttendanceDto.builder()
                            .employeeId(employee.getId())
                            .nom(employee.getNom())
                            .prenom(employee.getPrenom())
                            .email(employee.getEmail())
                            .nombreAbsences((int) absences)
                            .nombreRetards((int) retards)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Récupère les statistiques globales des présences/absences.
     * 
     * @param date Date pour filtrer les pointages (optionnel, si null, utilise la date actuelle)
     * @return Map contenant les statistiques
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistics(LocalDate date) {
        // Si aucune date n'est fournie, utiliser la date actuelle
        if (date == null) {
            date = LocalDate.now();
        }

        // Récupérer toutes les données de présence
        List<AttendanceDto> attendances = getAllAttendances(date);

        // Calculer les statistiques
        int totalAbsences = attendances.stream()
                .mapToInt(att -> att.getNombreAbsences() != null ? att.getNombreAbsences() : 0)
                .sum();

        int totalRetards = attendances.stream()
                .mapToInt(att -> att.getNombreRetards() != null ? att.getNombreRetards() : 0)
                .sum();

        int totalEmployees = attendances.size();

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalAbsences", totalAbsences);
        statistics.put("totalRetards", totalRetards);
        statistics.put("totalEmployees", totalEmployees);
        statistics.put("date", date.toString());

        return statistics;
    }
}

