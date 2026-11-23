package com.devrenanrodrigues.travelapi.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitServiceTest {

    private RateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        rateLimitService = new RateLimitService();
    }

    @Test
    @DisplayName("Deve permitir requisições dentro do limite do Tier")
    void shouldAllowRequestsWithinLimit() {
        String clientIp = "192.168.1.100";

        boolean allowed = rateLimitService.tryAcquire(clientIp, RateLimitTier.AUTH);

        assertThat(allowed).isTrue();
    }

    @Test
    @DisplayName("Deve isolar limites entre IPs e Tiers diferentes")
    void shouldIsolateLimitsBetweenIpsAndTiers() {
        String ip1 = "10.0.0.1";
        String ip2 = "10.0.0.2";

        boolean allowedIp1 = rateLimitService.tryAcquire(ip1, RateLimitTier.EXTERNAL);
        boolean allowedIp2 = rateLimitService.tryAcquire(ip2, RateLimitTier.EXTERNAL);
        boolean allowedIp1Upload = rateLimitService.tryAcquire(ip1, RateLimitTier.UPLOAD);

        assertThat(allowedIp1).isTrue();
        assertThat(allowedIp2).isTrue();
        assertThat(allowedIp1Upload).isTrue();
    }

    @Test
    @DisplayName("Deve executar limpeza de buckets inativos sem falhas")
    void shouldCleanupIdleBucketsWithoutErrors() {
        rateLimitService.tryAcquire("127.0.0.1", RateLimitTier.GENERAL);
        rateLimitService.cleanupIdleBuckets();
    }
}
