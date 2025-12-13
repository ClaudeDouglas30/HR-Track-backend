package com.hrtrack.mappers;

import com.hrtrack.domain.dtos.JustificatifDto;
import com.hrtrack.domain.entities.Justificatif;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JustificatifMapper {
    JustificatifDto toDto(Justificatif justificatif);
    Justificatif toEntity(JustificatifDto justificatifDto);
}
