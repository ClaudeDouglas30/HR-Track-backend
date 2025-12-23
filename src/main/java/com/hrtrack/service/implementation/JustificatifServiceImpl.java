package com.hrtrack.service.implementation;

import com.hrtrack.domain.dtos.JustificatifDto;
import com.hrtrack.domain.entities.Conge;
import com.hrtrack.domain.entities.Justificatif;
import com.hrtrack.exception.FileStorageException;
import com.hrtrack.exception.ValidationException;
import com.hrtrack.mappers.JustificatifMapper;
import com.hrtrack.repository.CongeRepository;
import com.hrtrack.repository.JustificatifRepository;
import com.hrtrack.service.FileStorageService;
import com.hrtrack.service.JustificatifService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implémentation du service de gestion des justificatifs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JustificatifServiceImpl implements JustificatifService {

    private final JustificatifRepository justificatifRepository;
    private final CongeRepository congeRepository;
    private final FileStorageService fileStorageService;
    private final JustificatifMapper justificatifMapper;

    /**
     * Upload un justificatif pour un congé.
     * 
     * @param congeUuid L'UUID du congé
     * @param file Le fichier à uploader
     * @return Le DTO du justificatif créé
     */
    @Override
    @Transactional
    public JustificatifDto uploadJustificatif(UUID congeUuid, MultipartFile file) {
        // Vérifier que le fichier n'est pas vide
        if (file == null || file.isEmpty()) {
            throw new ValidationException("Le fichier ne peut pas être vide");
        }

        // Vérifier la taille du fichier (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new ValidationException("La taille du fichier ne peut pas dépasser 10MB");
        }

        // Récupérer le congé
        Conge conge = congeRepository.findByCongeUuid(congeUuid);
        if (conge == null) {
            throw new ValidationException("Le congé spécifié n'existe pas");
        }

        // Vérifier que le congé peut encore recevoir des justificatifs (seulement si ENATTENTE)
        if (conge.getCongeStatus() != com.hrtrack.domain.CongeStatus.ENATTENTE) {
            throw new ValidationException("Les justificatifs ne peuvent être ajoutés que pour les congés en attente");
        }

        try {
            // Générer un UUID pour le fichier
            UUID fileUuid = UUID.randomUUID();

            // Stocker le fichier
            fileStorageService.storeFile(file, fileUuid);

            // Créer l'entité Justificatif
            Justificatif justificatif = Justificatif.builder()
                    .nomFichier(fileUuid)
                    .conge(conge)
                    .build();

            // Sauvegarder en base
            Justificatif savedJustificatif = justificatifRepository.save(justificatif);

            log.info("Justificatif uploadé avec succès pour le congé : {}", congeUuid);

            // Mapper vers DTO
            JustificatifDto dto = justificatifMapper.toDto(savedJustificatif);
            dto.setUrlFichier(fileUuid.toString());
            
            return dto;
        } catch (FileStorageException e) {
            log.error("Erreur lors du stockage du fichier", e);
            throw new ValidationException("Erreur lors du stockage du fichier : " + e.getMessage());
        }
    }

    /**
     * Récupère tous les justificatifs d'un congé.
     * 
     * @param congeUuid L'UUID du congé
     * @return Liste des justificatifs
     */
    @Override
    @Transactional(readOnly = true)
    public List<JustificatifDto> getJustificatifsByConge(UUID congeUuid) {
        Conge conge = congeRepository.findByCongeUuid(congeUuid);
        if (conge == null) {
            throw new ValidationException("Le congé spécifié n'existe pas");
        }

        List<Justificatif> justificatifs = justificatifRepository.findByCongeId(conge.getId());

        return justificatifs.stream()
                .map(justificatif -> {
                    JustificatifDto dto = justificatifMapper.toDto(justificatif);
                    dto.setUrlFichier(justificatif.getNomFichier().toString());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Supprime un justificatif.
     * 
     * @param justificatifUuid L'UUID du justificatif
     * @return true si la suppression a réussi
     */
    @Override
    @Transactional
    public boolean deleteJustificatif(UUID justificatifUuid) {
        Justificatif justificatif = justificatifRepository.findByNomFichier(justificatifUuid)
                .orElse(null);

        if (justificatif == null) {
            return false;
        }

        // Vérifier que le congé peut encore être modifié
        if (justificatif.getConge().getCongeStatus() != com.hrtrack.domain.CongeStatus.ENATTENTE) {
            throw new ValidationException("Les justificatifs ne peuvent être supprimés que pour les congés en attente");
        }

        try {
            // Supprimer le fichier du système de fichiers
            fileStorageService.deleteFile(justificatifUuid);

            // Supprimer l'entité de la base de données
            justificatifRepository.delete(justificatif);

            log.info("Justificatif supprimé avec succès : {}", justificatifUuid);
            return true;
        } catch (Exception e) {
            log.error("Erreur lors de la suppression du justificatif : {}", justificatifUuid, e);
            return false;
        }
    }
}

