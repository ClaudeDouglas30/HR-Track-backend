package com.hrtrack.domain.dtos;

import lombok.*;

/**
 * DTO simplifié pour les informations utilisateur dans les messages
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSimpleDto {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
}