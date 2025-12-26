package com.hrtrack.service;

import com.hrtrack.domain.dtos.JustificatifDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Service pour gérer les justificatifs de congés.
 */
public interface JustificatifService {
    
    /**
     * Upload un justificatif pour un congé.
     * 
     * @param congeUuid L'UUID du congé
     * @param file Le fichier à uploader
     * @return Le DTO du justificatif créé
     */
    JustificatifDto uploadJustificatif(UUID congeUuid, MultipartFile file);
    
    /**
     * Upload plusieurs justificatifs pour un congé.
     *
     * @param congeUuid L'UUID du congé
     * @param files Les fichiers à uploader
     * @return Liste des DTOs des justificatifs créés
     */
    List<JustificatifDto> uploadJustificatifs(UUID congeUuid, List<MultipartFile> files);

    /**
     * Récupère tous les justificatifs d'un congé.
     * 
     * @param congeUuid L'UUID du congé
     * @return Liste des justificatifs
     */
    List<JustificatifDto> getJustificatifsByConge(UUID congeUuid);
    
    /**
     * Supprime un justificatif.
     * 
     * @param justificatifUuid L'UUID du justificatif
     * @return true si la suppression a réussi
     */
    boolean deleteJustificatif(UUID justificatifUuid);

    /**
     * Récupère un justificatif par son UUID (nom de fichier).
     * @param uuid L'UUID du justificatif
     * @return Le DTO du justificatif ou null si non trouvé
     */
    JustificatifDto getJustificatifByUuid(UUID uuid);
}