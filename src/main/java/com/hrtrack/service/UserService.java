package com.hrtrack.service;

import com.hrtrack.domain.dtos.UserDto;
import com.hrtrack.exception.EmailingException;
import jakarta.mail.MessagingException;

import java.util.List;
import java.util.Map;

public interface UserService {

    /**
     * Récupère la liste de tous les utilisateurs.
     * @return Liste de tous les utilisateurs
     */
    List<UserDto> getAllUsers();

    /**
     * Valide le compte d'un utilisateur et envoie un email de confirmation.
     * @param userId L'ID de l'utilisateur à valider
     * @return L'utilisateur validé
     * @throws MessagingException
     * @throws EmailingException
     */
    UserDto validateUserAccount(Long userId) throws MessagingException, EmailingException;

    /**
     * Met à jour les informations d'un utilisateur.
     * @param userId L'ID de l'utilisateur à modifier
     * @param userDto Les nouvelles informations de l'utilisateur
     * @return L'utilisateur mis à jour
     */
    UserDto updateUser(Long userId, UserDto userDto);

    /**
     * Crée un nouvel utilisateur.
     * @param nom Le nom de l'utilisateur
     * @param prenom Le prénom de l'utilisateur
     * @param email L'email de l'utilisateur
     * @param telephone Le téléphone de l'utilisateur
     * @param password Le mot de passe (sera encodé)
     * @param role Le rôle de l'utilisateur
     * @param photoProfil La photo de profil (optionnel)
     * @return L'utilisateur créé
     */
    UserDto createUser(String nom, String prenom, String email, String telephone, String password, com.hrtrack.domain.Role role, String photoProfil);

    /**
     * Supprime un utilisateur.
     * @param userId L'ID de l'utilisateur à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    boolean deleteUser(Long userId);

    /**
     * Récupère les statistiques des utilisateurs.
     * @return Map contenant les statistiques (total, validés, non validés, par rôle, etc.)
     */
    Map<String, Object> getStatistics();

    /**
     * Récupère un utilisateur par son ID.
     * @param userId L'ID de l'utilisateur
     * @return L'utilisateur trouvé, null si non trouvé
     */
    UserDto getUserById(Long userId);

    /**
     * Récupère les détails complets d'un utilisateur avec toutes ses statistiques.
     * @param userId L'ID de l'utilisateur
     * @return Les détails complets de l'utilisateur avec statistiques, pointages et congés
     */
    com.hrtrack.domain.dtos.UserDetailDto getUserDetailById(Long userId);
}

