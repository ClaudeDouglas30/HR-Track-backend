package com.hrtrack.mappers;


import com.hrtrack.domain.dtos.CongeDto;
import com.hrtrack.domain.entities.Conge;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CongeMapper {
    CongeDto toDto(Conge conge);
    Conge toEntity(CongeDto congeDto);
}
