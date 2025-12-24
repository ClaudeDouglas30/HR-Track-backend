package com.hrtrack.domain.requests.typeConge;

import com.hrtrack.domain.Libelle;

/**
 * Request pour mettre à jour un type de congé.
 */
public record UpdateTypeCongeRequest(
        Libelle libelle,

        Integer quotaAnnuel,
        
        String description // Note: faute de frappe dans l'entité (descirption au lieu de description)
) {
}

