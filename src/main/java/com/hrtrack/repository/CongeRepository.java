package com.hrtrack.repository;

import com.hrtrack.domain.CongeStatus;
import com.hrtrack.domain.entities.Conge;
import com.hrtrack.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public interface CongeRepository extends JpaRepository<Conge, Integer> {
    Conge findByCongeUuid(UUID congeUuid);
    List<Conge> findByUser(User user);

    @Query("SELECT COUNT(c) FROM Conge c WHERE c.user.id = :userId AND c.congeStatus = :status AND c.dateFin < :date")
    int countByUserIdAndStatusAndDateFinBefore(@Param("userId") Long userId, @Param("status") CongeStatus status, @Param("date") Date date);

    @Query("SELECT COUNT(c) FROM Conge c WHERE c.congeStatus = :status")
    int countByStatus(@Param("status") com.hrtrack.domain.CongeStatus status);

    @Query("SELECT COUNT(DISTINCT c.user.id) FROM Conge c WHERE c.congeStatus = 'ACCEPTER' AND c.dateDebut <= :date AND c.dateFin >= :date")
    int countUsersEnConge(@Param("date") Date date);
}