package com.hrtrack.domain.dtos;

import com.hrtrack.domain.Libelle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour les types de congés.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TypeCongeDto {
    private Integer id;
    private Libelle libelle;
    private Integer quotaAnnuel;
    private String description;
}
