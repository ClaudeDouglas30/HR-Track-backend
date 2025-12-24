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
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/conges")
public class CongeController {

    private final CongeService congeService;
    private final JustificatifService justificatifService;
    private final FileStorageService fileStorageService;

    // ==================== EMPLOYEE ENDPOINTS ====================

    /**
     * Récupère les détails d'un congé par son UUID (EMPLOYEE uniquement).
     * L'employé ne peut accéder qu'à ses propres congés.
     */
    @PreAuthorize("hasRole('EMPLOYEE')")
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
    @PreAuthorize("hasRole('EMPLOYEE')")
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
    @PreAuthorize("hasRole('EMPLOYEE')")
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
    @PreAuthorize("hasRole('EMPLOYEE')")
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
     * Supprime un congé (EMPLOYEE).
     * L'employé ne peut supprimer que ses propres congés et seulement s'ils sont ENATTENTE.
     */
    @PreAuthorize("hasRole('EMPLOYEE')")
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
     * Récupère la liste de toutes les demandes de congés (RH uniquement).
     */
    @PreAuthorize("hasRole('RH')")
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
    @PreAuthorize("hasRole('RH')")
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
    @PreAuthorize("hasRole('RH')")
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
    @PreAuthorize("hasRole('RH')")
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
    @PreAuthorize("hasRole('RH')")
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
    @PreAuthorize("hasRole('RH')")
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
    @PreAuthorize("hasRole('RH')")
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
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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
}
