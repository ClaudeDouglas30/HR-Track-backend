package com.hrtrack.service.implementation;

import com.hrtrack.domain.Role;
import com.hrtrack.domain.dtos.DashboardDto;
import com.hrtrack.domain.entities.Pointage;
import com.hrtrack.domain.entities.User;
import com.hrtrack.domain.PointageStatus;
import com.hrtrack.repository.PointageRepository;
import com.hrtrack.repository.UserRepository;
import com.hrtrack.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service d'implémentation pour la gestion du dashboard administrateur.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final PointageRepository pointageRepository;

    /**
     * Récupère toutes les données nécessaires pour le dashboard administrateur.
     * 
     * @return DashboardDto contenant toutes les statistiques et données pour les graphiques
     */
    @Override
    @Transactional(readOnly = true)
    public DashboardDto getAdminDashboardData() {
        // Récupérer tous les utilisateurs
        List<User> allUsers = userRepository.findAll();

        // Calculer les statistiques par rôle
        long totalUsers = allUsers.size();
        long totalAdmins = allUsers.stream()
                .filter(user -> user.getRole() == Role.ADMIN)
                .count();
        long totalRH = allUsers.stream()
                .filter(user -> user.getRole() == Role.RH)
                .count();
        long totalEmployees = allUsers.stream()
                .filter(user -> user.getRole() == Role.EMPLOYEE)
                .count();

        // Calculer les nouveaux utilisateurs ce mois
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        
        long newUsersThisMonth = allUsers.stream()
                .filter(user -> user.getCreatedAt() != null 
                        && user.getCreatedAt().toLocalDate().isAfter(startOfMonth.minusDays(1)))
                .count();

        // Préparer les données pour le graphique de répartition des rôles
        List<Map<String, Object>> userRoleDistribution = new ArrayList<>();
        
        Map<String, Object> employeesData = new HashMap<>();
        employeesData.put("name", "Employés");
        employeesData.put("value", (int) totalEmployees);
        employeesData.put("color", "#0366fc");
        userRoleDistribution.add(employeesData);

        Map<String, Object> rhData = new HashMap<>();
        rhData.put("name", "RH");
        rhData.put("value", (int) totalRH);
        rhData.put("color", "#538be3");
        userRoleDistribution.add(rhData);

        Map<String, Object> adminData = new HashMap<>();
        adminData.put("name", "Admin");
        adminData.put("value", (int) totalAdmins);
        adminData.put("color", "#cddcf4");
        userRoleDistribution.add(adminData);

        // Calculer les absences et retards par jour (7 derniers jours)
        List<Map<String, Object>> dailyAttendanceData = new ArrayList<>();
        String[] joursSemaine = {"Dim", "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam"};
        
        // Récupérer tous les employés
        List<User> employees = allUsers.stream()
                .filter(user -> user.getRole() == Role.EMPLOYEE)
                .toList();

        // Pour chaque jour des 7 derniers jours
        for (int i = 6; i >= 0; i--) {
            LocalDate day = now.minusDays(i);
            DayOfWeek dayOfWeek = day.getDayOfWeek();
            int dayIndex = dayOfWeek.getValue() % 7; // Dimanche = 0, Lundi = 1, etc.
            
            Date dayStart = Date.from(day.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date dayEnd = Date.from(day.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

            // Calculer les absences et retards pour ce jour
            long absences = 0;
            long retards = 0;

            for (User employee : employees) {
                List<Pointage> pointages = pointageRepository.findByUserAndDateBetween(
                        employee, dayStart, dayEnd);

                // Absences : pointages sans heure d'arrivée ni de départ
                absences += pointages.stream()
                        .filter(p -> p.getHeureArrivee() == null && p.getHeureDepart() == null)
                        .count();

                // Retards
                retards += pointages.stream()
                        .filter(p -> p.getStatut() == PointageStatus.RETARD)
                        .count();
            }

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("jour", joursSemaine[dayIndex]);
            dayData.put("absences", absences);
            dayData.put("retards", retards);
            dailyAttendanceData.add(dayData);
        }

        return DashboardDto.builder()
                .totalUsers((int) totalUsers)
                .totalAdmins((int) totalAdmins)
                .totalRH((int) totalRH)
                .totalEmployees((int) totalEmployees)
                .newUsersThisMonth((int) newUsersThisMonth)
                .userRoleDistribution(userRoleDistribution)
                .dailyAttendanceData(dailyAttendanceData)
                .build();
    }
}

