package com.hrtrack.service.implementation;

import com.hrtrack.domain.CongeStatus;
import com.hrtrack.domain.Role;
import com.hrtrack.domain.dtos.StatsUserDto;
import com.hrtrack.domain.dtos.StatsRhDto;
import com.hrtrack.domain.entities.*;
import com.hrtrack.repository.*;
import com.hrtrack.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final CongeRepository congeRepository;
    private final PointageRepository pointageRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Override
    public StatsUserDto getUserStats(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return StatsUserDto.builder().build();
        LocalDate now = LocalDate.now();
        YearMonth thisMonth = YearMonth.now();

        Date dateParam = Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant());


        // 1. Nombre total de congés acceptés déjà passés
        int totalCongeAccepte = congeRepository.countByUserIdAndStatusAndDateFinBefore(
            user.getId(), CongeStatus.valueOf("ACCEPTER"), dateParam
        );

        // 2. Taux de présence (nb jours pointés / nb jours ouvrés du mois)
        Set<LocalDate> joursPointes = pointageRepository.findDistinctDatesByUserId(
                        user.getId(),
                        thisMonth.getYear(),
                        thisMonth.getMonthValue()
                ).stream()
                .map(d -> d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                .collect(Collectors.toSet());
        int joursOuvres = (int) thisMonth.atEndOfMonth().datesUntil(thisMonth.atEndOfMonth().plusDays(1))
            .filter(d -> d.getDayOfWeek().getValue() < 6).count();
        double tauxPresence = joursOuvres == 0 ? 0 : (double) joursPointes.size() / joursOuvres;

        // 3. Nombre de jours distincts pointés
        int nbJoursPointes = joursPointes.size();

        // 4. Nombre de messages envoyés et reçus
        int messagesEnvoyes = messageRepository.countBySenderId(user.getId());
        int messagesRecus = messageRepository.countByReceiverId(user.getId());

        // 5. Nombre d'heures totales effectuées ce mois
        double heuresEffectueesMois = 0;
        for (LocalDate jour : joursPointes) {
            Date dateParams = Date.from(jour.atStartOfDay(ZoneId.systemDefault()).toInstant());
            LocalTime premierPointage = pointageRepository.findFirstPointageTimeByUserIdAndDate(user.getId(), dateParams);
            if (premierPointage != null) {
                heuresEffectueesMois += Duration.between(premierPointage, LocalTime.of(17, 0)).toMinutes() / 60.0;
            }
        }

        return StatsUserDto.builder()
                .totalCongeAccepte(totalCongeAccepte)
                .tauxPresence(tauxPresence)
                .joursPointes(nbJoursPointes)
                .messagesEnvoyes(messagesEnvoyes)
                .messagesRecus(messagesRecus)
                .heuresEffectueesMois(heuresEffectueesMois)
                .build();
    }

    @Override
    public StatsRhDto getRhStats() {
        LocalDate now = LocalDate.now();
        Date dateParam = Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant());
        // 1. Nombre total d'employés
        int totalEmployes = userRepository.countByRole(Role.valueOf("EMPLOYEE"));
        // 2. Nombre de demandes de congé en attente
        int demandesCongeEnAttente = congeRepository.countByStatus(CongeStatus.valueOf("ENATTENTE"));
        // 3. Nombre de pointages uniques aujourd'hui
        int pointagesUniquesAujourdhui = pointageRepository.countDistinctUserIdByDate(dateParam);
        // 4. Nombre d'utilisateurs en congé (congé accepté, non terminé)
        int utilisateursEnConge = congeRepository.countUsersEnConge(dateParam);
        return StatsRhDto.builder()
                .totalEmployes(totalEmployes)
                .demandesCongeEnAttente(demandesCongeEnAttente)
                .pointagesUniquesAujourdhui(pointagesUniquesAujourdhui)
                .utilisateursEnConge(utilisateursEnConge)
                .build();
    }
}