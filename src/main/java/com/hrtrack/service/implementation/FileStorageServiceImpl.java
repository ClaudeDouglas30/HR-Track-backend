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
 * Gère le stockage, le chargement et la suppression des fichiers dans le répertoire storage/justificatifs/emailUser.
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
    public FileStorageServiceImpl(@Value("${file.justificatifs-dir:storage/justificatifs}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        
        try {
            // Créer le répertoire s'il n'existe pas
            Files.createDirectories(this.fileStorageLocation);
            log.info("Répertoire de stockage des justificatifs initialisé : {}", this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Impossible de créer le répertoire de stockage des justificatifs", ex);
        }
    }

    /**
     * Stocke un fichier dans le répertoire de stockage avec sous-dossier utilisateur.
     *
     * @param file Le fichier à stocker
     * @param fileName Le nom du fichier (UUID)
     * @param userEmail L'email de l'utilisateur
     * @return Le chemin complet du fichier stocké
     * @throws FileStorageException Si le stockage échoue
     */
    @Override
    public String storeFile(MultipartFile file, UUID fileName, String userEmail) {
        // Normaliser le nom du fichier
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        
        try {
            // Vérifier que le fichier ne contient pas de caractères invalides
            if (originalFilename.contains("..")) {
                throw new FileStorageException("Le nom du fichier contient un chemin invalide : " + originalFilename);
            }

            // Créer le sous-dossier pour l'utilisateur
            Path userDirectory = this.fileStorageLocation.resolve(userEmail);
            if (!Files.exists(userDirectory)) {
                Files.createDirectories(userDirectory);
                log.info("Répertoire utilisateur créé : {}", userDirectory);
            }

            // Obtenir l'extension du fichier original
            String extension = "";
            if (originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // Créer le nom de fichier final avec UUID + extension
            String finalFileName = fileName.toString() + extension;
            Path targetLocation = userDirectory.resolve(finalFileName);

            // Copier le fichier vers le répertoire de stockage
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("Fichier stocké avec succès : {} pour l'utilisateur {}", finalFileName, userEmail);
            return targetLocation.toString();
        } catch (IOException ex) {
            throw new FileStorageException("Impossible de stocker le fichier " + originalFilename, ex);
        }
    }

    /**
     * Stocke un fichier dans le répertoire de stockage (méthode legacy sans sous-dossier).
     */
    @Override
    public String storeFile(MultipartFile file, UUID fileName) {
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        try {
            String finalFileName = getString(fileName, originalFilename);
            Path targetLocation = this.fileStorageLocation.resolve(finalFileName);

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("Fichier stocké avec succès (legacy) : {}", finalFileName);
            return targetLocation.toString();
        } catch (IOException ex) {
            throw new FileStorageException("Impossible de stocker le fichier " + originalFilename, ex);
        }
    }

    private static String getString(UUID fileName, String originalFilename) {
        if (originalFilename.contains("..")) {
            throw new FileStorageException("Le nom du fichier contient un chemin invalide : " + originalFilename);
        }

        String extension = "";
        if (originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        return fileName.toString() + extension;
    }

    /**
     * Charge un fichier depuis le répertoire de stockage avec sous-dossier utilisateur.
     */
    @Override
    public Resource loadFileAsResource(UUID fileName, String userEmail) {
        try {
            Path userDirectory = this.fileStorageLocation.resolve(userEmail);
            Path filePath = findFileWithExtension(fileName, userDirectory);

            if (filePath == null) {
                throw new FileStorageException("Fichier non trouvé : " + fileName + " pour l'utilisateur " + userEmail);
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileStorageException("Le fichier existe mais n'est pas lisible : " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException("Fichier non trouvé : " + fileName, ex);
        }
    }

    /**
     * Charge un fichier depuis le répertoire de stockage (méthode legacy).
     */
    @Override
    public Resource loadFileAsResource(UUID fileName) {
        try {
            Path filePath = findFileWithExtension(fileName, this.fileStorageLocation);

            if (filePath == null) {
                throw new FileStorageException("Fichier non trouvé : " + fileName);
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileStorageException("Le fichier existe mais n'est pas lisible : " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException("Fichier non trouvé : " + fileName, ex);
        }
    }

    /**
     * Trouve un fichier avec son UUID, en essayant différentes extensions.
     */
    private Path findFileWithExtension(UUID fileName, Path directory) {
        String[] commonExtensions = {".pdf", ".jpg", ".jpeg", ".png", ".doc", ".docx", ".txt"};
        
        // D'abord essayer sans extension
        Path filePath = directory.resolve(fileName.toString());
        if (Files.exists(filePath)) {
            return filePath;
        }
        
        // Essayer avec les extensions communes
        for (String ext : commonExtensions) {
            filePath = directory.resolve(fileName.toString() + ext);
            if (Files.exists(filePath)) {
                return filePath;
            }
        }
        
        return null;
    }

    /**
     * Supprime un fichier du répertoire de stockage avec sous-dossier utilisateur.
     */
    @Override
    public boolean deleteFile(UUID fileName, String userEmail) {
        try {
            Path userDirectory = this.fileStorageLocation.resolve(userEmail);
            Path filePath = findFileWithExtension(fileName, userDirectory);

            if (filePath == null) {
                log.warn("Fichier non trouvé pour suppression : {} pour l'utilisateur {}", fileName, userEmail);
                return false;
            }

            Files.delete(filePath);
            log.info("Fichier supprimé avec succès : {} pour l'utilisateur {}", fileName, userEmail);
            return true;
        } catch (IOException ex) {
            log.error("Erreur lors de la suppression du fichier : {}", fileName, ex);
            return false;
        }
    }

    /**
     * Supprime un fichier du répertoire de stockage (méthode legacy).
     */
    @Override
    public boolean deleteFile(UUID fileName) {
        try {
            Path filePath = findFileWithExtension(fileName, this.fileStorageLocation);

            if (filePath == null) {
                log.warn("Fichier non trouvé pour suppression : {}", fileName);
                return false;
            }

            Files.delete(filePath);
            log.info("Fichier supprimé avec succès (legacy) : {}", fileName);
            return true;
        } catch (IOException ex) {
            log.error("Erreur lors de la suppression du fichier : {}", fileName, ex);
            return false;
        }
    }

    @Override
    public boolean fileExists(UUID fileName) {
        Path filePath = findFileWithExtension(fileName, this.fileStorageLocation);
        return filePath != null;
    }

    @Override
    public Path getStorageLocation() {
        return this.fileStorageLocation;
    }

    /**
     * Charge un fichier justificatif par son nom (UUID) depuis tous les sous-dossiers utilisateur de storage/justificatifs.
     * Recherche dans chaque sous-dossier jusqu'à trouver le fichier correspondant à l'UUID (avec ou sans extension).
     */
    @Override
    public Resource loadJustificatifAsResource(UUID uuid) {
        try {
            // Parcourir tous les sous-dossiers (emails) dans storage/justificatifs
            try (var userDirs = Files.list(this.fileStorageLocation)) {
                for (Path userDir : userDirs.toList()) {
                    if (Files.isDirectory(userDir)) {
                        // Chercher le fichier avec ou sans extension dans ce dossier
                        Path filePath = findFileWithExtension(String.valueOf(uuid), userDir);
                        if (filePath != null && Files.exists(filePath)) {
                            Resource resource = new UrlResource(filePath.toUri());
                            if (resource.exists() && resource.isReadable()) {
                                return resource;
                            }
                        }
                    }
                }
            }
            // Si non trouvé
            return null;
        } catch (IOException e) {
            log.error("Erreur lors de la recherche du justificatif {} dans les sous-dossiers", uuid, e);
            return null;
        }
    }

    /**
     * Cherche un fichier par UUID (avec ou sans extension) dans un dossier donné.
     */
    private Path findFileWithExtension(String uuid, Path directory) {
        String[] commonExtensions = {".pdf", ".jpg", ".jpeg", ".png", ".doc", ".docx", ".txt"};
        // Essayer sans extension
        Path filePath = directory.resolve(uuid);
        if (Files.exists(filePath)) {
            return filePath;
        }
        // Essayer avec extensions
        for (String ext : commonExtensions) {
            filePath = directory.resolve(uuid + ext);
            if (Files.exists(filePath)) {
                return filePath;
            }
        }
        return null;
    }
}