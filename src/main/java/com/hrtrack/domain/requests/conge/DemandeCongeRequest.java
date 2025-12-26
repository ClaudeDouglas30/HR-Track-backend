package com.hrtrack.domain.requests.conge;

import lombok.Builder;

@Builder
public record DemandeCongeRequest(
        String typeConge,
        String dateDebut,
        String dateFin,
        Integer nombreJours,
        String motif,
        String remplacant,
        Integer userId
) {
}