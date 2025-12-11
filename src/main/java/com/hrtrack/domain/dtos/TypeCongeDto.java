package com.hrtrack.domain.dtos;

import com.hrtrack.domain.Libelle;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class TypeCongeDto {
    private Libelle libelle;
    private int quotaAnnuel;
}
