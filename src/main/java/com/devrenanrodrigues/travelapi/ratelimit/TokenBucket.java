package com.devrenanrodrigues.travelapi.ratelimit;

import java.time.Duration;

public class TokenBucket {

    private final long capacity;
    private final double refillRatePerNano;
    private double tokens;
    private long lastRefillNanos;

    public TokenBucket(long capacity, Duration refillPeriod) {
        this.capacity = capacity;
        this.tokens = capacity;
        this.refillRatePerNano = (double) capacity / refillPeriod.toNanos();
        this.lastRefillNanos = System.nanoTime();
    }

    public synchronized boolean tryConsume() {
        refill();
        if (tokens >= 1.0) {
            tokens -= 1.0;
            return true;
        }
        return false;
    }

    public synchronized boolean isIdle(long idleThresholdNanos) {
        refill();
        return tokens >= capacity && (System.nanoTime() - lastRefillNanos) > idleThresholdNanos;
    }

    private void refill() {
        long now = System.nanoTime();
        long elapsed = now - lastRefillNanos;
        if (elapsed > 0) {
            tokens = Math.min(capacity, tokens + elapsed * refillRatePerNano);
            lastRefillNanos = now;
        }
    }
}
