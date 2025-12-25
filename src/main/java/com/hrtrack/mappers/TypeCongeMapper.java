package com.hrtrack.mappers;

import com.hrtrack.domain.dtos.TypeCongeDto;
import com.hrtrack.domain.entities.TypeConge;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TypeCongeMapper {
    TypeCongeDto toDto(TypeConge typeConge);
    TypeConge toEntity(TypeCongeDto typeCongeDto);
}

