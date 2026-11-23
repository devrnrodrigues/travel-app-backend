package com.devrenanrodrigues.travelapi.ratelimit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class TokenBucketTest {

    @Test
    @DisplayName("Deve permitir consumo até a capacidade máxima inicial")
    void shouldAllowConsumeUpToCapacity() {
        TokenBucket bucket = new TokenBucket(3, Duration.ofMinutes(1));

        assertThat(bucket.tryConsume()).isTrue();
        assertThat(bucket.tryConsume()).isTrue();
        assertThat(bucket.tryConsume()).isTrue();
        assertThat(bucket.tryConsume()).isFalse();
    }

    @Test
    @DisplayName("Deve recarregar tokens após passagem do tempo")
    void shouldRefillTokensOverTime() throws InterruptedException {
        // Capacidade 1 token por 20 milissegundos
        TokenBucket bucket = new TokenBucket(1, Duration.ofMillis(20));

        assertThat(bucket.tryConsume()).isTrue();
        assertThat(bucket.tryConsume()).isFalse();

        Thread.sleep(40);

        assertThat(bucket.tryConsume()).isTrue();
    }

    @Test
    @DisplayName("Deve identificar se o bucket está ocioso baseado em threshold")
    void shouldCheckIfBucketIsIdle() {
        TokenBucket bucket = new TokenBucket(5, Duration.ofMillis(10));

        boolean isIdle = bucket.isIdle(0);
        assertThat(isIdle).isTrue();
    }
}
