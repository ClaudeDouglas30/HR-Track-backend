package com.hrtrack.repository;

import com.hrtrack.domain.Role;
import com.hrtrack.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    User findByResetPasswordToken(String token);

    List<User> findByRole(Role role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    int countByRole(@Param("role") com.hrtrack.domain.Role role);
}