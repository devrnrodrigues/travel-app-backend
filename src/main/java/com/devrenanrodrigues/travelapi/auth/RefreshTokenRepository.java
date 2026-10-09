package com.devrenanrodrigues.travelapi.auth;

import com.devrenanrodrigues.travelapi.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.user = :user AND r.revoked = false")
    void revokeAllByUser(@Param("user") User user);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM RefreshToken r WHERE r.expiresAt < :now OR (r.revoked = true AND (r.updatedAt < :revokedCutoff OR (r.updatedAt IS NULL AND r.createdAt < :revokedCutoff)))")
    int deleteExpiredOrRevokedBefore(@Param("now") Instant now, @Param("revokedCutoff") Instant revokedCutoff);
}
