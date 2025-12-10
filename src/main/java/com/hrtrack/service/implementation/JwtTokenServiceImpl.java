package com.hrtrack.service.implementation;

import com.hrtrack.domain.entities.JwtToken;
import com.hrtrack.domain.entities.User;
import com.hrtrack.repository.JwtTokenRepository;
import com.hrtrack.service.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class JwtTokenServiceImpl implements JwtTokenService {

    private final JwtTokenRepository jwtTokenRepository;

    @Override
    public void saveUserToken(User user, String jwtToken, String refreshToken) {
        var token = JwtToken.builder()
                .user(user)
                .token(jwtToken)
                .refreshToken(refreshToken)
                .expired(false)
                .revoked(false)
                .build();

        jwtTokenRepository.save(token);
    }

    @Override
    public void revokeAllUserTokens(User user) {
        var tokens = jwtTokenRepository.findAllByUserId((long) user.getId());

        if (tokens.isEmpty()) {
            return;
        }
        jwtTokenRepository.deleteAll(tokens);
    }

    @Override
    public boolean isTokenValid(String jwtToken) {
        return jwtTokenRepository.findByToken(jwtToken)
                .map(t -> !t.isExpired() && !t.isRevoked())
                .orElse(false);
    }

    @Override
    public boolean isRefreshTokenValid(String refreshToken) {
        return jwtTokenRepository.findByRefreshToken(refreshToken)
                .map(t -> !t.isExpired() && !t.isRevoked())
                .orElse(false);
    }

    @Override
    public void rotateUserTokens(User user, String jwtToken, String refreshToken, String oldToken) {
        var storedToken = jwtTokenRepository.findByRefreshToken(oldToken)
                .orElseThrow(() -> new RuntimeException("Token introuvable"));

        storedToken.setRevoked(true);
        storedToken.setExpired(true);

        var newToken = JwtToken.builder()
                .user(user)
                .token(jwtToken)
                .refreshToken(refreshToken)
                .expired(false)
                .revoked(false)
                .build();

        jwtTokenRepository.save(storedToken);
        jwtTokenRepository.save(newToken);
    }
}
