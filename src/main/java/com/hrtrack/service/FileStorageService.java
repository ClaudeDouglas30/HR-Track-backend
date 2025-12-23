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
     * Stocke un fichier dans le répertoire de stockage.
     * 
     * @param file Le fichier à stocker
     * @param fileName Le nom du fichier (généralement un UUID)
     * @return Le chemin complet du fichier stocké
     * @throws com.hrtrack.exception.FileStorageException Si le stockage échoue
     */
    String storeFile(MultipartFile file, UUID fileName);
    
    /**
     * Charge un fichier depuis le répertoire de stockage.
     * 
     * @param fileName Le nom du fichier (UUID)
     * @return La ressource du fichier
     * @throws com.hrtrack.exception.FileStorageException Si le fichier n'existe pas
     */
    Resource loadFileAsResource(UUID fileName);
    
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
     * @return Le chemin du répertoire de stockage
     */
    Path getStorageLocation();
}

