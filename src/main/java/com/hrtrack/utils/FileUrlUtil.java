package com.hrtrack.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utilitaire pour convertir les chemins de fichiers locaux en URLs HTTP accessibles
 */
@Component
public class FileUrlUtil {

    @Value("${application.server.base-url}")
    private String serverBaseUrl;

    /**
     * Convertit un chemin de fichier local en URL HTTP
     *
     * @param filePath Le chemin local du fichier (ex: storage/profiles/profile_1_uuid.jpg)
     * @return L'URL HTTP accessible (ex: http://192.168.11.104:8080/api/v1/files/profiles/profile_1_uuid.jpg)
     */
    public String convertToUrl(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }

        try {
            Path path = Paths.get(filePath);
            String fileName = path.getFileName().toString();

            // Déterminer le type de fichier (profile ou justificatif)
            String fileType;
            if (filePath.contains("profiles")) {
                fileType = "profiles";
            } else if (filePath.contains("justificatifs")) {
                fileType = "justificatifs";
            } else {
                return null;
            }

            // Construire l'URL HTTP en utilisant la base URL configurée
            return String.format("%s/api/v1/files/%s/%s",
                    serverBaseUrl, fileType, fileName);

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extrait le nom du fichier depuis un chemin complet
     *
     * @param filePath Le chemin complet du fichier
     * @return Le nom du fichier uniquement
     */
    public String extractFileName(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }

        Path path = Paths.get(filePath);
        return path.getFileName().toString();
    }
}