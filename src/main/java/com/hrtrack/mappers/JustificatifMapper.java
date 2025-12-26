package com.hrtrack.mappers;

import com.hrtrack.domain.dtos.JustificatifDto;
import com.hrtrack.domain.entities.Justificatif;
import com.hrtrack.utils.FileUrlUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class JustificatifMapper {

    @Autowired
    protected FileUrlUtil fileUrlUtil;

    @Mapping(target = "urlFichier", expression = "java(convertFilePath(justificatif.getNomFichier()))")
    public abstract JustificatifDto toDto(Justificatif justificatif);

    public abstract Justificatif toEntity(JustificatifDto justificatifDto);

    /**
     * Convertit le UUID du justificatif en URL HTTP accessible
     */
    protected String convertFilePath(java.util.UUID fileUuid) {
        if (fileUuid == null) {
            return null;
        }
        // Le fichier est stocké comme: storage/justificatifs/{uuid}.extension
        // On doit utiliser le service FileStorageService pour obtenir le chemin complet
        String filePath = "storage/justificatifs/" + fileUuid.toString();
        return fileUrlUtil.convertToUrl(filePath);
    }
}