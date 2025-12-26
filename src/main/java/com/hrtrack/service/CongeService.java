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
     * Permet d'ajouter des justificatifs à un congé existant
     * @param uuid uuid du congé
     * @param files fichiers justificatifs à ajouter
     * @return le congé mis à jour avec les nouveaux justificatifs
     */
    CongeDto addJustificatifsToConge(UUID uuid, List<MultipartFile> files);

    /**
     * Permet d'ajouter ou modifier un commentaire RH sur un congé
     * @param uuid uuid du congé
     * @param commentaireRH le commentaire à ajouter
     * @return le congé mis à jour
     */
    CongeDto addCommentaireRH(UUID uuid, String commentaireRH);

}