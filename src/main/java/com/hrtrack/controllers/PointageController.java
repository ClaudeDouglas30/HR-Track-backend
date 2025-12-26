package com.hrtrack.controllers;


import com.hrtrack.domain.entities.User;

import com.hrtrack.domain.requests.pointage.PointageRequest;
import com.hrtrack.domain.responses.pointage.PointResp;
import com.hrtrack.domain.responses.pointage.PointageResponse;
import com.hrtrack.service.IPointageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pointages")
@RequiredArgsConstructor
public class PointageController {

    private final IPointageService pointageService;

    /**
     * Endpoint pour pointer
     * Accessible à tous les utilisateurs connectés
     */
    @PostMapping("/pointer")
    public ResponseEntity<PointResp> pointer(
            @RequestBody PointageRequest request,
            Authentication authentication // récupère l'utilisateur connecté
    ) throws Exception {
        User user = (User) authentication.getPrincipal();
        try {
            PointageResponse response = pointageService.pointer(request, user.getId());
            return ResponseEntity.ok(
                    new PointResp(
                            true,
                            "pointage effectuer avec sucess",
                            response
                    )
            );
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new PointResp(
                            false,
                            "Erreur lors du pointage: " + e.getMessage(),
                            null
                    )
            );
        }
    }

    /**
     * Endpoint pour lister les pointages
     * - EMPLOYEE : voit uniquement ses propres pointages
     * - RH : voit tous les pointages
     */
    @GetMapping
    public ResponseEntity<List<PointageResponse>> getPointages(
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        List<PointageResponse> pointages = pointageService.getPointages(user);
        return ResponseEntity.ok(pointages);
    }
}