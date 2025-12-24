package com.hrtrack.service.implementation;

import com.hrtrack.domain.PointageStatus;
import com.hrtrack.domain.Role;
import com.hrtrack.domain.dtos.EmployeeDto;
import com.hrtrack.domain.dtos.EmployeeDetailDto;
import com.hrtrack.domain.entities.Pointage;
import com.hrtrack.domain.entities.User;
import com.hrtrack.repository.PointageRepository;
import com.hrtrack.repository.UserRepository;
import com.hrtrack.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service d'implémentation pour la gestion des employés.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final UserRepository userRepository;
    private final PointageRepository pointageRepository;

    /**
     * Récupère la liste de tous les employés (utilisateurs avec le rôle EMPLOYEE).
     * 
     * @return Liste de tous les employés avec leurs statistiques de base
     */
    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDto> getAllEmployees() {
        // Récupérer tous les utilisateurs avec le rôle EMPLOYEE
        List<User> employees = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.EMPLOYEE)
                .toList();

        // Calculer les statistiques pour chaque employé (sur les 6 derniers mois)
        LocalDate now = LocalDate.now();
        LocalDate sixMonthsAgo = now.minusMonths(6);
        Date startDate = Date.from(sixMonthsAgo.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(now.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

        return employees.stream()
                .map(employee -> {
                    List<Pointage> pointages = pointageRepository.findByUserAndDateBetween(employee, startDate, endDate);
                    
                    long absences = pointages.stream()
                            .filter(p -> p.getHeureArrivee() == null && p.getHeureDepart() == null)
                            .count();
                    
                    long retards = pointages.stream()
                            .filter(p -> p.getStatut() == PointageStatus.RETARD)
                            .count();

                    return EmployeeDto.builder()
                            .id(employee.getId())
                            .nom(employee.getNom())
                            .prenom(employee.getPrenom())
                            .email(employee.getEmail())
                            .telephone(employee.getTelephone())
                            .nombreAbsences((int) absences)
                            .nombreRetards((int) retards)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Récupère les détails complets d'un employé par son ID.
     * 
     * @param employeeId L'ID de l'employé
     * @return Les détails complets de l'employé avec statistiques et graphiques
     */
    @Override
    @Transactional(readOnly = true)
    public EmployeeDetailDto getEmployeeById(Long employeeId) {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé avec l'ID: " + employeeId));

        if (employee.getRole() != Role.EMPLOYEE) {
            throw new RuntimeException("L'utilisateur avec l'ID " + employeeId + " n'est pas un employé");
        }

        // Calculer les statistiques sur les 6 derniers mois
        LocalDate now = LocalDate.now();
        LocalDate sixMonthsAgo = now.minusMonths(6);
        Date startDate = Date.from(sixMonthsAgo.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(now.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

        List<Pointage> allPointages = pointageRepository.findByUserAndDateBetween(employee, startDate, endDate);

        // Calculer les statistiques globales
        long totalAbsences = allPointages.stream()
                .filter(p -> p.getHeureArrivee() == null && p.getHeureDepart() == null)
                .count();

        long totalRetards = allPointages.stream()
                .filter(p -> p.getStatut() == PointageStatus.RETARD)
                .count();

        // Calculer les heures travaillées
        double heuresTravaillees = allPointages.stream()
                .filter(p -> p.getHeureArrivee() != null && p.getHeureDepart() != null)
                .mapToDouble(p -> {
                    LocalTime arrivee = p.getHeureArrivee();
                    LocalTime depart = p.getHeureDepart();
                    long minutes = java.time.Duration.between(arrivee, depart).toMinutes();
                    return minutes / 60.0;
                })
                .sum();

        // Calculer la fréquence de ponctualité
        long totalPointages = allPointages.size();
        double frequencePonctualite = totalPointages > 0
                ? ((double) (totalPointages - totalAbsences - totalRetards) / totalPointages) * 100
                : 0.0;

        // Calculer les fréquences mensuelles pour les graphiques
        List<Map<String, Object>> frequenceAbsences = new ArrayList<>();
        List<Map<String, Object>> frequenceRetards = new ArrayList<>();
        String[] moisNoms = {"Jan", "Fév", "Mar", "Avr", "Mai", "Juin"};

        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

            Date monthStartDate = Date.from(monthStart.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date monthEndDate = Date.from(monthEnd.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

            List<Pointage> pointagesMois = pointageRepository.findByUserAndDateBetween(employee, monthStartDate, monthEndDate);

            long absencesMois = pointagesMois.stream()
                    .filter(p -> p.getHeureArrivee() == null && p.getHeureDepart() == null)
                    .count();

            long retardsMois = pointagesMois.stream()
                    .filter(p -> p.getStatut() == PointageStatus.RETARD)
                    .count();

            Map<String, Object> absencesData = new HashMap<>();
            absencesData.put("mois", moisNoms[5 - i]);
            absencesData.put("nombre", absencesMois);
            frequenceAbsences.add(absencesData);

            Map<String, Object> retardsData = new HashMap<>();
            retardsData.put("mois", moisNoms[5 - i]);
            retardsData.put("nombre", retardsMois);
            frequenceRetards.add(retardsData);
        }

        return EmployeeDetailDto.builder()
                .id(employee.getId())
                .nom(employee.getNom())
                .prenom(employee.getPrenom())
                .email(employee.getEmail())
                .telephone(employee.getTelephone())
                .nombreAbsences((int) totalAbsences)
                .nombreRetards((int) totalRetards)
                .heuresTravaillees(Math.round(heuresTravaillees * 100.0) / 100.0)
                .frequencePonctualite(Math.round(frequencePonctualite * 100.0) / 100.0)
                .frequenceAbsences(frequenceAbsences)
                .frequenceRetards(frequenceRetards)
                .build();
    }

    /**
     * Récupère les statistiques globales des employés.
     * 
     * @return Map contenant les statistiques (total, absences, retards, moyennes, top 5, etc.)
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistics() {
        List<EmployeeDto> employees = getAllEmployees();

        int totalEmployees = employees.size();
        int totalAbsences = employees.stream()
                .mapToInt(emp -> emp.getNombreAbsences() != null ? emp.getNombreAbsences() : 0)
                .sum();
        int totalRetards = employees.stream()
                .mapToInt(emp -> emp.getNombreRetards() != null ? emp.getNombreRetards() : 0)
                .sum();

        double moyenneAbsences = totalEmployees > 0 ? (double) totalAbsences / totalEmployees : 0.0;
        double moyenneRetards = totalEmployees > 0 ? (double) totalRetards / totalEmployees : 0.0;

        // Top 5 employés par absences
        List<Map<String, Object>> employeesByAbsences = employees.stream()
                .sorted((a, b) -> Integer.compare(
                        b.getNombreAbsences() != null ? b.getNombreAbsences() : 0,
                        a.getNombreAbsences() != null ? a.getNombreAbsences() : 0
                ))
                .limit(5)
                .map(emp -> {
                    Map<String, Object> empData = new HashMap<>();
                    empData.put("id", emp.getId());
                    empData.put("nom", emp.getNom());
                    empData.put("prenom", emp.getPrenom());
                    empData.put("nombreAbsences", emp.getNombreAbsences() != null ? emp.getNombreAbsences() : 0);
                    return empData;
                })
                .collect(Collectors.toList());

        // Top 5 employés par retards
        List<Map<String, Object>> employeesByRetards = employees.stream()
                .sorted((a, b) -> Integer.compare(
                        b.getNombreRetards() != null ? b.getNombreRetards() : 0,
                        a.getNombreRetards() != null ? a.getNombreRetards() : 0
                ))
                .limit(5)
                .map(emp -> {
                    Map<String, Object> empData = new HashMap<>();
                    empData.put("id", emp.getId());
                    empData.put("nom", emp.getNom());
                    empData.put("prenom", emp.getPrenom());
                    empData.put("nombreRetards", emp.getNombreRetards() != null ? emp.getNombreRetards() : 0);
                    return empData;
                })
                .collect(Collectors.toList());

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalEmployees", totalEmployees);
        statistics.put("totalAbsences", totalAbsences);
        statistics.put("totalRetards", totalRetards);
        statistics.put("moyenneAbsences", Math.round(moyenneAbsences * 10.0) / 10.0);
        statistics.put("moyenneRetards", Math.round(moyenneRetards * 10.0) / 10.0);
        statistics.put("employeesByAbsences", employeesByAbsences);
        statistics.put("employeesByRetards", employeesByRetards);

        return statistics;
    }
}

