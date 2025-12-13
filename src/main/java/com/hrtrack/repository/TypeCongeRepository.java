package com.hrtrack.repository;

import com.hrtrack.domain.Libelle;
import com.hrtrack.domain.entities.TypeConge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TypeCongeRepository extends JpaRepository<TypeConge, Integer> {
    Optional<TypeConge> findByLibelle(Libelle libelle);
}
