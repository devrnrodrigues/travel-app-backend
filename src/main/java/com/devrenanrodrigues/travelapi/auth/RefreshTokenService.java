package com.devrenanrodrigues.travelapi.auth;

import com.devrenanrodrigues.travelapi.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration-days:30}")
    private long refreshTokenExpirationDays;

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", ""))
                .expiresAt(Instant.now().plus(refreshTokenExpirationDays, ChronoUnit.DAYS))
                .revoked(false)
                .build();

        return refreshTokenRepository.saveAndFlush(refreshToken);
    }

    @Transactional
    public RefreshToken verifyAndRotate(String tokenString) {
        RefreshToken existingToken = refreshTokenRepository.findByToken(tokenString)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token inválido."));

        if (existingToken.isRevoked()) {
            refreshTokenRepository.revokeAllByUser(existingToken.getUser());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token revogado. Por favor, faça login novamente.");
        }

        if (existingToken.isExpired()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expirado. Por favor, faça login novamente.");
        }

        existingToken.setRevoked(true);
        refreshTokenRepository.saveAndFlush(existingToken);

        return createRefreshToken(existingToken.getUser());
    }

    @Transactional
    public void revokeToken(String tokenString) {
        refreshTokenRepository.findByToken(tokenString).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.saveAndFlush(token);
        });
    }
}
