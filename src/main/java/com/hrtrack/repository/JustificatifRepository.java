package com.hrtrack.repository;

import com.hrtrack.domain.entities.Justificatif;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JustificatifRepository extends JpaRepository<Justificatif, Long> {
    Optional<Justificatif> findByNomFichier(UUID nomFichier);
    List<Justificatif> findByCongeId(Long congeId);
}

