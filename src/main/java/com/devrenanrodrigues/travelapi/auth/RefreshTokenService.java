package com.devrenanrodrigues.travelapi.auth;

import com.devrenanrodrigues.travelapi.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final PlatformTransactionManager transactionManager;

    @Value("${jwt.refresh-token-expiration-days:30}")
    private long refreshTokenExpirationDays;

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        String rawToken = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(tokenHash)
                .rawToken(rawToken)
                .expiresAt(Instant.now().plus(refreshTokenExpirationDays, ChronoUnit.DAYS))
                .revoked(false)
                .build();

        return refreshTokenRepository.saveAndFlush(refreshToken);
    }

    @Transactional
    public RefreshToken verifyAndRotate(String tokenString) {
        String tokenHash = hashToken(tokenString);

        RefreshToken existingToken = refreshTokenRepository.findByToken(tokenHash)
                .or(() -> refreshTokenRepository.findByToken(tokenString))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token inválido."));

        if (existingToken.isRevoked()) {
            TransactionTemplate requiresNew = new TransactionTemplate(transactionManager);
            requiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            requiresNew.executeWithoutResult(status -> refreshTokenRepository.revokeAllByUser(existingToken.getUser()));
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
        String tokenHash = hashToken(tokenString);

        refreshTokenRepository.findByToken(tokenHash)
                .or(() -> refreshTokenRepository.findByToken(tokenString))
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.saveAndFlush(token);
                });
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeExpiredAndRevokedTokens() {
        Instant now = Instant.now();
        Instant revokedCutoff = now.minus(7, ChronoUnit.DAYS);
        refreshTokenRepository.deleteExpiredOrRevokedBefore(now, revokedCutoff);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 não disponível", e);
        }
    }
}
