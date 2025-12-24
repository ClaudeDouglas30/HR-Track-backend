package com.hrtrack.service.implementation;

import com.hrtrack.domain.Libelle;
import com.hrtrack.domain.dtos.TypeCongeDto;
import com.hrtrack.domain.entities.TypeConge;
import com.hrtrack.repository.TypeCongeRepository;
import com.hrtrack.service.TypeCongeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service d'implémentation pour la gestion des types de congés.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TypeCongeServiceImpl implements TypeCongeService {

    private final TypeCongeRepository typeCongeRepository;

    /**
     * Récupère la liste de tous les types de congés.
     * 
     * @return Liste de tous les types de congés
     */
    @Override
    @Transactional(readOnly = true)
    public List<TypeCongeDto> getAllTypeConges() {
        return typeCongeRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    /**
     * Récupère un type de congé par son ID.
     * 
     * @param id L'ID du type de congé
     * @return Le type de congé trouvé, null si non trouvé
     */
    @Override
    @Transactional(readOnly = true)
    public TypeCongeDto getTypeCongeById(Integer id) {
        return typeCongeRepository.findById(id)
                .map(this::mapToDto)
                .orElse(null);
    }

    /**
     * Crée un nouveau type de congé.
     * 
     * @param libelle Le libellé du type de congé
     * @param quotaAnnuel Le quota annuel
     * @param descirption La description (optionnelle)
     * @return Le type de congé créé
     */
    @Override
    @Transactional
    public TypeCongeDto createTypeConge(Libelle libelle, Integer quotaAnnuel, String descirption) {
        // Vérifier si un type de congé avec ce libellé existe déjà
        if (typeCongeRepository.findByLibelle(libelle).isPresent()) {
            log.warn("Tentative de création d'un type de congé avec un libellé déjà existant : {}", libelle);
            throw new RuntimeException("Un type de congé avec ce libellé existe déjà");
        }

        TypeConge typeConge = TypeConge.builder()
                .libelle(libelle)
                .quotaAnnuel(quotaAnnuel)
                .description(descirption)
                .build();

        TypeConge savedTypeConge = typeCongeRepository.save(typeConge);
        log.info("Nouveau type de congé créé : {} (ID: {})", savedTypeConge.getLibelle(), savedTypeConge.getId());
        
        return mapToDto(savedTypeConge);
    }

    /**
     * Met à jour un type de congé.
     * 
     * @param id L'ID du type de congé à modifier
     * @param libelle Le nouveau libellé (optionnel)
     * @param quotaAnnuel Le nouveau quota annuel (optionnel)
     * @param descirption La nouvelle description (optionnelle)
     * @return Le type de congé mis à jour, null si non trouvé
     */
    @Override
    @Transactional
    public TypeCongeDto updateTypeConge(Integer id, Libelle libelle, Integer quotaAnnuel, String descirption) {
        TypeConge typeConge = typeCongeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Type de congé non trouvé avec l'ID: " + id));

        // Vérifier si le nouveau libellé n'est pas déjà utilisé par un autre type de congé
        if (libelle != null && !libelle.equals(typeConge.getLibelle())) {
            if (typeCongeRepository.findByLibelle(libelle).isPresent()) {
                log.warn("Tentative de modification d'un type de congé avec un libellé déjà existant : {}", libelle);
                throw new RuntimeException("Un type de congé avec ce libellé existe déjà");
            }
            typeConge.setLibelle(libelle);
        }

        if (quotaAnnuel != null) {
            typeConge.setQuotaAnnuel(quotaAnnuel);
        }

        if (descirption != null) {
            typeConge.setDescription(descirption);
        }

        TypeConge savedTypeConge = typeCongeRepository.save(typeConge);
        log.info("Type de congé mis à jour : {} (ID: {})", savedTypeConge.getLibelle(), savedTypeConge.getId());
        
        return mapToDto(savedTypeConge);
    }

    /**
     * Supprime un type de congé.
     * 
     * @param id L'ID du type de congé à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    @Override
    @Transactional
    public boolean deleteTypeConge(Integer id) {
        if (!typeCongeRepository.existsById(id)) {
            log.warn("Tentative de suppression d'un type de congé inexistant : {}", id);
            return false;
        }

        typeCongeRepository.deleteById(id);
        log.info("Type de congé supprimé : {}", id);
        return true;
    }

    /**
     * Mappe une entité TypeConge vers un DTO TypeCongeDto.
     * 
     * @param typeConge L'entité TypeConge
     * @return Le DTO TypeCongeDto
     */
    private TypeCongeDto mapToDto(TypeConge typeConge) {
        return TypeCongeDto.builder()
                .id(typeConge.getId())
                .libelle(typeConge.getLibelle())
                .quotaAnnuel(typeConge.getQuotaAnnuel())
                .description(typeConge.getDescription())
                .build();
    }
}

