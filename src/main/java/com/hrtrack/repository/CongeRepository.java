package com.hrtrack.repository;

import com.hrtrack.domain.entities.Conge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CongeRepository extends JpaRepository<Conge, Integer> {
    Conge findByCongeUuid(UUID congeUuid);
}
