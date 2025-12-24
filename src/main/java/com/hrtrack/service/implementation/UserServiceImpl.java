package com.hrtrack.service.implementation;

import com.hrtrack.domain.CongeStatus;
import com.hrtrack.domain.PointageStatus;
import com.hrtrack.domain.Role;
import com.hrtrack.domain.dtos.CongeDto;
import com.hrtrack.domain.dtos.PointageDto;
import com.hrtrack.domain.dtos.UserDetailDto;
import com.hrtrack.domain.dtos.UserDto;
import com.hrtrack.domain.entities.Conge;
import com.hrtrack.domain.entities.Pointage;
import com.hrtrack.domain.entities.User;
import com.hrtrack.exception.EmailingException;
import com.hrtrack.mappers.UserMapper;
import com.hrtrack.repository.CongeRepository;
import com.hrtrack.repository.PointageRepository;
import com.hrtrack.repository.UserRepository;
import com.hrtrack.service.CongeService;
import com.hrtrack.service.EmailService;
import com.hrtrack.service.UserService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service d'implémentation pour la gestion des utilisateurs.
 * Fournit toutes les opérations CRUD et de gestion des comptes utilisateurs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final PointageRepository pointageRepository;
    private final CongeRepository congeRepository;
    private final CongeService congeService;

    /**
     * Récupère la liste de tous les utilisateurs.
     * 
     * @return Liste de tous les utilisateurs
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    /**
     * Valide le compte d'un utilisateur et envoie un email de confirmation.
     * 
     * @param userId L'ID de l'utilisateur à valider
     * @return L'utilisateur validé
     * @throws MessagingException
     * @throws EmailingException
     */
    @Override
    @Transactional
    public UserDto validateUserAccount(Long userId) throws MessagingException, EmailingException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId));

        if (user.isAccountValidated()) {
            log.warn("Tentative de validation d'un compte déjà validé : {}", userId);
            return userMapper.toDto(user);
        }

        // Valider le compte
        user.setAccountValidated(true);
        User savedUser = userRepository.save(user);

        // Envoyer l'email de confirmation
        try {
            Context context = new Context();
            context.setVariable("subject", "Votre compte a été activé - HR Track");
            context.setVariable("name", savedUser.getNomComplet());
            context.setVariable("loginUrl", "http://localhost:3000/login"); // URL de connexion

            emailService.sendMail(
                    savedUser.getEmail(),
                    "Votre compte a été activé - HR Track",
                    "user-confirmation-registration",
                    context
            );
            log.info("Email de validation envoyé à l'utilisateur : {}", savedUser.getEmail());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de validation : {}", e.getMessage());
            // Ne pas faire échouer la validation si l'email échoue
        }

        return userMapper.toDto(savedUser);
    }

    /**
     * Met à jour les informations d'un utilisateur.
     * 
     * @param userId L'ID de l'utilisateur à modifier
     * @param userDto Les nouvelles informations de l'utilisateur
     * @return L'utilisateur mis à jour
     */
    @Override
    @Transactional
    public UserDto updateUser(Long userId, UserDto userDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId));

        // Mise à jour des champs modifiables
        if (userDto.getNom() != null) {
            user.setNom(userDto.getNom().toUpperCase());
        }
        if (userDto.getPrenom() != null) {
            user.setPrenom(userDto.getPrenom());
        }
        if (userDto.getEmail() != null && !userDto.getEmail().equals(user.getEmail())) {
            // Vérifier que l'email n'est pas déjà utilisé
            if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
                throw new RuntimeException("Cet email est déjà utilisé par un autre utilisateur");
            }
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getTelephone() != null) {
            user.setTelephone(userDto.getTelephone());
        }
        if (userDto.getPhotoProfil() != null) {
            user.setPhotoProfil(userDto.getPhotoProfil());
        }
        if (userDto.getRole() != null) {
            user.setRole(userDto.getRole());
        }

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    /**
     * Crée un nouvel utilisateur.
     * 
     * @param nom Le nom de l'utilisateur
     * @param prenom Le prénom de l'utilisateur
     * @param email L'email de l'utilisateur
     * @param telephone Le téléphone de l'utilisateur
     * @param password Le mot de passe (sera encodé)
     * @param role Le rôle de l'utilisateur
     * @param photoProfil La photo de profil (optionnel)
     * @return L'utilisateur créé
     */
    @Override
    @Transactional
    public UserDto createUser(String nom, String prenom, String email, String telephone, String password, Role role, String photoProfil) {
        // Vérifier que l'email n'existe pas déjà
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        // Créer le nouvel utilisateur
        User user = User.builder()
                .nom(nom != null ? nom.toUpperCase() : "")
                .prenom(prenom != null ? prenom : "")
                .email(email)
                .telephone(telephone)
                .photoProfil(photoProfil)
                .role(role != null ? role : Role.EMPLOYEE)
                .password(passwordEncoder.encode(password))
                .isAccountValidated(false) // Par défaut, le compte n'est pas validé
                .build();

        User savedUser = userRepository.save(user);
        log.info("Nouvel utilisateur créé : {} ({})", savedUser.getNomComplet(), savedUser.getEmail());
        
        return userMapper.toDto(savedUser);
    }

    /**
     * Supprime un utilisateur.
     * 
     * @param userId L'ID de l'utilisateur à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    @Override
    @Transactional
    public boolean deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            log.warn("Tentative de suppression d'un utilisateur inexistant : {}", userId);
            return false;
        }

        userRepository.deleteById(userId);
        log.info("Utilisateur supprimé : {}", userId);
        return true;
    }

    /**
     * Récupère les statistiques des utilisateurs.
     * 
     * @return Map contenant les statistiques (total, validés, non validés, par rôle, etc.)
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistics() {
        List<User> allUsers = userRepository.findAll();

        long totalUsers = allUsers.size();
        long validatedUsers = allUsers.stream()
                .filter(User::isAccountValidated)
                .count();
        long nonValidatedUsers = totalUsers - validatedUsers;

        Map<String, Long> usersByRole = new HashMap<>();
        usersByRole.put("EMPLOYEE", allUsers.stream()
                .filter(u -> u.getRole() == Role.EMPLOYEE)
                .count());
        usersByRole.put("RH", allUsers.stream()
                .filter(u -> u.getRole() == Role.RH)
                .count());
        usersByRole.put("ADMIN", allUsers.stream()
                .filter(u -> u.getRole() == Role.ADMIN)
                .count());

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalUsers", totalUsers);
        statistics.put("validatedUsers", validatedUsers);
        statistics.put("nonValidatedUsers", nonValidatedUsers);
        statistics.put("usersByRole", usersByRole);
        statistics.put("validationRate", totalUsers > 0 ? (double) validatedUsers / totalUsers * 100 : 0);

        return statistics;
    }

    /**
     * Récupère un utilisateur par son ID.
     * 
     * @param userId L'ID de l'utilisateur
     * @return L'utilisateur trouvé, null si non trouvé
     */
    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(userMapper::toDto)
                .orElse(null);
    }

    /**
     * Récupère les détails complets d'un utilisateur avec toutes ses statistiques.
     * 
     * @param userId L'ID de l'utilisateur
     * @return Les détails complets de l'utilisateur avec statistiques, pointages et congés
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetailDto getUserDetailById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId));

        UserDto userDto = userMapper.toDto(user);

        // Calculer les statistiques mensuelles
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        
        Date startDate = Date.from(startOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(endOfMonth.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
        
        List<Pointage> pointagesMois = pointageRepository.findByUserAndDateBetween(user, startDate, endDate);
        
        // Calculer les statistiques
        long joursPresents = pointagesMois.stream()
                .filter(p -> p.getStatut() == PointageStatus.PRESENT || p.getStatut() == PointageStatus.RETARD)
                .count();
        
        long retards = pointagesMois.stream()
                .filter(p -> p.getStatut() == PointageStatus.RETARD)
                .count();
        
        long joursAbsents = pointagesMois.stream()
                .filter(p -> p.getHeureArrivee() == null && p.getHeureDepart() == null)
                .count();
        
        // Calculer les heures travaillées
        double heuresTravaillees = pointagesMois.stream()
                .filter(p -> p.getHeureArrivee() != null && p.getHeureDepart() != null)
                .mapToDouble(p -> {
                    LocalTime arrivee = p.getHeureArrivee();
                    LocalTime depart = p.getHeureDepart();
                    long minutes = java.time.Duration.between(arrivee, depart).toMinutes();
                    return minutes / 60.0;
                })
                .sum();
        
        // Calculer la fréquence de ponctualité
        long totalPointages = pointagesMois.size();
        double frequencePonctualite = totalPointages > 0 
                ? ((double) (joursPresents - retards) / totalPointages) * 100 
                : 0.0;

        // Récupérer les pointages de la semaine
        LocalDate startOfWeek = now.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        
        Date startWeekDate = Date.from(startOfWeek.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endWeekDate = Date.from(endOfWeek.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
        
        List<Pointage> pointagesSemaine = pointageRepository.findByUserAndDateBetween(user, startWeekDate, endWeekDate);
        
        // Créer un map pour les jours de la semaine
        Map<DayOfWeek, Pointage> pointageByDay = pointagesSemaine.stream()
                .collect(Collectors.toMap(
                        p -> p.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getDayOfWeek(),
                        p -> p,
                        (existing, replacement) -> existing
                ));
        
        // Créer la liste des pointages de la semaine (7 jours)
        List<PointageDto> pointageSemaine = new ArrayList<>();
        String[] joursSemaine = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        
        for (int i = 0; i < 7; i++) {
            LocalDate dayDate = startOfWeek.plusDays(i);
            DayOfWeek dayOfWeek = dayDate.getDayOfWeek();
            Pointage pointage = pointageByDay.get(dayOfWeek);
            
            if (pointage != null) {
                PointageDto dto = PointageDto.builder()
                        .id(pointage.getId())
                        .date(pointage.getDate())
                        .heureArrivee(pointage.getHeureArrivee())
                        .heureDepart(pointage.getHeureDepart())
                        .statut(pointage.getStatut())
                        .jour(joursSemaine[i])
                        .build();
                pointageSemaine.add(dto);
            } else {
                // Créer un pointage vide pour les jours sans pointage
                PointageDto dto = PointageDto.builder()
                        .id(null)
                        .date(Date.from(dayDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))
                        .heureArrivee(null)
                        .heureDepart(null)
                        .statut(null)
                        .jour(joursSemaine[i])
                        .build();
                pointageSemaine.add(dto);
            }
        }

        // Calculer l'évolution de la ponctualité sur 6 mois
        List<Map<String, Object>> evolutionPonctualite = new ArrayList<>();
        String[] moisNoms = {"Jan", "Fév", "Mar", "Avr", "Mai", "Juin"};
        
        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
            
            Date monthStartDate = Date.from(monthStart.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date monthEndDate = Date.from(monthEnd.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
            
            List<Pointage> pointagesMoisEvolution = pointageRepository.findByUserAndDateBetween(user, monthStartDate, monthEndDate);
            
            long absencesMois = pointagesMoisEvolution.stream()
                    .filter(p -> p.getHeureArrivee() == null && p.getHeureDepart() == null)
                    .count();
            
            long retardsMois = pointagesMoisEvolution.stream()
                    .filter(p -> p.getStatut() == PointageStatus.RETARD)
                    .count();
            
            long totalMois = pointagesMoisEvolution.size();
            double ponctualiteMois = totalMois > 0 
                    ? ((double) (totalMois - absencesMois - retardsMois) / totalMois) * 100 
                    : 0.0;
            
            Map<String, Object> moisData = new HashMap<>();
            moisData.put("mois", moisNoms[5 - i]);
            moisData.put("ponctualite", Math.round(ponctualiteMois));
            moisData.put("absences", absencesMois);
            moisData.put("retards", retardsMois);
            evolutionPonctualite.add(moisData);
        }

        // Récupérer les demandes de congés récentes (les 10 dernières)
        List<Conge> conges = congeRepository.findByUser(user);
        List<CongeDto> demandesConges = conges.stream()
                .sorted((c1, c2) -> c2.getDateSoumission().compareTo(c1.getDateSoumission()))
                .limit(10)
                .map(c -> congeService.getCongeById(c.getCongeUuid()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Construire le UserDetailDto
        return UserDetailDto.builder()
                .id(userDto.getId())
                .nom(userDto.getNom())
                .prenom(userDto.getPrenom())
                .email(userDto.getEmail())
                .photoProfil(userDto.getPhotoProfil())
                .telephone(userDto.getTelephone())
                .role(userDto.getRole())
                .accountValidated(userDto.isAccountValidated())
                .lastLogin(userDto.getLastLogin())
                .createdAt(userDto.getCreatedAt())
                .updatedAt(userDto.getUpdatedAt())
                .joursPresents((int) joursPresents)
                .joursAbsents((int) joursAbsents)
                .retards((int) retards)
                .heuresTravaillees(heuresTravaillees)
                .frequencePonctualite(Math.round(frequencePonctualite * 100.0) / 100.0)
                .pointageSemaine(pointageSemaine)
                .evolutionPonctualite(evolutionPonctualite)
                .demandesConges(demandesConges)
                .build();
    }
}
