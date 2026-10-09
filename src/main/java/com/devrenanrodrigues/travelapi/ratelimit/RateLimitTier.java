package com.devrenanrodrigues.travelapi.ratelimit;

import java.time.Duration;

public enum RateLimitTier {
    AUTH(10, Duration.ofMinutes(1)),
    EXTERNAL(15, Duration.ofMinutes(1)),
    UPLOAD(20, Duration.ofMinutes(1)),
    GENERAL(120, Duration.ofMinutes(1));

    private final long capacity;
    private final Duration refillPeriod;

    RateLimitTier(long capacity, Duration refillPeriod) {
        this.capacity = capacity;
        this.refillPeriod = refillPeriod;
    }

    public long getCapacity() {
        return capacity;
    }

    public Duration getRefillPeriod() {
        return refillPeriod;
    }
}
