package com.hrtrack.controllers;

import com.hrtrack.domain.dtos.UserDto;
import com.hrtrack.domain.requests.user.CreateUserRequest;
import com.hrtrack.domain.responses.user.ListUserResponse;
import com.hrtrack.domain.responses.user.StatisticsResponse;
import com.hrtrack.domain.responses.user.UserDetailResponse;
import com.hrtrack.domain.responses.user.UserResponse;
import com.hrtrack.service.UserService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Contrôleur pour la gestion des utilisateurs.
 * 
 * Règles de permissions :
 * - RH : peut créer des utilisateurs (non validés) et voir la liste des utilisateurs
 * - ADMIN : peut créer, modifier, supprimer, valider des utilisateurs et voir les statistiques
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    // ==================== RH ENDPOINTS ====================

    /**
     * Récupère la liste de tous les utilisateurs (RH et ADMIN).
     */
    @GetMapping("/list")
    public ResponseEntity<ListUserResponse> getAllUsers() {
        var users = userService.getAllUsers();

        return ResponseEntity.status(HttpStatus.OK).body(
                ListUserResponse.builder()
                        .success(true)
                        .message("Liste des utilisateurs récupérée avec succès")
                        .users(users)
                        .build()
        );
    }

    /**
     * Récupère les détails complets d'un utilisateur par son ID (RH et ADMIN).
     * Retourne toutes les informations nécessaires pour la page de détails :
     * - Informations de base de l'utilisateur
     * - Statistiques mensuelles (présences, absences, retards, heures travaillées, ponctualité)
     * - Pointage de la semaine
     * - Évolution de la ponctualité sur 6 mois
     * - Demandes de congés récentes
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDetailResponse> getUserById(@PathVariable Long id) {
        try {
            com.hrtrack.domain.dtos.UserDetailDto userDetail = userService.getUserDetailById(id);

            return ResponseEntity.status(HttpStatus.OK).body(
                    UserDetailResponse.builder()
                            .success(true)
                            .message("Détails de l'utilisateur récupérés avec succès")
                            .user(userDetail)
                            .build()
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    UserDetailResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .user(null)
                            .build()
            );
        }
    }

    /**
     * Valide le compte d'un utilisateur (ADMIN uniquement).
     * Envoie un email de confirmation à l'utilisateur.
     */
    @PutMapping("/{id}/validate")
    public ResponseEntity<UserResponse> adminValidateUserAccount(@PathVariable Long id) {
        try {
            UserDto user = userService.validateUserAccount(id);

            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.status(HttpStatus.OK).body(
                    UserResponse.builder()
                            .success(true)
                            .message("Compte utilisateur validé avec succès. Un email de confirmation a été envoyé.")
                            .user(user)
                            .build()
            );
        } catch (MessagingException | com.hrtrack.exception.EmailingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    UserResponse.builder()
                            .success(false)
                            .message("Erreur lors de l'envoi de l'email de validation : " + e.getMessage())
                            .user(null)
                            .build()
            );
        }
    }

    /**
     * Met à jour les informations d'un utilisateur (ADMIN uniquement).
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<UserResponse> adminUpdateUser(
            @PathVariable Long id,
            @RequestBody UserDto userDto) {
        try {
            UserDto user = userService.updateUser(id, userDto);

            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.status(HttpStatus.OK).body(
                    UserResponse.builder()
                            .success(true)
                            .message("Utilisateur mis à jour avec succès")
                            .user(user)
                            .build()
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    UserResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .user(null)
                            .build()
            );
        }
    }

    /**
     * Crée un nouvel utilisateur (RH uniquement).
     * Le compte créé ne sera pas validé (seul l'admin peut valider).
     */
    @PostMapping("/create")
    public ResponseEntity<UserResponse> rhCreateUser(@RequestBody CreateUserRequest request) {
        try {
            UserDto user = userService.createUser(
                    request.nom(),
                    request.prenom(),
                    request.email(),
                    request.telephone(),
                    request.password(),
                    request.role(),
                    request.photoProfil()
            );

            if (user == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        UserResponse.builder()
                                .success(false)
                                .message("Erreur lors de la création de l'utilisateur")
                                .user(null)
                                .build()
                );
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    UserResponse.builder()
                            .success(true)
                            .message("Utilisateur créé avec succès. Le compte est en attente de validation par l'administrateur.")
                            .user(user)
                            .build()
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    UserResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .user(null)
                            .build()
            );
        }
    }

    // ==================== ADMIN ENDPOINTS ====================

    /**
     * Crée un nouvel utilisateur (ADMIN uniquement).
     * L'admin peut créer un utilisateur directement validé si nécessaire.
     */
    @PostMapping("/admin/create")
    public ResponseEntity<UserResponse> adminCreateUser(@RequestBody CreateUserRequest request) {
        try {
            UserDto user = userService.createUser(
                    request.nom(),
                    request.prenom(),
                    request.email(),
                    request.telephone(),
                    request.password(),
                    request.role(),
                    request.photoProfil()
            );

            if (user == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        UserResponse.builder()
                                .success(false)
                                .message("Erreur lors de la création de l'utilisateur")
                                .user(null)
                                .build()
                );
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    UserResponse.builder()
                            .success(true)
                            .message("Utilisateur créé avec succès")
                            .user(user)
                            .build()
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    UserResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .user(null)
                            .build()
            );
        }
    }

    /**
     * Supprime un utilisateur (ADMIN uniquement).
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<UserResponse> adminDeleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                UserResponse.builder()
                        .success(true)
                        .message("Utilisateur supprimé avec succès")
                        .user(null)
                        .build()
        );
    }

    /**
     * Récupère les statistiques des utilisateurs (ADMIN uniquement).
     */
    @GetMapping("/admin/statistics")
    public ResponseEntity<StatisticsResponse> adminGetStatistics() {
        Map<String, Object> statistics = userService.getStatistics();

        return ResponseEntity.status(HttpStatus.OK).body(
                StatisticsResponse.builder()
                        .success(true)
                        .message("Statistiques récupérées avec succès")
                        .statistics(statistics)
                        .build()
        );
    }
}
