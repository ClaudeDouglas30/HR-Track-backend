package com.hrtrack.mappers;

import com.hrtrack.domain.dtos.UserDto;
import com.hrtrack.domain.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    
    /**
     * Mappe un User vers un UserDto.
     * Le mapping explicite pour accountValidated est nécessaire car MapStruct
     * peut avoir des difficultés avec les champs booléens commençant par "is".
     * Lombok génère isAccountValidated() comme getter, donc on utilise "accountValidated" comme source.
     */
    @Mapping(source = "accountValidated", target = "accountValidated")
    UserDto toDto(User user);
    
    /**
     * Mappe un UserDto vers un User.
     */
    @Mapping(source = "accountValidated", target = "isAccountValidated")
    User toEntity(UserDto userDto);
}
