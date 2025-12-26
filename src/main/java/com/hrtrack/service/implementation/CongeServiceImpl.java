package com.hrtrack.service.implementation;

import com.hrtrack.domain.CongeStatus;
import com.hrtrack.domain.PointageStatus;
import com.hrtrack.domain.Role;
import com.hrtrack.domain.Libelle;
import com.hrtrack.domain.dtos.CongeDto;
import com.hrtrack.domain.dtos.JustificatifDto;
import com.hrtrack.domain.dtos.*;
import com.hrtrack.domain.entities.Conge;
import com.hrtrack.domain.entities.Justificatif;
import com.hrtrack.domain.entities.Pointage;
import com.hrtrack.domain.entities.TypeConge;
import com.hrtrack.domain.entities.User;
import com.hrtrack.domain.responses.conge.PaginatedLeaveRequestResponse;
import com.hrtrack.domain.validation.CongeValidator;
import com.hrtrack.exception.ValidationException;
import com.hrtrack.mappers.CongeMapper;
import com.hrtrack.mappers.TypeCongeMapper;
import com.hrtrack.repository.CongeRepository;
import com.hrtrack.repository.JustificatifRepository;
import com.hrtrack.repository.PointageRepository;
import com.hrtrack.repository.TypeCongeRepository;
import com.hrtrack.repository.UserRepository;
import com.hrtrack.service.CongeService;
import com.hrtrack.service.JustificatifService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service d'implémentation pour la gestion des congés.
 * Fournit toutes les opérations CRUD et de gestion des statuts des congés.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CongeServiceImpl implements CongeService {

    private final CongeRepository congeRepository;
    private final CongeMapper congeMapper;
    private final UserRepository userRepository;
    private final TypeCongeRepository typeCongeRepository;
    private final CongeValidator congeValidator;
    private final JustificatifService justificatifService;
    private final PointageRepository pointageRepository;
    private final JustificatifRepository justificatifRepository;

    @Value("${application.upload.path:uploads/justificatifs}")
    private String uploadPath;

    /**
     * Récupère la liste de toutes les demandes de congés.
     *
     * @return Liste de tous les congés avec leurs justificatifs mappés
     */
    @Override
    @Transactional(readOnly = true)
    public List<CongeDto> getAllConge() {
        return congeRepository.findAll()
                .stream()
                .map(this::mapCongeWithFiles)
                .toList();
    }

    /**
     * Récupère les détails d'un congé par son UUID.
     * 
     * Règles de permissions :
     * - EMPLOYEE : peut accéder uniquement à ses propres congés
     * - RH/ADMIN : peut accéder à tous les congés
     * 
     * @param uuid L'UUID du congé à récupérer
     * @return Le DTO du congé trouvé, null si non trouvé ou si l'utilisateur n'a pas les permissions
     */
    @Override
    @Transactional(readOnly = true)
    public CongeDto getCongeById(UUID uuid) {
        Conge conge = congeRepository.findByCongeUuid(uuid);
        if (conge == null) {
            return null;
        }

        // Vérification des permissions pour les EMPLOYEE
        User currentUser = getCurrentUser();
        if (currentUser != null && currentUser.getRole() == Role.EMPLOYEE) {
            // Vérifier que le congé appartient à l'utilisateur
            if (!conge.getUser().getId().equals(currentUser.getId())) {
                log.warn("Tentative d'accès non autorisée au congé {} par l'utilisateur {}", uuid, currentUser.getEmail());
                return null;
            }
        }

        return mapCongeWithFiles(conge);
    }

    /**
     * Change le statut d'un congé (ACCEPTER, REFUSER, ENATTENTE).
     * 
     * @param uuid L'UUID du congé à modifier
     * @param status Le nouveau statut à appliquer
     * @return Le DTO du congé mis à jour, null si le congé n'existe pas
     */
    @Override
    @Transactional
    public CongeDto changeCongeStatus(UUID uuid, CongeStatus status) {
        Conge conge = congeRepository.findByCongeUuid(uuid);
        if (conge == null) {
            return null;
        }

        conge.setCongeStatus(status);
        Conge savedConge = congeRepository.save(conge);
        return mapCongeWithFiles(savedConge);
    }

    /**
     * Met à jour les informations d'un congé existant.
     * Seules les dates, le type de congé et le commentaire peuvent être modifiés.
     * Le statut ne peut être modifié que via changeCongeStatus.
     * 
     * Règles de permissions :
     * - EMPLOYEE : peut modifier uniquement ses propres congés et seulement s'ils sont ENATTENTE
     * - RH/ADMIN : peut modifier tous les congés
     * 
     * @param uuid L'UUID du congé à modifier
     * @param congeDto Les nouvelles informations du congé
     * @param files Liste des fichiers justificatifs à ajouter (optionnel)
     * @return Le DTO du congé mis à jour, null si le congé n'existe pas ou si la validation échoue
     */
    @Override
    @Transactional
    public CongeDto editConge(UUID uuid, CongeDto congeDto, List<MultipartFile> files) {
        try {
            Conge conge = congeRepository.findByCongeUuid(uuid);
            if (conge == null) {
                return null;
            }

            // Récupération de l'utilisateur actuellement authentifié
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return null;
            }

            // Vérification des permissions pour les EMPLOYEE
            if (currentUser.getRole() == Role.EMPLOYEE) {
                // Vérifier que le congé appartient à l'utilisateur
                if (!conge.getUser().getId().equals(currentUser.getId())) {
                    log.warn("Tentative de modification non autorisée du congé {} par l'utilisateur {}", uuid, currentUser.getEmail());
                    return null;
                }
                // Vérifier que le congé est en statut ENATTENTE (une fois soumis, plus de modification)
                if (conge.getCongeStatus() != CongeStatus.ENATTENTE) {
                    log.warn("Tentative de modification d'un congé {} déjà traité (statut: {}) par l'utilisateur {}", 
                            uuid, conge.getCongeStatus(), currentUser.getEmail());
                    return null;
                }
            }

            // Validation des données
            congeValidator.validateForUpdate(congeDto, conge, currentUser);

            // Mise à jour des dates si fournies
            if (congeDto.getDateDebut() != null) {
                conge.setDateDebut(congeDto.getDateDebut());
            }
            if (congeDto.getDateFin() != null) {
                conge.setDateFin(congeDto.getDateFin());
            }

            // Mise à jour du type de congé si fourni
            if (congeDto.getTypeConge() != null && congeDto.getTypeConge().getLibelle() != null) {
                typeCongeRepository.findByLibelle(congeDto.getTypeConge().getLibelle()).ifPresent(conge::setTypeConge);
            }

            // Sauvegarde des modifications
            Conge savedConge = congeRepository.save(conge);

            // Upload des fichiers justificatifs si fournis
            if (files != null && !files.isEmpty()) {
                for (MultipartFile file : files) {
                    if (file != null && !file.isEmpty()) {
                        try {
                            justificatifService.uploadJustificatif(uuid, file);
                            log.info("Justificatif ajouté lors de la mise à jour du congé : {}", uuid);
                        } catch (Exception e) {
                            log.error("Erreur lors de l'upload d'un justificatif : {}", e.getMessage());
                            // Continue avec les autres fichiers même si un échoue
                        }
                    }
                }
            }

            return mapCongeWithFiles(savedConge);
        } catch (ValidationException e) {
            log.error("Erreur de validation lors de la mise à jour du congé : {}", e.getMessage());
            return null;
        }
    }

    /**
     * Récupère la liste des congés de l'utilisateur actuellement authentifié.
     * Utilise une requête optimisée pour récupérer uniquement les congés de l'utilisateur.
     * 
     * @return Liste des congés de l'utilisateur connecté
     */
    @Override
    @Transactional(readOnly = true)
    public List<CongeDto> getMyListOfConge() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return List.of();
        }

        // Utilisation d'une requête optimisée pour récupérer uniquement les congés de l'utilisateur
        List<Conge> conges = congeRepository.findByUser(currentUser);

        return conges.stream()
                .map(this::mapCongeWithFiles)
                .toList();
    }

    /**
     * Crée une nouvelle demande de congé pour l'utilisateur actuellement authentifié.
     * Le congé est créé avec le statut ENATTENTE par défaut.
     * 
     * @param congeDto Les informations du congé à créer (dates, type, commentaire)
     * @param files Liste des fichiers justificatifs à ajouter (optionnel)
     * @return Le DTO du congé créé, null si l'utilisateur n'est pas authentifié ou si la validation échoue
     */
    @Override
    @Transactional
    public CongeDto createConge(CongeDto congeDto, List<MultipartFile> files) {
        try {
            // Récupération de l'utilisateur actuellement authentifié
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return null;
            }

            // Validation des données via le validator
            congeValidator.validateForCreation(congeDto, currentUser);

            // Récupération du type de congé validé
            TypeConge typeConge = typeCongeRepository.findByLibelle(congeDto.getTypeConge().getLibelle())
                    .orElseThrow(() -> new ValidationException("Type de congé invalide: " + congeDto.getTypeConge().getLibelle()));

            // Création de l'entité Conge à partir du DTO
            Conge conge = congeMapper.toEntity(congeDto);
            if (conge == null) {
                conge = new Conge();
            }
            
            // Configuration des champs obligatoires et spécifiques
            conge.setCongeUuid(UUID.randomUUID());
            conge.setDateDebut(congeDto.getDateDebut());
            conge.setDateFin(congeDto.getDateFin());
            conge.setCongeStatus(CongeStatus.ENATTENTE);
            conge.setDateSoumission(LocalDateTime.now());
            conge.setUser(currentUser);
            conge.setTypeConge(typeConge);
            conge.setCommentaireEmployee(congeDto.getCommentaireEmployee());
            conge.setJustificatif(""); // Sera mis à jour si des fichiers sont uploadés
            conge.setJustificatifs(new ArrayList<>());

            // Sauvegarde du congé
            Conge savedConge = congeRepository.save(conge);

            // Upload des fichiers justificatifs si fournis
            if (files != null && !files.isEmpty()) {
                UUID congeUuid = savedConge.getCongeUuid();
                for (MultipartFile file : files) {
                    if (file != null && !file.isEmpty()) {
                        try {
                            justificatifService.uploadJustificatif(congeUuid, file);
                            log.info("Justificatif ajouté lors de la création du congé : {}", congeUuid);
                        } catch (Exception e) {
                            log.error("Erreur lors de l'upload d'un justificatif : {}", e.getMessage());
                            // Continue avec les autres fichiers même si un échoue
                        }
                    }
                }
            }

            return mapCongeWithFiles(savedConge);

        } catch (ValidationException e) {
            log.error("Erreur de validation lors de la création du congé : {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la création du congé : {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Supprime un congé par son UUID.
     * Seuls les congés avec le statut ENATTENTE peuvent être supprimés.
     * 
     * @param uuid L'UUID du congé à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    @Override
    @Transactional
    public boolean deleteConge(UUID uuid) {
        try {
            Conge conge = congeRepository.findByCongeUuid(uuid);
            if (conge == null) {
                return false;
            }

            // Récupération de l'utilisateur actuellement authentifié
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return false;
            }

            // Validation de la suppression via le validator
            congeValidator.validateForDeletion(conge, currentUser);

            // Suppression du congé (les justificatifs seront supprimés en cascade)
            congeRepository.delete(conge);
            return true;
        } catch (ValidationException e) {
            log.error("Erreur de validation lors de la suppression du congé {} : {}", uuid, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la suppression du congé {} : {}", uuid, e.getMessage());
            return false;
        }
    }

    /**
     * Méthode utilitaire pour mapper un Conge en CongeDto avec les URLs des justificatifs.
     *
     * @param conge L'entité Conge à mapper
     * @return Le DTO mappé avec les URLs des justificatifs, null si conge est null
     */
//    private CongeDto mapCongeWithFiles(Conge conge) {
//        if (conge == null) {
//            return null;
//        }
//
//        return congeMapper.toDto(conge);
//    }

    /**
     * Méthode utilitaire pour mapper un Conge en CongeDto avec les URLs des justificatifs.
     *
     * @param conge L'entité Conge à mapper
     * @return Le DTO mappé avec les URLs des justificatifs, null si conge est null
     */
    private CongeDto mapCongeWithFiles(Conge conge) {
        if (conge == null) {
            return null;
        }
        CongeDto dto = congeMapper.toDto(conge);

        // Mise à jour des URLs des justificatifs
        List<JustificatifDto> justificatifDtos = dto.getJustificatifs();
        if (justificatifDtos != null) {
            justificatifDtos.forEach(justificatif -> {
                if (justificatif.getNomFichier() != null) {
                    justificatif.setUrlFichier(justificatif.getNomFichier().toString());
                }
            });
        }

        return dto;
    }

    /**
     * Ajoute ou modifie une annotation RH sur un congé.
     *
     * @param uuid L'UUID du congé
     * @param annotationRH L'annotation à ajouter
     * @return Le DTO du congé mis à jour, null si le congé n'existe pas
     */
    @Override
    @Transactional
    public CongeDto addAnnotationRH(UUID uuid, String annotationRH) {
        Conge conge = congeRepository.findByCongeUuid(uuid);
        if (conge == null) {
            return null;
        }

        conge.setAnnotationRH(annotationRH);
        Conge savedConge = congeRepository.save(conge);
        return mapCongeWithFiles(savedConge);
    }

    /**
     * Ajoute des justificatifs à un congé existant.
     * Seul l'employé propriétaire du congé peut ajouter des justificatifs.
     * Les justificatifs ne peuvent être ajoutés que si le congé est en attente.
     *
     * @param uuid UUID du congé
     * @param files Liste des fichiers justificatifs à ajouter
     * @return Le congé mis à jour avec les nouveaux justificatifs
     */
    @Override
    @Transactional
    public CongeDto addJustificatifsToConge(UUID uuid, List<MultipartFile> files) {
        // Vérifier que des fichiers sont fournis
        if (files == null || files.isEmpty()) {
            log.warn("Aucun fichier fourni pour l'ajout de justificatifs au congé {}", uuid);
            throw new ValidationException("Aucun fichier fourni");
        }

        // Récupérer le congé
        Conge conge = congeRepository.findByCongeUuid(uuid);
        if (conge == null) {
            log.error("Congé {} non trouvé", uuid);
            return null;
        }

        // Vérifier les permissions - seul le propriétaire peut ajouter des justificatifs
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            log.error("Aucun utilisateur authentifié");
            return null;
        }

        // Vérifier que l'utilisateur est le propriétaire du congé
        if (!conge.getUser().getId().equals(currentUser.getId())) {
            log.error("L'utilisateur {} n'est pas autorisé à modifier le congé {}", currentUser.getEmail(), uuid);
            throw new ValidationException("Vous n'êtes pas autorisé à modifier ce congé");
        }

        // Vérifier que le congé est en attente
        if (conge.getCongeStatus() != CongeStatus.ENATTENTE) {
            log.error("Le congé {} n'est pas en attente (statut: {})", uuid, conge.getCongeStatus());
            throw new ValidationException("Les justificatifs ne peuvent être ajoutés que pour les congés en attente");
        }

        // Ajouter les justificatifs via le service dédié
        log.info("Ajout de {} justificatif(s) au congé {} par l'utilisateur {}",
                files.size(), uuid, currentUser.getEmail());

        try {
            List<JustificatifDto> uploadedJustificatifs = justificatifService.uploadJustificatifs(uuid, files);
            log.info("{} justificatif(s) ajouté(s) avec succès au congé {}", uploadedJustificatifs.size(), uuid);

            // Recharger le congé pour obtenir les justificatifs mis à jour
            Conge updatedConge = congeRepository.findByCongeUuid(uuid);
            return mapCongeWithFiles(updatedConge);
        } catch (Exception e) {
            log.error("Erreur lors de l'ajout de justificatifs au congé {} : {}", uuid, e.getMessage());
            throw new ValidationException("Erreur lors de l'ajout des justificatifs : " + e.getMessage());
        }
    }

    /**
     * Récupère l'utilisateur actuellement authentifié depuis le contexte de sécurité Spring.
     * 
     * @return L'utilisateur authentifié, null si aucun utilisateur n'est authentifié
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            // Récupération de l'utilisateur complet depuis la base de données
            return userRepository.findByEmail(((User) principal).getEmail())
                    .orElse(null);
        }

        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            // Si c'est un UserDetails, récupérer l'utilisateur par email
            String email = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            return userRepository.findByEmail(email)
                    .orElse(null);
        }

        return null;
    }

    /**
     * Récupère les demandes de congés avec pagination et filtrage.
     *
     * @param status Filtre par statut (all, ENATTENTE, ACCEPTER, REFUSER)
     * @param searchTerm Terme de recherche (nom, prénom)
     * @param page Numéro de page (0-based)
     * @param size Taille de la page
     * @return Liste paginée des demandes de congés avec statistiques
     */
    @Override
    @Transactional(readOnly = true)
    public PaginatedLeaveRequestResponse getLeaveRequestsPaginated(
            String status, String searchTerm, int page, int size) {

        // Récupérer toutes les demandes
        List<Conge> allConges = congeRepository.findAll();

        // Filtrer par statut (gérer les mappings PENDING/APPROVED/REJECTED du frontend)
        List<Conge> filteredConges = allConges.stream()
                .filter(conge -> {
                    if (status == null || status.equals("all") || status.equalsIgnoreCase("all")) {
                        return true;
                    }
                    try {
                        // Mapper les statuts du frontend vers les statuts du backend
                        CongeStatus filterStatus = mapStatusFromFrontend(status);
                        return conge.getCongeStatus() == filterStatus;
                    } catch (IllegalArgumentException e) {
                        return true;
                    }
                })
                .filter(conge -> {
                    if (searchTerm == null || searchTerm.trim().isEmpty()) {
                        return true;
                    }
                    String search = searchTerm.toLowerCase().trim();
                    User user = conge.getUser();
                    if (user == null) {
                        return false;
                    }
                    return (user.getNom() != null && user.getNom().toLowerCase().contains(search)) ||
                           (user.getPrenom() != null && user.getPrenom().toLowerCase().contains(search));
                })
                .collect(Collectors.toList());

        // Calculer les statistiques globales
        long pendingCount = allConges.stream()
                .filter(c -> c.getCongeStatus() == CongeStatus.ENATTENTE)
                .count();
        long approvedCount = allConges.stream()
                .filter(c -> c.getCongeStatus() == CongeStatus.ACCEPTER)
                .count();
        long rejectedCount = allConges.stream()
                .filter(c -> c.getCongeStatus() == CongeStatus.REFUSER)
                .count();

        // Calculer la pagination
        long totalElements = filteredConges.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        // Extraire la page demandée
        int start = page * size;
        int end = Math.min(start + size, filteredConges.size());
        List<Conge> pageConges = filteredConges.subList(start, end);

        // Convertir en LeaveRequestDto
        List<LeaveRequestDto> leaveRequests = pageConges.stream()
                .map(this::mapCongeToLeaveRequestDto)
                .collect(Collectors.toList());

        return PaginatedLeaveRequestResponse.builder()
                .success(true)
                .message("Liste des demandes récupérée avec succès")
                .leaveRequests(leaveRequests)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .currentPage(page)
                .pageSize(size)
                .pendingCount(pendingCount)
                .approvedCount(approvedCount)
                .rejectedCount(rejectedCount)
                .build();
    }

    /**
     * Refuse un congé avec un motif de refus.
     * Le motif est stocké dans annotationRH.
     *
     * @param uuid UUID du congé
     * @param motifRefus Motif du refus
     * @return Le congé mis à jour
     */
    @Override
    @Transactional
    public CongeDto rejectCongeWithMotif(UUID uuid, String motifRefus) {
        Conge conge = congeRepository.findByCongeUuid(uuid);
        if (conge == null) {
            return null;
        }

        conge.setCongeStatus(CongeStatus.REFUSER);
        conge.setAnnotationRH(motifRefus);

        Conge savedConge = congeRepository.save(conge);
        return mapCongeWithFiles(savedConge);
    }

    /**
     * Convertit une entité Conge en LeaveRequestDto avec calcul des absences et retards.
     *
     * @param conge L'entité Conge à convertir
     * @return Le DTO LeaveRequestDto
     */
    private LeaveRequestDto mapCongeToLeaveRequestDto(Conge conge) {
        User user = conge.getUser();

        // Calculer les absences et retards de l'employé
        int nombreAbsences = 0;
        int nombreRetards = 0;

        if (user != null) {
            LocalDate now = LocalDate.now();
            LocalDate sixMonthsAgo = now.minusMonths(6);
            Date startDate = Date.from(sixMonthsAgo.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(now.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

            List<Pointage> pointages = pointageRepository.findByUserAndDateBetween(user, startDate, endDate);

            nombreAbsences = (int) pointages.stream()
                    .filter(p -> p.getHeureArrivee() == null && p.getHeureDepart() == null)
                    .count();

            nombreRetards = (int) pointages.stream()
                    .filter(p -> p.getStatut() == PointageStatus.RETARD)
                    .count();
        }

        // Créer EmployeeInfoDto
        EmployeeInfoDto employeeInfo = EmployeeInfoDto.builder()
                .id(user != null ? user.getId() : null)
                .nom(user != null ? user.getNom() : null)
                .prenom(user != null ? user.getPrenom() : null)
                .email(user != null ? user.getEmail() : null)
                .telephone(user != null ? user.getTelephone() : null)
                .nombreAbsences(nombreAbsences)
                .nombreRetards(nombreRetards)
                .build();

        // Créer TypeCongeDto
        TypeCongeDto typeCongeDto = null;
        if (conge.getTypeConge() != null) {
            TypeConge typeConge = conge.getTypeConge();
            typeCongeDto = TypeCongeDto.builder()
                    .id(typeConge.getId())
                    .libelle(typeConge.getLibelle())
                    .quotaAnnuel(typeConge.getQuotaAnnuel())
                    .description(typeConge.getDescription())
                    .build();
        }

        // Calculer la durée en jours
        int duree = 0;
        if (conge.getDateDebut() != null && conge.getDateFin() != null) {
            duree = (int) ChronoUnit.DAYS.between(conge.getDateDebut(), conge.getDateFin()) + 1;
        }

        // Déterminer le motif de refus (stocké dans annotationRH si le statut est REFUSER)
        String motifRefus = null;
        if (conge.getCongeStatus() == CongeStatus.REFUSER && conge.getAnnotationRH() != null) {
            motifRefus = conge.getAnnotationRH();
        }

        return LeaveRequestDto.builder()
                .id(conge.getId())
                .employe(employeeInfo)
                .typeConge(typeCongeDto)
                .dateDebut(conge.getDateDebut())
                .dateFin(conge.getDateFin())
                .duree(duree)
                .statut(conge.getCongeStatus())
                .dateDemande(conge.getDateSoumission())
                .commentaire(conge.getCommentaireEmployee())
                .motifRefus(motifRefus)
                .build();
    }

    /**
     * Mappe les statuts du frontend (PENDING, APPROVED, REJECTED) vers les statuts du backend.
     *
     * @param frontendStatus Statut du frontend (PENDING, APPROVED, REJECTED, pending, approved, rejected, ENATTENTE, ACCEPTER, REFUSER)
     * @return Le statut CongeStatus correspondant
     */
    private CongeStatus mapStatusFromFrontend(String frontendStatus) {
        if (frontendStatus == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        String upperStatus = frontendStatus.toUpperCase();
        switch (upperStatus) {
            case "PENDING":
            case "ENATTENTE":
                return CongeStatus.ENATTENTE;
            case "APPROVED":
            case "ACCEPTER":
                return CongeStatus.ACCEPTER;
            case "REJECTED":
            case "REFUSER":
                return CongeStatus.REFUSER;
            default:
                throw new IllegalArgumentException("Unknown status: " + frontendStatus);
        }
    }


    @Override
    @Transactional
    public CongeDto addCommentaireRH(UUID uuid, String commentaireRH) {
        Conge conge = congeRepository.findByCongeUuid(uuid);
        if (conge == null) {
            return null;
        }
        conge.setCommentaireEmployee(commentaireRH);
        Conge savedConge = congeRepository.save(conge);
        return mapCongeWithFiles(savedConge);
    }
}