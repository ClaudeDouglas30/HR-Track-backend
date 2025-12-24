package com.hrtrack.controllers;

import com.hrtrack.domain.requests.typeConge.CreateTypeCongeRequest;
import com.hrtrack.domain.requests.typeConge.UpdateTypeCongeRequest;
import com.hrtrack.domain.responses.typeConge.ListTypeCongeResponse;
import com.hrtrack.domain.responses.typeConge.TypeCongeResponse;
import com.hrtrack.service.TypeCongeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur pour la gestion des types de congés.
 * 
 * Endpoints disponibles :
 * - GET /api/v1/leave-types : Liste tous les types de congés
 * - GET /api/v1/leave-types/{id} : Récupère un type de congé par son ID
 * - POST /api/v1/leave-types : Crée un nouveau type de congé
 * - PUT /api/v1/leave-types/{id} : Met à jour un type de congé
 * - DELETE /api/v1/leave-types/{id} : Supprime un type de congé
 */
@RestController
@RequestMapping("/api/v1/leave-types")
@RequiredArgsConstructor
public class TypeCongeController {

    private final TypeCongeService typeCongeService;

    /**
     * Récupère la liste de tous les types de congés.
     * Accessible par ADMIN et RH.
     */
    @GetMapping("/list")
    public ResponseEntity<ListTypeCongeResponse> getAllTypeConges() {
        var typeConges = typeCongeService.getAllTypeConges();

        return ResponseEntity.status(HttpStatus.OK).body(
                ListTypeCongeResponse.builder()
                        .success(true)
                        .message("Liste des types de congés récupérée avec succès")
                        .typeConges(typeConges)
                        .total(typeConges.size())
                        .build()
        );
    }

    /**
     * Récupère un type de congé par son ID.
     * Accessible par ADMIN et RH.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TypeCongeResponse> getTypeCongeById(@PathVariable Integer id) {
        var typeConge = typeCongeService.getTypeCongeById(id);

        if (typeConge == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    TypeCongeResponse.builder()
                            .success(false)
                            .message("Type de congé non trouvé avec l'ID: " + id)
                            .typeConge(null)
                            .build()
            );
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                TypeCongeResponse.builder()
                        .success(true)
                        .message("Type de congé récupéré avec succès")
                        .typeConge(typeConge)
                        .build()
        );
    }

    /**
     * Crée un nouveau type de congé.
     * Accessible uniquement par ADMIN.
     */
    @PostMapping("/create")
    public ResponseEntity<TypeCongeResponse> createTypeConge(@RequestBody CreateTypeCongeRequest request) {
        try {
            var typeConge = typeCongeService.createTypeConge(
                    request.libelle(),
                    request.quotaAnnuel(),
                    request.description()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    TypeCongeResponse.builder()
                            .success(true)
                            .message("Type de congé créé avec succès")
                            .typeConge(typeConge)
                            .build()
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    TypeCongeResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .typeConge(null)
                            .build()
            );
        }
    }

    /**
     * Met à jour un type de congé.
     * Accessible uniquement par ADMIN.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TypeCongeResponse> updateTypeConge(
            @PathVariable Integer id,
            @RequestBody UpdateTypeCongeRequest request
    ) {
        try {
            var typeConge = typeCongeService.updateTypeConge(
                    id,
                    request.libelle(),
                    request.quotaAnnuel(),
                    request.description()
            );

            if (typeConge == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        TypeCongeResponse.builder()
                                .success(false)
                                .message("Type de congé non trouvé avec l'ID: " + id)
                                .typeConge(null)
                                .build()
                );
            }

            return ResponseEntity.status(HttpStatus.OK).body(
                    TypeCongeResponse.builder()
                            .success(true)
                            .message("Type de congé mis à jour avec succès")
                            .typeConge(typeConge)
                            .build()
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    TypeCongeResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .typeConge(null)
                            .build()
            );
        }
    }

    /**
     * Supprime un type de congé.
     * Accessible uniquement par ADMIN.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<TypeCongeResponse> deleteTypeConge(@PathVariable Integer id) {
        boolean deleted = typeCongeService.deleteTypeConge(id);

        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    TypeCongeResponse.builder()
                            .success(false)
                            .message("Type de congé non trouvé avec l'ID: " + id)
                            .typeConge(null)
                            .build()
            );
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                TypeCongeResponse.builder()
                        .success(true)
                        .message("Type de congé supprimé avec succès")
                        .typeConge(null)
                        .build()
        );
    }
}

