package com.hrtrack.service.implementation;

import com.hrtrack.exception.FileStorageException;
import com.hrtrack.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

/**
 * Implémentation du service de stockage des fichiers justificatifs.
 * Gère le stockage, le chargement et la suppression des fichiers dans le répertoire storage/justificatifs.
 */
@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;

    /**
     * Constructeur qui initialise le répertoire de stockage.
     * 
     * @param uploadDir Le répertoire de base pour le stockage (configuré dans application.properties)
     */
    public FileStorageServiceImpl(@Value("${file.upload-dir:storage/justificatifs}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        
        try {
            // Créer le répertoire s'il n'existe pas
            Files.createDirectories(this.fileStorageLocation);
            log.info("Répertoire de stockage initialisé : {}", this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Impossible de créer le répertoire de stockage", ex);
        }
    }

    /**
     * Stocke un fichier dans le répertoire de stockage.
     * 
     * @param file Le fichier à stocker
     * @param fileName Le nom du fichier (UUID)
     * @return Le chemin complet du fichier stocké
     * @throws FileStorageException Si le stockage échoue
     */
    @Override
    public String storeFile(MultipartFile file, UUID fileName) {
        // Normaliser le nom du fichier
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        
        try {
            // Vérifier que le fichier ne contient pas de caractères invalides
            String finalFileName = getString(fileName, originalFilename);
            Path targetLocation = this.fileStorageLocation.resolve(finalFileName);

            // Copier le fichier vers le répertoire de stockage
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("Fichier stocké avec succès : {}", finalFileName);
            return targetLocation.toString();
        } catch (IOException ex) {
            throw new FileStorageException("Impossible de stocker le fichier " + originalFilename, ex);
        }
    }

    private static String getString(UUID fileName, String originalFilename) {
        if (originalFilename.contains("..")) {
            throw new FileStorageException(STR."Le nom du fichier contient un chemin invalide : \{originalFilename}");
        }

        // Obtenir l'extension du fichier original
        String extension = "";
        if (originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // Créer le nom de fichier final avec UUID + extension
        return fileName.toString() + extension;
    }

    /**
     * Charge un fichier depuis le répertoire de stockage.
     * 
     * @param fileName Le nom du fichier (UUID)
     * @return La ressource du fichier
     * @throws FileStorageException Si le fichier n'existe pas
     */
    @Override
    public Resource loadFileAsResource(UUID fileName) {
        try {
            // Chercher le fichier avec toutes les extensions possibles
            Path filePath = findFileWithExtension(fileName);
            
            if (filePath == null) {
                throw new FileStorageException(STR."Fichier non trouvé : \{fileName}");
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileStorageException(STR."Le fichier existe mais n'est pas lisible : \{fileName}");
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException(STR."Fichier non trouvé : \{fileName}", ex);
        }
    }

    /**
     * Trouve un fichier avec son UUID, en essayant différentes extensions.
     * 
     * @param fileName L'UUID du fichier
     * @return Le chemin du fichier trouvé, ou null si non trouvé
     */
    private Path findFileWithExtension(UUID fileName) {
        // Extensions communes à essayer
        String[] commonExtensions = {".pdf", ".jpg", ".jpeg", ".png", ".doc", ".docx", ".txt"};
        
        // D'abord essayer sans extension
        Path filePath = this.fileStorageLocation.resolve(fileName.toString());
        if (Files.exists(filePath)) {
            return filePath;
        }
        
        // Essayer avec les extensions communes
        for (String ext : commonExtensions) {
            filePath = this.fileStorageLocation.resolve(fileName.toString() + ext);
            if (Files.exists(filePath)) {
                return filePath;
            }
        }
        
        return null;
    }

    /**
     * Supprime un fichier du répertoire de stockage.
     * 
     * @param fileName Le nom du fichier (UUID) à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    @Override
    public boolean deleteFile(UUID fileName) {
        try {
            Path filePath = findFileWithExtension(fileName);
            
            if (filePath != null && Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Fichier supprimé avec succès : {}", fileName);
                return true;
            }
            
            return false;
        } catch (IOException ex) {
            log.error("Erreur lors de la suppression du fichier : {}", fileName, ex);
            return false;
        }
    }

    /**
     * Vérifie si un fichier existe.
     * 
     * @param fileName Le nom du fichier (UUID)
     * @return true si le fichier existe, false sinon
     */
    @Override
    public boolean fileExists(UUID fileName) {
        Path filePath = findFileWithExtension(fileName);
        return filePath != null && Files.exists(filePath);
    }

    /**
     * Obtient le chemin du répertoire de stockage.
     * 
     * @return Le chemin du répertoire de stockage
     */
    @Override
    public Path getStorageLocation() {
        return this.fileStorageLocation;
    }
}

