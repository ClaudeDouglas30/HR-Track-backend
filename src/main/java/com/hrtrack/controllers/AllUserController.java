package com.hrtrack.controllers;


import com.hrtrack.domain.dtos.UserDto;
import com.hrtrack.domain.entities.User;
import com.hrtrack.domain.responses.user.ProfileResponse;
import com.hrtrack.domain.responses.user.UsersListResponse;
import com.hrtrack.mappers.UserMapper;
import com.hrtrack.service.Userser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class AllUserController {

    private final Userser userser;
    private final UserMapper userMapper;

    /**
     * Récupère le profil de l'utilisateur connecté
     * GET /api/v1/users/me
     */
    @GetMapping("/me")
    public HttpEntity<ProfileResponse> getCurrentUser() {
        try {
            User u = userser.getCurrentUser();
            UserDto userDto = userMapper.toDto(u);
            return ResponseEntity.ok(
                    new ProfileResponse(
                            true,
                            "Profil utilisateur récupéré avec succès",
                            userDto
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ProfileResponse(
                            false,
                            "Échec de la récupération du profil utilisateur: " + e.getMessage(),
                            null
                    )
            );
        }
    }

    /**
     * Met à jour le profil complet avec photo (multipart)
     * PUT /api/v1/users/me/update
     */
    @PutMapping(path = "/me/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public HttpEntity<ProfileResponse> updateUserProfileWithPhoto(
            @RequestParam("id") String id,
            @RequestParam(value = "nom", required = false) String nom,
            @RequestParam(value = "prenom", required = false) String prenom,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "telephone", required = false) String telephone,
            @RequestPart(value = "photo", required = false) MultipartFile photo
    ) {
        try {
            User u = userser.updateUserProfile(Integer.parseInt(id), nom, prenom, email, telephone, photo);
            UserDto userDto = userMapper.toDto(u);
            return ResponseEntity.ok(
                    new ProfileResponse(
                            true,
                            "Profil utilisateur mis à jour avec succès",
                            userDto
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ProfileResponse(
                            false,
                            "Échec de la mise à jour du profil: " + e.getMessage(),
                            null
                    )
            );
        }
    }

    /**
     * Met à jour uniquement la photo de profil
     * PUT /api/v1/users/me/photo
     */
    @PutMapping(path = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public HttpEntity<ProfileResponse> updateUserPhoto(
            @RequestPart("photo") MultipartFile photo
    ) {
        try {
            User currentUser = userser.getCurrentUser();
            User u = userser.updateUserProfile(
                    currentUser.getId().intValue(),
                    null,
                    null,
                    null,
                    null,
                    photo
            );
            UserDto userDto = userMapper.toDto(u);
            return ResponseEntity.ok(
                    new ProfileResponse(
                            true,
                            "Photo de profil mise à jour avec succès",
                            userDto
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ProfileResponse(
                            false,
                            "Échec de la mise à jour de la photo: " + e.getMessage(),
                            null
                    )
            );
        }
    }

    /**
     * Change le mot de passe de l'utilisateur
     * PUT /api/v1/users/me/password
     */
    @PutMapping("/me/password")
    public HttpEntity<ProfileResponse> changePassword(
            @RequestBody Map<String, String> passwordData
    ) {
        try {
            // Valider que tous les champs requis sont présents
            if (passwordData.get("id") == null || passwordData.get("id").isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new ProfileResponse(
                                false,
                                "L'identifiant de l'utilisateur est requis",
                                null
                        )
                );
            }

            if (passwordData.get("oldPassword") == null || passwordData.get("oldPassword").isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new ProfileResponse(
                                false,
                                "Le mot de passe actuel est requis",
                                null
                        )
                );
            }

            if (passwordData.get("rawPassword") == null || passwordData.get("rawPassword").isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new ProfileResponse(
                                false,
                                "Le nouveau mot de passe est requis",
                                null
                        )
                );
            }

            userser.updateUserPassword(
                    Integer.parseInt(passwordData.get("id")),
                    passwordData.get("oldPassword"),
                    passwordData.get("rawPassword")
            );
            return ResponseEntity.ok(
                    new ProfileResponse(
                            true,
                            "Mot de passe mis à jour avec succès",
                            null
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ProfileResponse(
                            false,
                            "Échec de la mise à jour du mot de passe: " + e.getMessage(),
                            null
                    )
            );
        }
    }

    /**
     * Récupère tous les utilisateurs (RH uniquement)
     * GET /api/v1/users/rh/all
     */
    @PreAuthorize("hasAuthority('RH')")
    @GetMapping("/rh/all")
    public HttpEntity<UsersListResponse> getAllUsers() {
        try {
            List<User> users = userser.getAllUsers();
            List<UserDto> userDtos = users.stream()
                    .map(userMapper::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(
                    new UsersListResponse(
                            true,
                            "Liste de tous les utilisateurs récupérée avec succès",
                            userDtos
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new UsersListResponse(
                            false,
                            "Échec de la récupération des utilisateurs: " + e.getMessage(),
                            null
                    )
            );
        }
    }

    /**
     * Récupère tous les employés (RH uniquement)
     * GET /api/v1/users/rh/employees
     */
    @PreAuthorize("hasAuthority('RH')")
    @GetMapping("/rh/employees")
    public HttpEntity<UsersListResponse> getEmployees() {
        try {
            List<User> employees = userser.getUsersByRole("EMPLOYEE");
            List<UserDto> employeeDtos = employees.stream()
                    .map(userMapper::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(
                    new UsersListResponse(
                            true,
                            "Liste des employés récupérée avec succès",
                            employeeDtos
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new UsersListResponse(
                            false,
                            "Échec de la récupération des employés: " + e.getMessage(),
                            null
                    )
            );
        }
    }
}