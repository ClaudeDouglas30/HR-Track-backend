package com.hrtrack.domain.validation;

import com.hrtrack.domain.dtos.CongeDto;
import com.hrtrack.domain.entities.Conge;
import com.hrtrack.domain.entities.TypeConge;
import com.hrtrack.domain.entities.User;
import com.hrtrack.repository.CongeRepository;
import com.hrtrack.repository.TypeCongeRepository;
import com.hrtrack.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Validateur pour les opérations de création et de mise à jour des congés.
 * Contient toutes les règles de validation métier pour les congés.
 */
@Component
@RequiredArgsConstructor
public class CongeValidator {

    private final TypeCongeRepository typeCongeRepository;
    private final CongeRepository congeRepository;

    /**
     * Valide les données d'un congé lors de sa création.
     * 
     * @param congeDto Le DTO du congé à valider
     * @param currentUser L'utilisateur qui crée le congé
     * @throws ValidationException Si la validation échoue
     */
    public void validateForCreation(CongeDto congeDto, User currentUser) {
        if (congeDto == null) {
            throw new ValidationException("Les données du congé ne peuvent pas être nulles");
        }

        // Validation de l'utilisateur
        validateUser(currentUser);

        // Validation des dates
        validateDates(congeDto.getDateDebut(), congeDto.getDateFin());

        // Validation du type de congé
        TypeConge typeConge = validateTypeConge(congeDto);

        // Validation des chevauchements de dates
        validateNoOverlap(congeDto.getDateDebut(), congeDto.getDateFin(), currentUser, null);
    }

    /**
     * Valide les données d'un congé lors de sa mise à jour.
     * 
     * @param congeDto Le DTO du congé à valider
     * @param existingConge Le congé existant à mettre à jour
     * @param currentUser L'utilisateur qui modifie le congé
     * @throws ValidationException Si la validation échoue
     */
    public void validateForUpdate(CongeDto congeDto, Conge existingConge, User currentUser) {
        if (congeDto == null) {
            throw new ValidationException("Les données du congé ne peuvent pas être nulles");
        }

        if (existingConge == null) {
            throw new ValidationException("Le congé à modifier n'existe pas");
        }

        // Validation de l'utilisateur
        validateUser(currentUser);

        // Vérification que l'utilisateur peut modifier ce congé
        if (!existingConge.getUser().getId().equals(currentUser.getId())) {
            throw new ValidationException("Vous ne pouvez modifier que vos propres congés");
        }

        // Vérification que le congé peut être modifié (seulement si ENATTENTE)
        if (existingConge.getCongeStatus() != com.hrtrack.domain.CongeStatus.ENATTENTE) {
            throw new ValidationException("Seuls les congés en attente peuvent être modifiés");
        }

        // Validation des dates si elles sont fournies
        LocalDate dateDebut = congeDto.getDateDebut() != null ? congeDto.getDateDebut() : existingConge.getDateDebut();
        LocalDate dateFin = congeDto.getDateFin() != null ? congeDto.getDateFin() : existingConge.getDateFin();

        if (dateDebut != null && dateFin != null) {
            validateDates(dateDebut, dateFin);
            // Validation des chevauchements (en excluant le congé actuel)
            validateNoOverlap(dateDebut, dateFin, currentUser, existingConge.getId());
        }

        // Validation du type de congé si fourni
        if (congeDto.getTypeConge() != null && congeDto.getTypeConge().getLibelle() != null) {
            validateTypeConge(congeDto);
        }
    }

    /**
     * Valide que l'utilisateur n'est pas null.
     * 
     * @param user L'utilisateur à valider
     * @throws ValidationException Si l'utilisateur est null
     */
    private void validateUser(User user) {
        if (user == null) {
            throw new ValidationException("L'utilisateur doit être authentifié pour effectuer cette opération");
        }
    }

    /**
     * Valide les dates de début et de fin du congé.
     * 
     * @param dateDebut La date de début
     * @param dateFin La date de fin
     * @throws ValidationException Si les dates sont invalides
     */
    private void validateDates(LocalDate dateDebut, LocalDate dateFin) {
        if (dateDebut == null) {
            throw new ValidationException("La date de début est obligatoire");
        }

        if (dateFin == null) {
            throw new ValidationException("La date de fin est obligatoire");
        }

        // La date de début ne peut pas être dans le passé
        if (dateDebut.isBefore(LocalDate.now())) {
            throw new ValidationException("La date de début ne peut pas être dans le passé");
        }

        // La date de fin ne peut pas être avant la date de début
        if (dateFin.isBefore(dateDebut)) {
            throw new ValidationException("La date de fin ne peut pas être antérieure à la date de début");
        }

        // Validation que la période n'est pas trop longue (par exemple, max 1 an)
        if (dateDebut.plusYears(1).isBefore(dateFin)) {
            throw new ValidationException("La période de congé ne peut pas dépasser 1 an");
        }
    }

    /**
     * Valide que le type de congé existe et est valide.
     * 
     * @param congeDto Le DTO contenant le type de congé
     * @return Le TypeConge validé
     * @throws ValidationException Si le type de congé est invalide
     */
    private TypeConge validateTypeConge(CongeDto congeDto) {
        if (congeDto.getTypeConge() == null || congeDto.getTypeConge().getLibelle() == null) {
            throw new ValidationException("Le type de congé est obligatoire");
        }

        TypeConge typeConge = typeCongeRepository.findByLibelle(congeDto.getTypeConge().getLibelle())
                .orElse(null);

        if (typeConge == null) {
            throw new ValidationException("Le type de congé spécifié n'existe pas");
        }

        return typeConge;
    }

    /**
     * Valide qu'il n'y a pas de chevauchement avec d'autres congés de l'utilisateur.
     * 
     * @param dateDebut La date de début du congé
     * @param dateFin La date de fin du congé
     * @param user L'utilisateur propriétaire du congé
     * @param excludeCongeId L'ID du congé à exclure de la vérification (pour les mises à jour)
     * @throws ValidationException Si un chevauchement est détecté
     */
    private void validateNoOverlap(LocalDate dateDebut, LocalDate dateFin, User user, Long excludeCongeId) {
        List<Conge> existingConges = congeRepository.findByUser(user);

        for (Conge existingConge : existingConges) {
            // Exclure le congé en cours de modification
            if (excludeCongeId != null && existingConge.getId().equals(excludeCongeId)) {
                continue;
            }

            // Vérifier le chevauchement uniquement pour les congés acceptés ou en attente
            if (existingConge.getCongeStatus() == com.hrtrack.domain.CongeStatus.ACCEPTER ||
                existingConge.getCongeStatus() == com.hrtrack.domain.CongeStatus.ENATTENTE) {

                LocalDate existingDebut = existingConge.getDateDebut();
                LocalDate existingFin = existingConge.getDateFin();

                // Vérifier si les périodes se chevauchent
                boolean overlaps = !dateFin.isBefore(existingDebut) && !dateDebut.isAfter(existingFin);

                if (overlaps) {
                    throw new ValidationException(
                            String.format("Cette période chevauche avec un congé existant du %s au %s",
                                    existingDebut, existingFin)
                    );
                }
            }
        }
    }

    /**
     * Valide qu'un congé peut être supprimé.
     * 
     * @param conge Le congé à valider
     * @param currentUser L'utilisateur qui tente de supprimer le congé
     * @throws ValidationException Si le congé ne peut pas être supprimé
     */
    public void validateForDeletion(Conge conge, User currentUser) {
        if (conge == null) {
            throw new ValidationException("Le congé à supprimer n'existe pas");
        }

        validateUser(currentUser);

        // Vérification que l'utilisateur peut supprimer ce congé
        if (!conge.getUser().getId().equals(currentUser.getId())) {
            throw new ValidationException("Vous ne pouvez supprimer que vos propres congés");
        }

        // Vérification que le congé peut être supprimé (seulement si ENATTENTE)
        if (conge.getCongeStatus() != com.hrtrack.domain.CongeStatus.ENATTENTE) {
            throw new ValidationException("Seuls les congés en attente peuvent être supprimés");
        }
    }
}

