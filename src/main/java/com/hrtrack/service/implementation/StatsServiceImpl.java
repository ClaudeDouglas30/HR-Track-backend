package com.hrtrack.service.implementation;

import com.hrtrack.domain.CongeStatus;
import com.hrtrack.domain.Role;
import com.hrtrack.domain.dtos.StatsUserDto;
import com.hrtrack.domain.dtos.StatsRhDto;
import com.hrtrack.domain.entities.*;
import com.hrtrack.repository.*;
import com.hrtrack.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
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
        log.info("getUserStats called with email: {}, LocalDate: {}, Date: {}", email, now, dateParam);

        // Correction : passer 'now' (LocalDate) au lieu de 'dateParam' (Date)
        int totalCongeAccepte = 0;
        try {
            log.info("Calling congeRepository.countByUserIdAndStatusAndDateFinBefore...");
            totalCongeAccepte = congeRepository.countByUserIdAndStatusAndDateFinBefore(
                user.getId(), CongeStatus.valueOf("ACCEPTER"), now
            );
            log.info("congeRepository.countByUserIdAndStatusAndDateFinBefore returned: {}", totalCongeAccepte);
        } catch (Exception e) {
            log.error("Error in congeRepository.countByUserIdAndStatusAndDateFinBefore", e);
            throw e;
        }

        Set<LocalDate> joursPointes = new HashSet<>();
        try {
            log.info("Calling pointageRepository.findDistinctDatesByUserId...");
            joursPointes = pointageRepository.findDistinctDatesByUserId(
                user.getId(),
                thisMonth.getYear(),
                thisMonth.getMonthValue()
            ).stream()
            .map(d -> d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
            .collect(Collectors.toSet());
            log.info("pointageRepository.findDistinctDatesByUserId returned: {} dates", joursPointes.size());
        } catch (Exception e) {
            log.error("Error in pointageRepository.findDistinctDatesByUserId", e);
            throw e;
        }

        int joursOuvres = (int) thisMonth.atEndOfMonth().datesUntil(thisMonth.atEndOfMonth().plusDays(1))
            .filter(d -> d.getDayOfWeek().getValue() < 6).count();
        double tauxPresence = joursOuvres == 0 ? 0 : (double) joursPointes.size() / joursOuvres;

        int nbJoursPointes = joursPointes.size();

        int messagesEnvoyes = 0;
        int messagesRecus = 0;
        try {
            log.info("Calling messageRepository.countBySenderId...");
            messagesEnvoyes = messageRepository.countBySenderId(user.getId());
            log.info("messageRepository.countBySenderId returned: {}", messagesEnvoyes);
            log.info("Calling messageRepository.countByReceiverId...");
            messagesRecus = messageRepository.countByReceiverId(user.getId());
            log.info("messageRepository.countByReceiverId returned: {}", messagesRecus);
        } catch (Exception e) {
            log.error("Error in messageRepository.countBySenderId or countByReceiverId", e);
            throw e;
        }

        double heuresEffectueesMois = 0;
        for (LocalDate jour : joursPointes) {
            try {
                Date dateParams = Date.from(jour.atStartOfDay(ZoneId.systemDefault()).toInstant());
                log.info("Calling pointageRepository.findFirstPointageTimeByUserIdAndDate for date {}...", dateParams);
                LocalTime premierPointage = pointageRepository.findFirstPointageTimeByUserIdAndDate(user.getId(), dateParams);
                log.info("pointageRepository.findFirstPointageTimeByUserIdAndDate returned: {}", premierPointage);
                if (premierPointage != null) {
                    heuresEffectueesMois += Duration.between(premierPointage, LocalTime.of(17, 0)).toMinutes() / 60.0;
                }
            } catch (Exception e) {
                log.error("Error in pointageRepository.findFirstPointageTimeByUserIdAndDate", e);
                throw e;
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
        log.info("getRhStats called with LocalDate: {} and Date: {}", now, dateParam);
        // 1. Nombre total d'employés
        log.info("Calling userRepository.countByRole...");
        int totalEmployes = userRepository.countByRole(Role.valueOf("EMPLOYEE"));
        log.info("userRepository.countByRole returned: {}", totalEmployes);
        // 2. Nombre de demandes de congé en attente
        log.info("Calling congeRepository.countByStatus...");
        int demandesCongeEnAttente = congeRepository.countByStatus(CongeStatus.valueOf("ENATTENTE"));
        log.info("congeRepository.countByStatus returned: {}", demandesCongeEnAttente);
        // 3. Nombre de pointages uniques aujourd'hui
        log.info("Calling pointageRepository.countDistinctUserIdByDate...");
        int pointagesUniquesAujourdhui = 0;
        try {
            pointagesUniquesAujourdhui = pointageRepository.countDistinctUserIdByDate(dateParam);
            log.info("pointageRepository.countDistinctUserIdByDate returned: {}", pointagesUniquesAujourdhui);
        } catch (Exception e) {
            log.error("Error in pointageRepository.countDistinctUserIdByDate", e);
            throw e;
        }
        // 4. Nombre d'utilisateurs en congé (congé accepté, non terminé)
        log.info("Calling congeRepository.countUsersEnConge...");
        int utilisateursEnConge = 0;
        try {
            utilisateursEnConge = congeRepository.countUsersEnConge(now); // Use LocalDate
            log.info("congeRepository.countUsersEnConge returned: {}", utilisateursEnConge);
        } catch (Exception e) {
            log.error("Error in congeRepository.countUsersEnConge", e);
            throw e;
        }
        return StatsRhDto.builder()
                .totalEmployes(totalEmployes)
                .demandesCongeEnAttente(demandesCongeEnAttente)
                .pointagesUniquesAujourdhui(pointagesUniquesAujourdhui)
                .utilisateursEnConge(utilisateursEnConge)
                .build();
    }
}