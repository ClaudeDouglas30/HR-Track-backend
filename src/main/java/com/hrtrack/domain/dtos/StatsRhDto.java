package com.hrtrack.domain.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatsRhDto {
    private int totalEmployes;
    private int demandesCongeEnAttente;
    private int pointagesUniquesAujourdhui;
    private int utilisateursEnConge;
}