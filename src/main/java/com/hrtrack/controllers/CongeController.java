package com.hrtrack.controllers;


import com.hrtrack.domain.CongeStatus;
import com.hrtrack.domain.Libelle;
import com.hrtrack.domain.dtos.CongeDto;
import com.hrtrack.domain.dtos.TypeCongeDto;
import com.hrtrack.domain.entities.User;
import com.hrtrack.domain.responses.conge.CongeResponse;
import com.hrtrack.domain.responses.conge.CreateResponse;
import com.hrtrack.domain.responses.conge.ListCongeResponse;
import com.hrtrack.service.CongeService;
import com.hrtrack.service.FileStorageService;
import com.hrtrack.service.JustificatifService;
import com.hrtrack.service.implementation.CongeServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/conges")
public class CongeController {

    private final CongeService congeService;
    private final JustificatifService justificatifService;
    private final FileStorageService fileStorageService;


    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreateResponse> creerDemande(
            @RequestParam("typeConge") String typeConge,
            @RequestParam("dateDebut") String dateDebut,
            @RequestParam("dateFin") String dateFin,
            @RequestParam("nombreJours") String nombreJours,
            @RequestParam("motif") String motif,
            @RequestParam(value = "remplacant", required = false) String remplacant,
            @RequestParam("userId") String userId,
            @RequestPart(value = "justificatifs", required = false) List<MultipartFile> justificatifs
    ) {
        try {
            log.info("=== Création de congé ===");
            log.info("Type: {}, Dates: {} -> {}", typeConge, dateDebut, dateFin);
            log.info("Nombre de fichiers justificatifs reçus: {}", justificatifs != null ? justificatifs.size() : "NULL");

            if (justificatifs != null && !justificatifs.isEmpty()) {
                for (MultipartFile file : justificatifs) {
                    log.info(">>> Fichier reçu: {}, Taille: {} bytes, isEmpty: {}",
                            file.getOriginalFilename(),
                            file.getSize(),
                            file.isEmpty());
                }
            } else {
                log.warn("!!! AUCUN FICHIER REÇU - Le paramètre 'justificatifs' est NULL ou vide !!!");
            }

            // Convertir les paramètres en CongeDto pour utiliser la méthode unifiée createConge
            LocalDate dateDebutParsed = LocalDate.parse(dateDebut, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate dateFinParsed = LocalDate.parse(dateFin, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // Créer le TypeCongeDto avec le builder
            TypeCongeDto typeCongeDto = TypeCongeDto.builder()
                    .libelle(com.hrtrack.domain.Libelle.valueOf(typeConge))
                    .build();

            // Créer le CongeDto avec le builder
            CongeDto congeDto = CongeDto.builder()
                    .dateDebut(dateDebutParsed)
                    .dateFin(dateFinParsed)
                    .typeConge(typeCongeDto)
                    .commentaireEmployee(motif)
                    .build();

            log.info("Appel du service createConge avec {} fichier(s)", justificatifs != null ? justificatifs.size() : 0);

            // Appeler le service unifié pour créer la demande
            CongeDto createdConge = congeService.createConge(congeDto, justificatifs);

            if (createdConge == null) {
                log.error("Le service createConge a retourné NULL");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        new CreateResponse(
                                false,
                                "Erreur lors de la création de la demande. Vérifiez vos données."
                        )
                );
            }

            log.info(">>> Congé créé avec succès, UUID: {}, Justificatifs: {}",
                    createdConge.getCongeUuid(),
                    createdConge.getJustificatifs() != null ? createdConge.getJustificatifs().size() : 0);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new CreateResponse(
                            true,
                            "Demande de congé créée avec succès"
                    )
            );
        } catch (Exception e) {
            log.error("!!! ERREUR lors de la création de la demande !!!", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new CreateResponse(
                            false,
                            "Erreur lors de la création de la demande: " + e.getMessage()
                    )
            );
        }
    }

    // ==================== EMPLOYEE ENDPOINTS ====================

    /**
     * Récupère les détails d'un congé par son UUID (EMPLOYEE uniquement).
     * L'employé ne peut accéder qu'à ses propres congés.
     */
    @PreAuthorize("hasAuthority('EMPLOYEE')")
    @GetMapping("/employee/{id}")
    public ResponseEntity<CongeResponse> employeeGetCongeById(@PathVariable UUID id) {
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

    /**
     * Récupère la liste des congés de l'employé connecté.
     */
    @PreAuthorize("hasAuthority('EMPLOYEE')")
    @GetMapping("/employee/my-list")
    public ResponseEntity<ListCongeResponse> employeeGetMyListOfConge() {
        List<CongeDto> congeDtoList = congeService.getMyListOfConge();

        return ResponseEntity.status(HttpStatus.OK).body(
                new ListCongeResponse(
                        congeDtoList != null,
                        congeDtoList
                )
        );
    }


    /**
     * Crée une demande de congé sans fichiers (EMPLOYEE).
     * Accepte application/json.
     */
    @PreAuthorize("hasAuthority('EMPLOYEE')")
    @PostMapping(value = "/employee/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CongeResponse> employeeCreateCongeJson(@RequestBody CongeDto congeDto) {
        CongeDto conge = congeService.createConge(congeDto, null);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new CongeResponse(
                        conge != null,
                        conge
                )
        );
    }


    /**
     * Met à jour un congé sans fichiers (EMPLOYEE).
     * L'employé ne peut modifier que ses propres congés et seulement s'ils sont ENATTENTE.
     * Accepte application/json.
     */
    @PreAuthorize("hasAnyAuthority('RH', 'EMPLOYEE', 'ADMIN')")
    @PutMapping(value = "/employee/{id}/edit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CongeResponse> employeeEditCongeByIdJson(
            @PathVariable UUID id,
            @RequestBody CongeDto congeDto) {

        CongeDto conge = congeService.editConge(id, congeDto, null);

        if (conge == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new CongeResponse(false, null));
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                new CongeResponse(
                        true,
                        conge
                )
        );
    }

    /**
     * Met à jour un congé avec ou sans fichiers (EMPLOYEE).
     * L'employé ne peut modifier que ses propres congés et seulement s'ils sont ENATTENTE.
     * Accepte multipart/form-data.
     */
    @PreAuthorize("hasAnyAuthority('RH', 'EMPLOYEE', 'ADMIN')")
    @PutMapping(value = "/employee/{id}/edit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CongeResponse> employeeEditCongeByIdMultipart(
            @PathVariable UUID id,
            @RequestPart("typeConge") String typeConge,
            @RequestPart("dateDebut") String dateDebut,
            @RequestPart("dateFin") String dateFin,
            @RequestPart("nombreJours") String nombreJours,
            @RequestPart("motif") String motif,
            @RequestPart(value = "remplacant", required = false) String remplacant,
            @RequestPart(value = "justificatifs", required = false) List<MultipartFile> justificatifs) {
        log.info("[EDIT_CONGE] Reçu requête multipart pour id={} | typeConge={} | dateDebut={} | dateFin={} | nombreJours={} | motif={} | remplacant={} | justificatifs null? {} | justificatifs.size={}",
                id, typeConge, dateDebut, dateFin, nombreJours, motif, remplacant, (justificatifs == null), (justificatifs != null ? justificatifs.size() : 0));
        try {
            // Reconstruire le CongeDto à partir des champs reçus
            var congeDto = CongeDto.builder()
                    .typeConge(TypeCongeDto.builder().libelle(Libelle.valueOf(typeConge)).build())
                    .dateDebut(LocalDate.parse(dateDebut))
                    .dateFin(LocalDate.parse(dateFin))
                    .commentaireEmployee(motif)
                    .build();
            // Appeler le service
            CongeDto conge = congeService.editConge(id, congeDto, justificatifs);
            if (conge == null) {
                log.warn("[EDIT_CONGE] 403/404 - Echec modification congé id={}", id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new CongeResponse(false, null));
            }
            log.info("[EDIT_CONGE] Succès modification congé id={}", id);
            return ResponseEntity.status(HttpStatus.OK).body(
                    new CongeResponse(
                            true,
                            conge
                    )
            );
        } catch (Exception e) {
            log.error("[EDIT_CONGE] 400 - Exception lors de la modification du congé id={}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new CongeResponse(false, null));
        }
    }

    /**
     * Supprime un congé (EMPLOYEE).
     * L'employé ne peut supprimer que ses propres congés et seulement s'ils sont ENATTENTE.
     */
    @PreAuthorize("hasAnyAuthority('RH', 'EMPLOYEE', 'ADMIN')")
    @DeleteMapping("/employee/{id}/delete")
    public ResponseEntity<CongeResponse> employeeDeleteCongeById(@PathVariable UUID id) {
        boolean deleted = congeService.deleteConge(id);

        if (!deleted) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new CongeResponse(false, null));
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                new CongeResponse(
                        true,
                        null
                )
        );
    }

    /**
     * Ajoute des justificatifs à un congé existant (EMPLOYEE).
     * L'employé ne peut ajouter des justificatifs qu'à ses propres congés en attente.
     */
    @PreAuthorize("hasAuthority('EMPLOYEE')")
    @PutMapping(value = "/employee/{id}/justificatifs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CongeResponse> employeeAddJustificatifsToConge(
            @PathVariable UUID id,
            @RequestPart("justificatifs") List<MultipartFile> justificatifs) {
        try {
            log.info("=== Ajout de justificatifs au congé {} ===", id);
            log.info("Nombre de fichiers reçus: {}", justificatifs != null ? justificatifs.size() : "NULL");

            if (justificatifs != null && !justificatifs.isEmpty()) {
                for (MultipartFile file : justificatifs) {
                    log.info(">>> Fichier reçu: {}, Taille: {} bytes, isEmpty: {}",
                            file.getOriginalFilename(),
                            file.getSize(),
                            file.isEmpty());
                }
            }

            CongeDto conge = congeService.addJustificatifsToConge(id, justificatifs);

            if (conge == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new CongeResponse(false, null));
            }

            log.info(">>> Justificatifs ajoutés avec succès au congé {}", id);
            return ResponseEntity.status(HttpStatus.OK).body(
                    new CongeResponse(
                            true,
                            conge
                    )
            );
        } catch (Exception e) {
            log.error("!!! ERREUR lors de l'ajout de justificatifs au congé {} !!!", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new CongeResponse(
                            false,
                            null
                    )
            );
        }
    }

    /**
     * Télécharge un justificatif par son UUID (accessible à l'employé, RH, ou admin).
     */
    @PreAuthorize("hasAnyAuthority('EMPLOYEE', 'RH', 'ADMIN')")
    @GetMapping("/justificatifs/{uuid}/download")
    public ResponseEntity<?> downloadJustificatif(@PathVariable String uuid) {
        try {
            //transformer le string uuid en UUID
            UUID uuidObj = UUID.fromString(uuid);
            // Récupérer le fichier et ses infos via le service
            var justificatif = justificatifService.getJustificatifByUuid(uuidObj);
            if (justificatif == null || justificatif.getNomFichier() == null) {
                return ResponseEntity.notFound().build();
            }
            var resource = fileStorageService.loadJustificatifAsResource(justificatif.getNomFichier());
            if (resource == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + justificatif.getNomFichier() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors du téléchargement du justificatif.");
        }
    }

    // ==================== RH ENDPOINTS ====================

    /**
     * Récupère la liste de toutes les demandes de congés (RH uniquement).
     */
    @PreAuthorize("hasAuthority('RH')")
    @GetMapping("/rh")
    public ResponseEntity<ListCongeResponse> rhGetAllListOfConge() {
        List<CongeDto> congeDtoList = congeService.getAllConge();

        return ResponseEntity.status(HttpStatus.OK).body(
                new ListCongeResponse(
                        congeDtoList != null,
                        congeDtoList
                )
        );
    }

    /**
     * Récupère les détails d'un congé par son UUID (RH uniquement).
     * Le RH peut accéder à tous les congés.
     */
    @PreAuthorize("hasAuthority('RH')")
    @GetMapping("/rh/{id}")
    public ResponseEntity<CongeResponse> rhGetCongeById(@PathVariable UUID id) {
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

    /**
     * Accepte un congé (RH uniquement).
     */
    @PreAuthorize("hasAuthority('RH')")
    @PutMapping("/rh/{id}/accept")
    public ResponseEntity<CongeResponse> rhAcceptCongeById(@PathVariable UUID id) {
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

    /**
     * Refuse un congé (RH uniquement).
     */
    @PreAuthorize("hasAuthority('RH')")
    @PutMapping("/rh/{id}/refuse")
    public ResponseEntity<CongeResponse> rhRefuseCongeById(@PathVariable UUID id) {
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


    /**
     * Met à jour un congé sans fichiers (RH).
     * Le RH peut modifier tous les congés.
     * Accepte application/json.
     */
    @PreAuthorize("hasAuthority('RH')")
    @PutMapping(value = "/rh/{id}/edit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CongeResponse> rhEditCongeByIdJson(
            @PathVariable UUID id,
            @RequestBody CongeDto congeDto) {

        CongeDto conge = congeService.editConge(id, congeDto, null);

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

    /**
     * Supprime un congé (RH).
     * Le RH peut supprimer tous les congés.
     */
    @PreAuthorize("hasAuthority('RH')")
    @DeleteMapping("/rh/{id}/delete")
    public ResponseEntity<CongeResponse> rhDeleteCongeById(@PathVariable UUID id) {
        boolean deleted = congeService.deleteConge(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                new CongeResponse(
                        true,
                        null
                )
        );
    }

    /**
     * Ajoute ou modifie une annotation RH sur un congé (RH uniquement).
     */
    @PreAuthorize("hasAuthority('RH')")
    @PutMapping("/rh/{id}/annotation")
    public ResponseEntity<CongeResponse> rhAddAnnotation(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request) {

        String annotationRH = request.get("annotationRH");
        if (annotationRH == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new CongeResponse(false, null));
        }

        CongeDto conge = congeService.addAnnotationRH(id, annotationRH);

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


    /**
     * Récupère la liste de toutes les demandes de congés (ADMIN uniquement).
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<ListCongeResponse> adminGetAllListOfConge() {
        List<CongeDto> congeDtoList = congeService.getAllConge();

        return ResponseEntity.status(HttpStatus.OK).body(
                new ListCongeResponse(
                        congeDtoList != null,
                        congeDtoList
                )
        );
    }

    /**
     * Récupère les détails d'un congé par son UUID (ADMIN uniquement).
     * L'admin peut accéder à tous les congés.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin/{id}")
    public ResponseEntity<CongeResponse> adminGetCongeById(@PathVariable UUID id) {
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

    /**
     * Accepte un congé (ADMIN uniquement).
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/admin/{id}/accept")
    public ResponseEntity<CongeResponse> adminAcceptCongeById(@PathVariable UUID id) {
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

    /**
     * Refuse un congé (ADMIN uniquement).
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/admin/{id}/refuse")
    public ResponseEntity<CongeResponse> adminRefuseCongeById(@PathVariable UUID id) {
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

    /**
     * Met à jour un congé sans fichiers (ADMIN).
     * L'admin peut modifier tous les congés.
     * Accepte application/json.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping(value = "/admin/{id}/edit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CongeResponse> adminEditCongeByIdJson(
            @PathVariable UUID id,
            @RequestBody CongeDto congeDto) {

        CongeDto conge = congeService.editConge(id, congeDto, null);

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

    /**
     * Supprime un congé (ADMIN).
     * L'admin peut supprimer tous les congés.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/admin/{id}/delete")
    public ResponseEntity<CongeResponse> adminDeleteCongeById(@PathVariable UUID id) {
        boolean deleted = congeService.deleteConge(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                new CongeResponse(
                        true,
                        null
                )
        );
    }

    /**
     * Ajoute ou modifie une annotation RH sur un congé (ADMIN uniquement).
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/admin/{id}/annotation")
    public ResponseEntity<CongeResponse> adminAddAnnotation(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request) {

        String annotationRH = request.get("annotationRH");
        if (annotationRH == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new CongeResponse(false, null));
        }

        CongeDto conge = congeService.addAnnotationRH(id, annotationRH);

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

    /**
     * Ajoute ou modifie un commentaire RH sur un congé (RH uniquement).
     *
     * @param id L'UUID du congé
     * @param request Map contenant la clé "commentaireRH"
     * @return CongeResponse avec le congé mis à jour ou erreur
     */
    @PreAuthorize("hasAuthority('EMPLOYEE')")
    @PutMapping("/rh/{id}/commentaire")
    public ResponseEntity<CongeResponse> rhAddCommentaire(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request) {
        String commentaireRH = request.get("commentaireRH");
        if (commentaireRH == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new CongeResponse(false, null));
        }
        CongeDto conge = congeService.addCommentaireRH(id, commentaireRH);
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
}