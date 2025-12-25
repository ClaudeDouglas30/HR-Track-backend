package com.hrtrack.repository;

import com.hrtrack.domain.entities.Pointage;
import com.hrtrack.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface PointageRepository extends JpaRepository<Pointage, Long> {
    List<Pointage> findByUser(User user);
    List<Pointage> findByUserAndDateBetween(User user, Date startDate, Date endDate);

    // Pour un employé
    List<Pointage> findByUserId(Long userId);

}

