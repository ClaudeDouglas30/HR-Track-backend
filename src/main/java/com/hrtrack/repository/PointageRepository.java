package com.hrtrack.repository;

import com.hrtrack.domain.entities.Pointage;
import com.hrtrack.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.Date;
import java.util.List;

public interface PointageRepository extends JpaRepository<Pointage, Long> {
    List<Pointage> findByUser(User user);
    List<Pointage> findByUserAndDateBetween(User user, Date startDate, Date endDate);

    // Pour un employé
    List<Pointage> findByUserId(Long userId);
    List<Pointage> findByUserIdAndDate(Long userId, Date date);

    @Query("SELECT DISTINCT p.date FROM Pointage p WHERE p.user.id = :userId AND YEAR(p.date) = :year AND MONTH(p.date) = :month")
    java.util.List<Date> findDistinctDatesByUserId(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);

    @Query("SELECT COUNT(DISTINCT p.user.id) FROM Pointage p WHERE p.date = :date")
    int countDistinctUserIdByDate(@Param("date") Date date);

    @Query("SELECT MIN(p.heureArrivee) FROM Pointage p WHERE p.user.id = :userId AND p.date = :date")
    LocalTime findFirstPointageTimeByUserIdAndDate(@Param("userId") Long userId, @Param("date") Date date);
}