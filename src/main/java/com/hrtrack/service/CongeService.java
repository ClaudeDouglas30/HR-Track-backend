package com.hrtrack.service;


import com.hrtrack.domain.CongeStatus;
import com.hrtrack.domain.dtos.CongeDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface CongeService {

    /***
     * Liste de toutes les demandes de congés
     * @return
     */
    List<CongeDto> getAllConge();

    /**
     * Obtenir les details d'un elements conge
     * @param uuid
     * @return
     */
    CongeDto getCongeById(UUID uuid);

    /**
     * Permet de changer le status d'un congé
     * @param uuid
     * @return
     */
    CongeDto changeCongeStatus(UUID uuid, CongeStatus status);

    /**
     * Permet de mettre à jour une demande de congé
     * @param uuid uuid d'un congé
     * @param congeDto informations à modifier
     * @param files fichiers justificatifs à ajouter (optionnel)
     * @return
     */
    CongeDto editConge(UUID uuid, CongeDto congeDto, List<MultipartFile> files);


    /**
     * Obtenir la liste de mes conge
     * @return la liste des congés associés à un utilisateur
     */
    List<CongeDto> getMyListOfConge();

    /**
     * Permet de créer un congé
     * @param congeDto les informations du congé à créer
     * @param files fichiers justificatifs à ajouter (optionnel)
     * @return le congé créé par un utilisateur
     */
    CongeDto createConge(CongeDto congeDto, List<MultipartFile> files);

    /**
     * Permet de supprimer un congé
     * @param uuid uuid du congé à supprimer
     * @return un boolean
     */
    boolean deleteConge(UUID uuid);

    /**
     * Permet d'ajouter ou modifier une annotation RH sur un congé
     * @param uuid uuid du congé
     * @param annotationRH l'annotation à ajouter
     * @return le congé mis à jour
     */
    CongeDto addAnnotationRH(UUID uuid, String annotationRH);

    /**
     * Récupère les demandes de congés avec pagination et filtrage (pour RH/ADMIN).
     * @param status Filtre par statut (all, ENATTENTE, ACCEPTER, REFUSER)
     * @param searchTerm Terme de recherche (nom, prénom)
     * @param page Numéro de page (0-based)
     * @param size Taille de la page
     * @return Liste paginée des demandes de congés avec statistiques
     */
    com.hrtrack.domain.responses.conge.PaginatedLeaveRequestResponse getLeaveRequestsPaginated(
            String status, String searchTerm, int page, int size);

    /**
     * Refuse un congé avec un motif de refus.
     * @param uuid UUID du congé
     * @param motifRefus Motif du refus
     * @return Le congé mis à jour
     */
    CongeDto rejectCongeWithMotif(UUID uuid, String motifRefus);

}
