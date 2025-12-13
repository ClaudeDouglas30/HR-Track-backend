package com.hrtrack.service.implementation;

import com.hrtrack.domain.CongeStatus;
import com.hrtrack.domain.dtos.CongeDto;
import com.hrtrack.domain.dtos.JustificatifDto;
import com.hrtrack.domain.entities.Conge;
import com.hrtrack.domain.entities.TypeConge;
import com.hrtrack.domain.entities.User;
import com.hrtrack.domain.validation.CongeValidator;
import com.hrtrack.exception.ValidationException;
import com.hrtrack.mappers.CongeMapper;
import com.hrtrack.repository.CongeRepository;
import com.hrtrack.repository.TypeCongeRepository;
import com.hrtrack.repository.UserRepository;
import com.hrtrack.service.CongeService;
import com.hrtrack.service.JustificatifService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
     * @param uuid L'UUID du congé à récupérer
     * @return Le DTO du congé trouvé, null si non trouvé
     */
    @Override
    @Transactional(readOnly = true)
    public CongeDto getCongeById(UUID uuid) {
        Conge conge = congeRepository.findByCongeUuid(uuid);
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
                TypeConge typeConge = typeCongeRepository.findByLibelle(congeDto.getTypeConge().getLibelle())
                        .orElse(null);
                if (typeConge != null) {
                    conge.setTypeConge(typeConge);
                }
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
                    .orElse(null);

            // Création de l'entité Conge à partir du DTO
            Conge conge = congeMapper.toEntity(congeDto);
            if (conge == null) {
                conge = new Conge();
            }
            
            // Configuration des champs obligatoires et spécifiques
            conge.setDateDebut(congeDto.getDateDebut());
            conge.setDateFin(congeDto.getDateFin());
            conge.setCongeStatus(CongeStatus.ENATTENTE);
            conge.setDateSoumission(LocalDateTime.now());
            conge.setUser(currentUser);
            conge.setTypeConge(typeConge);
            conge.setJustificatif("");

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

            // Suppression du congé
            congeRepository.delete(conge);
            return true;
        } catch (ValidationException e) {
            // Log l'erreur de validation si nécessaire
            return false;
        }
    }

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
}
