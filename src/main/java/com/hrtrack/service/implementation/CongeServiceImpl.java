package com.hrtrack.service.implementation;

import com.hrtrack.domain.CongeStatus;
import com.hrtrack.domain.dtos.CongeDto;
import com.hrtrack.domain.dtos.JustificatifDto;
import com.hrtrack.domain.entities.Conge;
import com.hrtrack.mappers.CongeMapper;
import com.hrtrack.repository.CongeRepository;
import com.hrtrack.service.CongeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CongeServiceImpl implements CongeService {

    private final CongeRepository congeRepository;
    private final CongeMapper congeMapper;


    @Override
    @Transactional(readOnly = true)
    public List<CongeDto> getAllConge() {
        return congeRepository.findAll()
                .stream()
                .map(this::mapCongeWithFiles)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CongeDto getCongeById(UUID uuid) {
        Conge conge = congeRepository.findByCongeUuid(uuid);
        return mapCongeWithFiles(conge);
    }

    @Override
    @Transactional
    public CongeDto changeCongeStatus(UUID uuid, CongeStatus status) {
        Conge conge = congeRepository.findByCongeUuid(uuid);
        if (conge == null) {
            return null;
        }

        conge.setCongeStatus(status);
        Conge savedConge = congeRepository.save(conge);
        return mapCongeWithFiles(savedConge);
    }

    private CongeDto mapCongeWithFiles(Conge conge) {
        if (conge == null) {
            return null;
        }
        CongeDto dto = congeMapper.toDto(conge);

        List<JustificatifDto> justificatifDtos = dto.getJustificatifs();
        if (justificatifDtos != null) {
            justificatifDtos.forEach(justificatif -> justificatif.setUrlFichier(justificatif.getNomFichier().toString()));
        }

        return dto;
    }
}
