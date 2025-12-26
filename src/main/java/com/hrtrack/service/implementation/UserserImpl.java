package com.hrtrack.service.implementation;

import com.hrtrack.domain.Role;
import com.hrtrack.domain.entities.User;
import com.hrtrack.exception.UserException;
import com.hrtrack.repository.UserRepository;
import com.hrtrack.service.Userser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserserImpl implements Userser {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${file.profile-dir}")
    private String uploadDir;

    @Override
    public User updateUserProfile(int id, String nom, String prenom, String email, String telephone, MultipartFile profilePicture) {
        Optional<User> u = userRepository.findById((long) id);

        if (u.isEmpty()) {
            throw new UserException("Utilisateur non trouvé");
        }

        User user = u.get();

        // Mise à jour du nom
        if (nom != null && !nom.trim().isEmpty()) {
            user.setNom(nom);
        }

        // Mise à jour du prénom
        if (prenom != null && !prenom.trim().isEmpty()) {
            user.setPrenom(prenom);
        }

        // Mise à jour de l'email
        if (email != null && !email.trim().isEmpty()) {
            // Vérifier que l'email n'est pas déjà utilisé par un autre utilisateur
            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
                throw new UserException("Cet email est déjà utilisé par un autre utilisateur");
            }
            user.setEmail(email);
        }

        // Mise à jour du téléphone
        if (telephone != null && !telephone.trim().isEmpty()) {
            user.setTelephone(telephone);
        }

        // Mise à jour de la photo de profil
        if (profilePicture != null && !profilePicture.isEmpty()) {
            String photoPath = saveProfilePicture(id, profilePicture, user.getPhotoProfil());
            user.setPhotoProfil(photoPath);
        }

        return userRepository.save(user);
    }

    /**
     * Sauvegarde la photo de profil de manière sécurisée
     */
    private String saveProfilePicture(int userId, MultipartFile file, String oldPhotoPath) {
        try {
            // Créer le répertoire s'il n'existe pas
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Répertoire de photos de profil créé : {}", uploadPath);
            }

            // Valider le type de fichier
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new UserException("Le fichier doit être une image (jpg, png, etc.)");
            }

            // Valider la taille du fichier (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new UserException("La taille de l'image ne doit pas dépasser 5MB");
            }

            // Nettoyer le nom du fichier original
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

            // Vérifier qu'il n'y a pas de caractères dangereux
            if (originalFilename.contains("..")) {
                throw new UserException("Le nom du fichier contient une séquence de chemin invalide");
            }

            // Obtenir l'extension du fichier
            String fileExtension = "";
            int lastDotIndex = originalFilename.lastIndexOf('.');
            if (lastDotIndex > 0) {
                fileExtension = originalFilename.substring(lastDotIndex);
            }

            // Générer un nom de fichier unique
            String newFileName = "profile_" + userId + "_" + UUID.randomUUID() + fileExtension;
            Path targetLocation = uploadPath.resolve(newFileName);

            // Copier le fichier vers le répertoire cible
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("Photo de profil sauvegardée : {}", targetLocation);

            // Supprimer l'ancienne photo si elle existe
            if (oldPhotoPath != null && !oldPhotoPath.isEmpty()) {
                try {
                    Path oldPath = Paths.get(oldPhotoPath);
                    if (Files.exists(oldPath)) {
                        Files.delete(oldPath);
                        log.info("Ancienne photo de profil supprimée : {}", oldPath);
                    }
                } catch (IOException e) {
                    log.warn("Impossible de supprimer l'ancienne photo : {}", e.getMessage());
                    // Ne pas bloquer si la suppression échoue
                }
            }

            // Retourner le chemin relatif pour la base de données
            return targetLocation.toString();

        } catch (IOException e) {
            log.error("Erreur lors de la sauvegarde de la photo de profil", e);
            throw new UserException("Erreur lors de la sauvegarde de la photo : " + e.getMessage());
        }
    }

    @Override
    public void updateUserPassword(int id, String currentPassword, String newPassword) {
        // Valider les paramètres d'entrée
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            throw new UserException("Le mot de passe actuel est requis");
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new UserException("Le nouveau mot de passe est requis");
        }

        Optional<User> u = userRepository.findById((long) id);

        if (u.isEmpty()) {
            throw new UserException("Utilisateur non trouvé");
        }

        User user = u.get();

        // Vérifier que le mot de passe actuel est correct (comparaison avec hash)
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new UserException("Mot de passe actuel incorrect");
        }

        // Valider le nouveau mot de passe
        if (newPassword.length() < 6) {
            throw new UserException("Le nouveau mot de passe doit contenir au moins 6 caractères");
        }

        // Hasher et sauvegarder le nouveau mot de passe
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Mot de passe mis à jour pour l'utilisateur ID: {}", id);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> getUsersByRole(String role) {
        try {
            Role roleEnum = Role.valueOf(role.toUpperCase());
            return userRepository.findByRole(roleEnum);
        } catch (IllegalArgumentException e) {
            throw new UserException("Rôle invalide: " + role);
        }
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserException("Utilisateur non authentifié");
        }

        String email;
        if (authentication.getPrincipal() instanceof UserDetails) {
            email = ((UserDetails) authentication.getPrincipal()).getUsername();
        } else {
            email = authentication.getPrincipal().toString();
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException("Utilisateur non trouvé"));
    }
}