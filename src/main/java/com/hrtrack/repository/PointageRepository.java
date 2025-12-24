package com.hrtrack.repository;

import com.hrtrack.domain.entities.Pointage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointageRepository extends JpaRepository<Pointage, Long> {

    // Pour un employé
    List<Pointage> findByUserId(Long userId);

}
