package com.hrtrack.controllers;

import com.hrtrack.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Contrôleur pour servir les fichiers statiques (photos de profil)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    /**
     * Sert les photos de profil
     * GET /api/v1/files/profiles/{filename}
     */
    @GetMapping("/profiles/{filename:.+}")
    public ResponseEntity<Resource> serveProfilePicture(@PathVariable String filename) {
        try {
            Path filePath = Paths.get("storage/profiles").resolve(filename).normalize();

            if (!Files.exists(filePath)) {
                log.warn("Photo de profil introuvable : {}", filename);
                return ResponseEntity.notFound().build();
            }

            Resource resource = new org.springframework.core.io.UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.warn("Photo de profil non lisible : {}", filename);
                return ResponseEntity.notFound().build();
            }

            // Déterminer le type de contenu
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (IOException e) {
            log.error("Erreur lors du chargement de la photo de profil : {}", filename, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Sert les justificatifs
     * GET /api/v1/files/justificatifs/{filename}
     */
    @GetMapping("/justificatifs/{filename:.+}")
    public ResponseEntity<Resource> serveJustificatif(@PathVariable String filename) {
        try {
            Path filePath = Paths.get("storage/justificatifs").resolve(filename).normalize();

            if (!Files.exists(filePath)) {
                log.warn("Justificatif introuvable : {}", filename);
                return ResponseEntity.notFound().build();
            }

            Resource resource = new org.springframework.core.io.UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.warn("Justificatif non lisible : {}", filename);
                return ResponseEntity.notFound().build();
            }

            // Déterminer le type de contenu
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (IOException e) {
            log.error("Erreur lors du chargement du justificatif : {}", filename, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}