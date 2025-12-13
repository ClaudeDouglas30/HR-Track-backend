package com.hrtrack.controllers;


import com.hrtrack.domain.CongeStatus;
import com.hrtrack.domain.dtos.CongeDto;
import com.hrtrack.domain.dtos.JustificatifDto;
import com.hrtrack.domain.responses.conge.CongeResponse;
import com.hrtrack.domain.responses.conge.JustificatifResponse;
import com.hrtrack.domain.responses.conge.ListCongeResponse;
import com.hrtrack.domain.responses.conge.ListJustificatifResponse;
import com.hrtrack.service.CongeService;
import com.hrtrack.service.FileStorageService;
import com.hrtrack.service.JustificatifService;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/conges")
public class CongeController {

    private final CongeService congeService;
    private final JustificatifService justificatifService;
    private final FileStorageService fileStorageService;

    @RolesAllowed({"RH", "ADMIN"})
    @GetMapping
    public ResponseEntity<ListCongeResponse> getAllListOfConge() {

        List<CongeDto> congeDtoList = congeService.getAllConge();

        return ResponseEntity.status(HttpStatus.OK).body(
                new ListCongeResponse(
                        congeDtoList != null,
                        congeDtoList
                )
        );
    }

    @RolesAllowed({"EMPLOYEE", "RH", "ADMIN"})
    @GetMapping("/{id}")
    public ResponseEntity<CongeResponse> getCongeById(@PathVariable UUID id) {
        CongeDto conge = congeService.getCongeById(id);

        if (conge == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                new CongeResponse(
                        true,
                        conge
                )
        );
    }

    @RolesAllowed({"RH", "ADMIN"})
    @PutMapping("/{id}/accept")
    public ResponseEntity<CongeResponse> acceptCongeById(@PathVariable UUID id) {
        CongeDto conge = congeService.changeCongeStatus(id, CongeStatus.ACCEPTER);

        if (conge == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                new CongeResponse(
                        true,
                        conge
                )
        );
    }

    @RolesAllowed({"RH", "ADMIN"})
    @PutMapping("/{id}/refuse")
    public ResponseEntity<CongeResponse> refusedCongeById(@PathVariable UUID id) {
        CongeDto conge = congeService.changeCongeStatus(id, CongeStatus.REFUSER);

        if (conge == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                new CongeResponse(
                        true,
                        conge
                )
        );
    }


    @RolesAllowed({"EMPLOYEE", "RH"})
    @GetMapping("/my-list")
    public ResponseEntity<ListCongeResponse> getListDeMesConges() {
        List<CongeDto> congeDtoList = congeService.getMyListOfConge();

        return ResponseEntity.status(HttpStatus.OK).body(
                new ListCongeResponse(
                        congeDtoList != null,
                        congeDtoList
                )
        );
    }


    /**
     * Crée une demande de congé avec des fichiers justificatifs.
     * Accepte multipart/form-data avec le DTO du congé et les fichiers.
     */
    @RolesAllowed({"EMPLOYEE", "RH"})
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CongeResponse> faireDemandeDeConge(
            @RequestPart("conge") CongeDto congeDto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        CongeDto conge = congeService.createConge(congeDto, files);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new CongeResponse(
                        conge != null,
                        conge
                )
        );
    }

    /**
     * Crée une demande de congé sans fichiers (compatibilité).
     * Accepte application/json.
     */
    @RolesAllowed({"EMPLOYEE", "RH"})
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CongeResponse> faireDemandeDeCongeJson(@RequestBody CongeDto congeDto) {
        CongeDto conge = congeService.createConge(congeDto, null);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new CongeResponse(
                        conge != null,
                        conge
                )
        );
    }

    /**
     * Met à jour un congé avec des fichiers justificatifs.
     * Accepte multipart/form-data avec le DTO du congé et les fichiers.
     */
    @RolesAllowed({"EMPLOYEE", "RH"})
    @PutMapping(value = "/{id}/edit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CongeResponse> editCongeById(
            @PathVariable UUID id,
            @RequestPart("conge") CongeDto congeDto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        CongeDto conge = congeService.editConge(id, congeDto, files);

        return ResponseEntity.status(HttpStatus.OK).body(
                new CongeResponse(
                        conge != null,
                        conge
                )
        );
    }

    /**
     * Met à jour un congé sans fichiers (compatibilité).
     * Accepte application/json.
     */
    @RolesAllowed({"EMPLOYEE", "RH"})
    @PutMapping(value = "/{id}/edit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CongeResponse> editCongeByIdJson(
            @PathVariable UUID id,
            @RequestBody CongeDto congeDto) {
        CongeDto conge = congeService.editConge(id, congeDto, null);

        return ResponseEntity.status(HttpStatus.OK).body(
                new CongeResponse(
                        conge != null,
                        conge
                )
        );
    }

    @RolesAllowed({"EMPLOYEE", "RH"})
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<CongeResponse> deleteCongeById(@PathVariable UUID id) {
        boolean deleted = congeService.deleteConge(id);

        return ResponseEntity.status(HttpStatus.OK).body(
                new CongeResponse(
                        deleted,
                        deleted ? congeService.getCongeById(id) : null
                )
        );
    }

    /**
     * Upload un justificatif pour un congé.
     * 
     * @param congeUuid L'UUID du congé
     * @param file Le fichier à uploader
     * @return Le justificatif créé
     */
    @RolesAllowed({"EMPLOYEE", "RH"})
    @PostMapping("/{congeUuid}/justificatifs")
    public ResponseEntity<JustificatifResponse> uploadJustificatif(
            @PathVariable UUID congeUuid,
            @RequestParam("file") MultipartFile file) {
        
        JustificatifDto justificatif = justificatifService.uploadJustificatif(congeUuid, file);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new JustificatifResponse(
                        justificatif != null,
                        "Justificatif uploadé avec succès",
                        justificatif
                )
        );
    }

    /**
     * Récupère tous les justificatifs d'un congé.
     * 
     * @param congeUuid L'UUID du congé
     * @return Liste des justificatifs
     */
    @RolesAllowed({"EMPLOYEE", "RH"})
    @GetMapping("/{congeUuid}/justificatifs")
    public ResponseEntity<ListJustificatifResponse> getJustificatifsByConge(
            @PathVariable UUID congeUuid) {
        
        List<JustificatifDto> justificatifs = justificatifService.getJustificatifsByConge(congeUuid);
        
        return ResponseEntity.status(HttpStatus.OK).body(
                new ListJustificatifResponse(
                        true,
                        "Liste des justificatifs récupérée avec succès",
                        justificatifs
                )
        );
    }

    /**
     * Télécharge un justificatif par son UUID.
     * 
     * @param congeUuid L'UUID du congé
     * @param justificatifUuid L'UUID du justificatif
     * @return Le fichier à télécharger
     */
    @RolesAllowed({"EMPLOYEE", "RH"})
    @GetMapping("/{congeUuid}/justificatifs/{justificatifUuid}/download")
    public ResponseEntity<Resource> downloadJustificatif(
            @PathVariable UUID congeUuid,
            @PathVariable UUID justificatifUuid) {
        try {
            Resource resource = fileStorageService.loadFileAsResource(justificatifUuid);
            
            String contentType = "application/octet-stream";
            if (resource.getFilename() != null) {
                String filename = resource.getFilename().toLowerCase();
                if (filename.endsWith(".pdf")) {
                    contentType = "application/pdf";
                } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (filename.endsWith(".png")) {
                    contentType = "image/png";
                } else if (filename.endsWith(".doc")) {
                    contentType = "application/msword";
                } else if (filename.endsWith(".docx")) {
                    contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                }
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Supprime un justificatif.
     * 
     * @param congeUuid L'UUID du congé
     * @param justificatifUuid L'UUID du justificatif
     * @return Réponse de succès ou d'échec
     */
    @RolesAllowed({"EMPLOYEE"})
    @DeleteMapping("/{congeUuid}/justificatifs/{justificatifUuid}")
    public ResponseEntity<JustificatifResponse> deleteJustificatif(
            @PathVariable UUID congeUuid,
            @PathVariable UUID justificatifUuid) {
        
        boolean deleted = justificatifService.deleteJustificatif(justificatifUuid);
        
        return ResponseEntity.status(HttpStatus.OK).body(
                new JustificatifResponse(
                        deleted,
                        deleted ? "Justificatif supprimé avec succès" : "Erreur lors de la suppression",
                        null
                )
        );
    }

}
