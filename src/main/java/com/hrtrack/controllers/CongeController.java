package com.hrtrack.controllers;

import com.hrtrack.domain.CongeStatus;
import com.hrtrack.domain.dtos.CongeDto;
import com.hrtrack.domain.responses.conge.CongeResponse;
import com.hrtrack.domain.responses.conge.ListCongeResponse;
import com.hrtrack.service.CongeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Contrôleur pour la gestion des demandes de congés.
 * Organisation :
 * - Section RH : Gestion complète des demandes (voir, accepter, refuser, modifier, supprimer, annoter)
 * - Section ADMIN : Consultation des demandes et statistiques
 * - Section EMPLOYEE : Gestion de ses propres demandes
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/conges")
public class CongeController {

    private final CongeService congeService;

    // ==================== EMPLOYEE ENDPOINTS ====================

    /**
     * Récupère la liste des congés de l'employé connecté.
     * Endpoint: GET /api/v1/conges/employee/my-list
     */
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
     * Récupère les détails d'un congé par son UUID (EMPLOYEE uniquement).
     * L'employé ne peut accéder qu'à ses propres congés.
     * Endpoint: GET /api/v1/conges/employee/{id}
     */
    @GetMapping("/employee/{id}")
    public ResponseEntity<CongeResponse> employeeGetCongeById(@PathVariable UUID id) {
        CongeDto conge = congeService.getCongeById(id);

        if (conge == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                new CongeResponse(true, conge)
        );
    }

    /**
     * Crée une demande de congé sans fichiers (EMPLOYEE).
     * Accepte application/json.
     * Endpoint: POST /api/v1/conges/employee/create
     */
    @PostMapping(value = "/employee/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CongeResponse> employeeCreateCongeJson(@RequestBody CongeDto congeDto) {
        CongeDto conge = congeService.createConge(congeDto, null);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new CongeResponse(conge != null, conge)
        );
    }

    /**
     * Crée une demande de congé avec fichiers justificatifs (EMPLOYEE).
     * Accepte multipart/form-data.
     * Endpoint: POST /api/v1/conges/employee/create
     */
    @PostMapping(value = "/employee/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CongeResponse> employeeCreateCongeWithFiles(
            @RequestPart("conge") CongeDto congeDto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        CongeDto conge = congeService.createConge(congeDto, files);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new CongeResponse(conge != null, conge)
        );
    }

    /**
     * Met à jour un congé sans fichiers (EMPLOYEE).
     * L'employé ne peut modifier que ses propres congés et seulement s'ils sont ENATTENTE.
     * Accepte application/json.
     * Endpoint: PUT /api/v1/conges/employee/{id}/edit
     */
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
                new CongeResponse(true, conge)
        );
    }

    /**
     * Met à jour un congé avec fichiers justificatifs (EMPLOYEE).
     * Accepte multipart/form-data.
     * Endpoint: PUT /api/v1/conges/employee/{id}/edit
     */
    @PutMapping(value = "/employee/{id}/edit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CongeResponse> employeeEditCongeByIdWithFiles(
            @PathVariable UUID id,
            @RequestPart("conge") CongeDto congeDto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        
        CongeDto conge = congeService.editConge(id, congeDto, files);

        if (conge == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new CongeResponse(false, null));
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                new CongeResponse(true, conge)
        );
    }

    /**
     * Supprime un congé (EMPLOYEE).
     * L'employé ne peut supprimer que ses propres congés et seulement s'ils sont ENATTENTE.
     * Endpoint: DELETE /api/v1/conges/employee/{id}/delete
     */
    @DeleteMapping("/employee/{id}/delete")
    public ResponseEntity<CongeResponse> employeeDeleteCongeById(@PathVariable UUID id) {
        boolean deleted = congeService.deleteConge(id);

        if (!deleted) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new CongeResponse(false, null));
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                new CongeResponse(true, null)
        );
    }

    // ==================== RH ENDPOINTS ====================

    /**
     * Récupère la liste de toutes les demandes de congés (RH uniquement).
     * Endpoint: GET /api/v1/conges/rh/list
     */
    @GetMapping("/rh/list")
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
     * Endpoint: GET /api/v1/conges/rh/{id}
     */
    @GetMapping("/rh/{id}")
    public ResponseEntity<CongeResponse> rhGetCongeById(@PathVariable UUID id) {
        CongeDto conge = congeService.getCongeById(id);

        if (conge == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                new CongeResponse(true, conge)
        );
    }

    /**
     * Accepte un congé (RH uniquement).
     * Endpoint: PUT /api/v1/conges/rh/{id}/accept
     */
    @PutMapping("/rh/{id}/accept")
    public ResponseEntity<CongeResponse> rhAcceptCongeById(@PathVariable UUID id) {
        CongeDto conge = congeService.changeCongeStatus(id, CongeStatus.ACCEPTER);

        if (conge == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                new CongeResponse(true, conge)
        );
    }

    /**
     * Refuse un congé (RH uniquement).
     * Endpoint: PUT /api/v1/conges/rh/{id}/refuse
     */
    @PutMapping("/rh/{id}/refuse")
    public ResponseEntity<CongeResponse> rhRefuseCongeById(@PathVariable UUID id) {
        CongeDto conge = congeService.changeCongeStatus(id, CongeStatus.REFUSER);

        if (conge == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                new CongeResponse(true, conge)
        );
    }

    /**
     * Met à jour un congé sans fichiers (RH).
     * Le RH peut modifier tous les congés.
     * Accepte application/json.
     * Endpoint: PUT /api/v1/conges/rh/{id}/edit
     */
    @PutMapping(value = "/rh/{id}/edit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CongeResponse> rhEditCongeByIdJson(
            @PathVariable UUID id,
            @RequestBody CongeDto congeDto) {
        
        CongeDto conge = congeService.editConge(id, congeDto, null);

        if (conge == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                new CongeResponse(true, conge)
        );
    }

    /**
     * Met à jour un congé avec fichiers justificatifs (RH).
     * Accepte multipart/form-data.
     * Endpoint: PUT /api/v1/conges/rh/{id}/edit
     */
    @PutMapping(value = "/rh/{id}/edit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CongeResponse> rhEditCongeByIdWithFiles(
            @PathVariable UUID id,
            @RequestPart("conge") CongeDto congeDto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        
        CongeDto conge = congeService.editConge(id, congeDto, files);

        if (conge == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                new CongeResponse(true, conge)
        );
    }

    /**
     * Supprime un congé (RH).
     * Le RH peut supprimer tous les congés.
     * Endpoint: DELETE /api/v1/conges/rh/{id}/delete
     */
    @DeleteMapping("/rh/{id}/delete")
    public ResponseEntity<CongeResponse> rhDeleteCongeById(@PathVariable UUID id) {
        boolean deleted = congeService.deleteConge(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                new CongeResponse(true, null)
        );
    }

    /**
     * Ajoute ou modifie une annotation RH sur un congé (RH uniquement).
     * Endpoint: PUT /api/v1/conges/rh/{id}/annotation
     */
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
                new CongeResponse(true, conge)
        );
    }

    // ==================== ADMIN ENDPOINTS ====================

    /**
     * Récupère la liste de toutes les demandes de congés (ADMIN uniquement).
     * L'admin peut voir toutes les demandes pour consultation.
     * Endpoint: GET /api/v1/conges/admin/list
     */
    @GetMapping("/admin/list")
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
     * L'admin peut accéder à tous les congés pour consultation.
     * Endpoint: GET /api/v1/conges/admin/{id}
     */
    @GetMapping("/admin/{id}")
    public ResponseEntity<CongeResponse> adminGetCongeById(@PathVariable UUID id) {
        CongeDto conge = congeService.getCongeById(id);

        if (conge == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                new CongeResponse(true, conge)
        );
    }
}
