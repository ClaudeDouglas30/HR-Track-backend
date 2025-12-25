package com.hrtrack.controllers;

import com.hrtrack.domain.CongeStatus;
import com.hrtrack.domain.dtos.CongeDto;
import com.hrtrack.domain.responses.conge.CongeResponse;
import com.hrtrack.domain.responses.conge.ListCongeResponse;
import com.hrtrack.domain.responses.conge.PaginatedLeaveRequestResponse;
import com.hrtrack.service.CongeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * Crée une demande de congé (EMPLOYEE).
     * Endpoint: POST /api/v1/conges/employee/create
     */
    @PostMapping(value = "/employee/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CongeResponse> employeeCreateConge(@RequestBody CongeDto congeDto) {
        CongeDto conge = congeService.createConge(congeDto, null);

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
     * Récupère la liste paginée des demandes de congés avec filtrage (RH uniquement).
     * Endpoint: GET /api/v1/conges/rh/list
     * 
     * @param status Filtre par statut (all, ENATTENTE, ACCEPTER, REFUSER)
     * @param search Terme de recherche (nom, prénom)
     * @param page Numéro de page (par défaut 0)
     * @param size Taille de la page (par défaut 10)
     */
    @GetMapping("/rh/list")
    public ResponseEntity<PaginatedLeaveRequestResponse> rhGetAllListOfConge(
            @RequestParam(required = false, defaultValue = "all") String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        
        PaginatedLeaveRequestResponse response = congeService.getLeaveRequestsPaginated(status, search, page, size);
        
        return ResponseEntity.status(HttpStatus.OK).body(response);
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
     * Refuse un congé avec motif (RH uniquement).
     * Endpoint: PUT /api/v1/conges/rh/{id}/refuse
     */
    @PutMapping("/rh/{id}/refuse")
    public ResponseEntity<CongeResponse> rhRefuseCongeById(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request) {
        
        String motifRefus = request.get("motifRefus");
        if (motifRefus == null || motifRefus.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new CongeResponse(false, null));
        }
        
        CongeDto conge = congeService.rejectCongeWithMotif(id, motifRefus);

        if (conge == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                new CongeResponse(true, conge)
        );
    }

    /**
     * Met à jour un congé (RH).
     * Le RH peut modifier tous les congés.
     * Endpoint: PUT /api/v1/conges/rh/{id}/edit
     */
    @PutMapping(value = "/rh/{id}/edit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CongeResponse> rhEditCongeById(
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
     * Récupère la liste paginée des demandes de congés avec filtrage (ADMIN uniquement).
     * L'admin peut voir toutes les demandes pour consultation.
     * Endpoint: GET /api/v1/conges/admin/list
     * 
     * @param status Filtre par statut (all, ENATTENTE, ACCEPTER, REFUSER)
     * @param search Terme de recherche (nom, prénom)
     * @param page Numéro de page (par défaut 0)
     * @param size Taille de la page (par défaut 10)
     */
    @GetMapping("/admin/list")
    public ResponseEntity<PaginatedLeaveRequestResponse> adminGetAllListOfConge(
            @RequestParam(required = false, defaultValue = "all") String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        
        PaginatedLeaveRequestResponse response = congeService.getLeaveRequestsPaginated(status, search, page, size);
        
        return ResponseEntity.status(HttpStatus.OK).body(response);
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
