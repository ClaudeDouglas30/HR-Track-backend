package com.hrtrack.repository;

import com.hrtrack.domain.entities.JwtToken;
import com.hrtrack.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface JwtTokenRepository extends JpaRepository<JwtToken, Long> {

    @Query(value = """
         select t from JwtToken t inner join User u on t.user.id = u.id\s\s
              where u.id = :id and t.expired = false or t.revoked = false\s
    \s""")
    List<JwtToken> findAllByUserId(Long id);

    Optional<JwtToken> findByToken(String token);

    Optional<JwtToken> findByRefreshToken(String refreshToken);
}
