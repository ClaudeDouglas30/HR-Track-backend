package com.hrtrack.service;


import com.hrtrack.domain.CongeStatus;
import com.hrtrack.domain.dtos.CongeDto;

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
}
