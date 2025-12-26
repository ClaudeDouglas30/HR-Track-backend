package com.hrtrack.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.UUID;

/**
 * Service pour gérer le stockage des fichiers justificatifs.
 */
public interface FileStorageService {
    
    /**
     * Stocke un fichier dans le répertoire de stockage avec un sous-dossier utilisateur.
     *
     * @param file Le fichier à stocker
     * @param fileName Le nom du fichier (généralement un UUID)
     * @param userEmail L'email de l'utilisateur pour organiser les fichiers
     * @return Le chemin complet du fichier stocké
     * @throws com.hrtrack.exception.FileStorageException Si le stockage échoue
     */
    String storeFile(MultipartFile file, UUID fileName, String userEmail);

    /**
     * Stocke un fichier dans le répertoire de stockage (méthode legacy).
     *
     * @param file Le fichier à stocker
     * @param fileName Le nom du fichier (UUID)
     * @return Le chemin complet du fichier stocké
     * @throws com.hrtrack.exception.FileStorageException Si le stockage échoue
     */
    String storeFile(MultipartFile file, UUID fileName);
    
    /**
     * Charge un fichier depuis le répertoire de stockage avec sous-dossier utilisateur.
     *
     * @param fileName Le nom du fichier (UUID)
     * @param userEmail L'email de l'utilisateur
     * @return La ressource du fichier
     * @throws com.hrtrack.exception.FileStorageException Si le fichier n'existe pas
     */
    Resource loadFileAsResource(UUID fileName, String userEmail);

    /**
     * Charge un fichier depuis le répertoire de stockage.
     * 
     * @param fileName Le nom du fichier (UUID)
     * @return La ressource du fichier
     * @throws com.hrtrack.exception.FileStorageException Si le fichier n'existe pas
     */
    Resource loadFileAsResource(UUID fileName);
    
    /**
     * Supprime un fichier du répertoire de stockage avec sous-dossier utilisateur.
     *
     * @param fileName Le nom du fichier (UUID) à supprimer
     * @param userEmail L'email de l'utilisateur
     * @return true si la suppression a réussi, false sinon
     */
    boolean deleteFile(UUID fileName, String userEmail);

    /**
     * Supprime un fichier du répertoire de stockage.
     * 
     * @param fileName Le nom du fichier (UUID) à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    boolean deleteFile(UUID fileName);
    
    /**
     * Vérifie si un fichier existe.
     * 
     * @param fileName Le nom du fichier (UUID)
     * @return true si le fichier existe, false sinon
     */
    boolean fileExists(UUID fileName);
    
    /**
     * Obtient le chemin du répertoire de stockage.
     * 
     * @return Le chemin du répertoire
     */
    Path getStorageLocation();

    /**
     * Charge un justificatif par son nom (UUID).
     * @param uuid L'UUID du fichier justificatif
     * @return La ressource du fichier ou null si non trouvé
     */
    Resource loadJustificatifAsResource(UUID uuid);
}