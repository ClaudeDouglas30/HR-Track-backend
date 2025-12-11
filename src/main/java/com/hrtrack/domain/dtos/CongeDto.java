package com.hrtrack.domain.dtos;

import com.hrtrack.domain.CongeStatus;
import lombok.Builder;
import lombok.Data;


import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@Data
@Builder
public class CongeDto {
    private Long id;
    private UUID congeUuid;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private CongeStatus congeStatus;
    private TypeCongeDto typeConge;
    private UserDto user;
    private List<JustificatifDto> justificatifs;
}
