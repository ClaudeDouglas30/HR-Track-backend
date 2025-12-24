package com.hrtrack.service;

import com.hrtrack.domain.dtos.TypeCongeDto;

import java.util.List;

/**
 * Service pour la gestion des types de congés.
 */
public interface TypeCongeService {

    /**
     * Récupère la liste de tous les types de congés.
     * @return Liste de tous les types de congés
     */
    List<TypeCongeDto> getAllTypeConges();

    /**
     * Récupère un type de congé par son ID.
     * @param id L'ID du type de congé
     * @return Le type de congé trouvé, null si non trouvé
     */
    TypeCongeDto getTypeCongeById(Integer id);

    /**
     * Crée un nouveau type de congé.
     * @param libelle Le libellé du type de congé
     * @param quotaAnnuel Le quota annuel
     * @param descirption La description (optionnelle)
     * @return Le type de congé créé
     */
    TypeCongeDto createTypeConge(com.hrtrack.domain.Libelle libelle, Integer quotaAnnuel, String descirption);

    /**
     * Met à jour un type de congé.
     * @param id L'ID du type de congé à modifier
     * @param libelle Le nouveau libellé (optionnel)
     * @param quotaAnnuel Le nouveau quota annuel (optionnel)
     * @param descirption La nouvelle description (optionnelle)
     * @return Le type de congé mis à jour, null si non trouvé
     */
    TypeCongeDto updateTypeConge(Integer id, com.hrtrack.domain.Libelle libelle, Integer quotaAnnuel, String descirption);

    /**
     * Supprime un type de congé.
     * @param id L'ID du type de congé à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    boolean deleteTypeConge(Integer id);
}

