package com.hrtrack.service.implementation;

import com.hrtrack.domain.CongeStatus;
import com.hrtrack.domain.Role;
import com.hrtrack.domain.dtos.DashboardDto;
import com.hrtrack.domain.dtos.RHDashboardDto;
import com.hrtrack.domain.entities.Conge;
import com.hrtrack.domain.entities.Pointage;
import com.hrtrack.domain.entities.User;
import com.hrtrack.domain.PointageStatus;
import com.hrtrack.repository.CongeRepository;
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
    private final CongeRepository congeRepository;

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

    /**
     * Récupère toutes les données nécessaires pour le dashboard RH.
     * 
     * @return RHDashboardDto contenant toutes les statistiques et données pour les graphiques
     */
    @Override
    @Transactional(readOnly = true)
    public RHDashboardDto getRHDashboardData() {
        LocalDate now = LocalDate.now();
        LocalDate yesterday = now.minusDays(1);
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate startOfWeek = now.with(DayOfWeek.MONDAY);

        // Récupérer tous les congés
        List<Conge> allConges = congeRepository.findAll();

        // Calculer les demandes en attente
        long demandesEnAttente = allConges.stream()
                .filter(c -> c.getCongeStatus() == CongeStatus.ENATTENTE)
                .count();

        // Calculer les demandes en attente hier (pour l'évolution)
        // On compte les demandes en attente qui ont été soumises avant aujourd'hui
        // Cela donne une approximation de l'état d'hier
        long demandesEnAttenteHier = allConges.stream()
                .filter(c -> c.getCongeStatus() == CongeStatus.ENATTENTE
                        && c.getDateSoumission() != null
                        && c.getDateSoumission().toLocalDate().isBefore(now))
                .count();

        // Récupérer tous les employés
        List<User> employees = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.EMPLOYEE)
                .toList();

        int totalEmployees = employees.size();

        // Calculer les congés ce mois (demandes soumises ce mois)
        long congesCeMois = allConges.stream()
                .filter(c -> c.getDateSoumission() != null
                        && c.getDateSoumission().toLocalDate().isAfter(startOfMonth.minusDays(1)))
                .count();

        // Calculer les congés cette semaine (pour l'évolution)
        long congesCetteSemaine = allConges.stream()
                .filter(c -> c.getDateSoumission() != null
                        && c.getDateSoumission().toLocalDate().isAfter(startOfWeek.minusDays(1)))
                .count();

        // Calculer les pointages aujourd'hui
        Date todayStart = Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date todayEnd = Date.from(now.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

        long pointagesAujourdhui = 0;
        long presentsAujourdhui = 0;

        for (User employee : employees) {
            List<Pointage> pointages = pointageRepository.findByUserAndDateBetween(
                    employee, todayStart, todayEnd);
            
            if (!pointages.isEmpty()) {
                pointagesAujourdhui++;
                // Considérer présent si au moins un pointage avec heure d'arrivée
                boolean present = pointages.stream()
                        .anyMatch(p -> p.getHeureArrivee() != null);
                if (present) {
                    presentsAujourdhui++;
                }
            }
        }

        // Calculer le pourcentage de présence
        double pourcentagePresence = totalEmployees > 0
                ? (double) presentsAujourdhui / totalEmployees * 100
                : 0.0;

        // Calculer les absences et retards par jour (7 derniers jours)
        List<Map<String, Object>> dailyAttendanceData = new ArrayList<>();
        String[] joursSemaine = {"Dim", "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam"};
        
        for (int i = 6; i >= 0; i--) {
            LocalDate day = now.minusDays(i);
            DayOfWeek dayOfWeek = day.getDayOfWeek();
            int dayIndex = dayOfWeek.getValue() % 7;
            
            Date dayStart = Date.from(day.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date dayEnd = Date.from(day.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

            long absences = 0;
            long retards = 0;

            for (User employee : employees) {
                List<Pointage> pointages = pointageRepository.findByUserAndDateBetween(
                        employee, dayStart, dayEnd);

                absences += pointages.stream()
                        .filter(p -> p.getHeureArrivee() == null && p.getHeureDepart() == null)
                        .count();

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

        // Calculer la répartition des congés par type
        Map<com.hrtrack.domain.Libelle, Long> congesByType = allConges.stream()
                .filter(c -> c.getTypeConge() != null && c.getTypeConge().getLibelle() != null)
                .collect(Collectors.groupingBy(
                        c -> c.getTypeConge().getLibelle(),
                        Collectors.counting()
                ));

        List<Map<String, Object>> leaveTypeDistribution = new ArrayList<>();
        
        // Couleurs et noms pour les différents types de congés
        Map<com.hrtrack.domain.Libelle, String> typeColors = new HashMap<>();
        typeColors.put(com.hrtrack.domain.Libelle.MALADIE, "#0366fc");
        typeColors.put(com.hrtrack.domain.Libelle.PARENTAL, "#538be3");
        typeColors.put(com.hrtrack.domain.Libelle.MISSION, "#cddcf4");
        typeColors.put(com.hrtrack.domain.Libelle.AUTRES, "#F5F7FA");
        
        Map<com.hrtrack.domain.Libelle, String> typeNames = new HashMap<>();
        typeNames.put(com.hrtrack.domain.Libelle.MALADIE, "Congés maladie");
        typeNames.put(com.hrtrack.domain.Libelle.PARENTAL, "Congés parentaux");
        typeNames.put(com.hrtrack.domain.Libelle.MISSION, "Congés mission");
        typeNames.put(com.hrtrack.domain.Libelle.AUTRES, "Autres");

        // Ajouter chaque type de congé
        for (Map.Entry<com.hrtrack.domain.Libelle, Long> entry : congesByType.entrySet()) {
            Map<String, Object> typeData = new HashMap<>();
            typeData.put("name", typeNames.getOrDefault(entry.getKey(), entry.getKey().toString()));
            typeData.put("value", entry.getValue().intValue());
            typeData.put("color", typeColors.getOrDefault(entry.getKey(), "#0366fc"));
            leaveTypeDistribution.add(typeData);
        }

        // Si aucun congé, ajouter des données vides
        if (leaveTypeDistribution.isEmpty()) {
            Map<String, Object> emptyData = new HashMap<>();
            emptyData.put("name", "Aucun congé");
            emptyData.put("value", 0);
            emptyData.put("color", "#F5F7FA");
            leaveTypeDistribution.add(emptyData);
        }

        // Calculer les tendances mensuelles (6 derniers mois)
        List<Map<String, Object>> monthlyStats = new ArrayList<>();
        String[] moisNoms = {"Jan", "Fév", "Mar", "Avr", "Mai", "Juin"};
        
        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

            long demandes = allConges.stream()
                    .filter(c -> c.getDateSoumission() != null
                            && c.getDateSoumission().toLocalDate().isAfter(monthStart.minusDays(1))
                            && c.getDateSoumission().toLocalDate().isBefore(monthEnd.plusDays(1)))
                    .count();

            long approuvees = allConges.stream()
                    .filter(c -> c.getCongeStatus() == CongeStatus.ACCEPTER
                            && c.getDateSoumission() != null
                            && c.getDateSoumission().toLocalDate().isAfter(monthStart.minusDays(1))
                            && c.getDateSoumission().toLocalDate().isBefore(monthEnd.plusDays(1)))
                    .count();

            Map<String, Object> monthData = new HashMap<>();
            monthData.put("name", moisNoms[5 - i]);
            monthData.put("demandes", demandes);
            monthData.put("approuvées", approuvees);
            monthlyStats.add(monthData);
        }

        return RHDashboardDto.builder()
                .demandesEnAttente((int) demandesEnAttente)
                .demandesEnAttenteHier((int) demandesEnAttenteHier)
                .totalEmployees(totalEmployees)
                .congesCeMois((int) congesCeMois)
                .congesCetteSemaine((int) congesCetteSemaine)
                .pointagesAujourdhui((int) pointagesAujourdhui)
                .pourcentagePresence(Math.round(pourcentagePresence * 10.0) / 10.0)
                .dailyAttendanceData(dailyAttendanceData)
                .leaveTypeDistribution(leaveTypeDistribution)
                .monthlyStats(monthlyStats)
                .build();
    }
}

