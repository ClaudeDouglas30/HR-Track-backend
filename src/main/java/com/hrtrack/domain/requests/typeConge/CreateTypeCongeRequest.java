package com.hrtrack.domain.requests.typeConge;

import com.hrtrack.domain.Libelle;

/**
 * Request pour créer un nouveau type de congé.
 */
public record CreateTypeCongeRequest(
        Libelle libelle,

        Integer quotaAnnuel,
        
        String description // Note: faute de frappe dans l'entité (descirption au lieu de description)
) {
}

