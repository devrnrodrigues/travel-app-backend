package com.devrenanrodrigues.travelapi.ratelimit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitService {

    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public boolean tryAcquire(String clientKey, RateLimitTier tier) {
        String key = tier.name() + ":" + clientKey;
        TokenBucket bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(tier.getCapacity(), tier.getRefillPeriod()));
        return bucket.tryConsume();
    }

    @Scheduled(fixedRate = 600000)
    public void cleanupIdleBuckets() {
        long idleThresholdNanos = TimeUnit.MINUTES.toNanos(10);
        buckets.entrySet().removeIf(entry -> entry.getValue().isIdle(idleThresholdNanos));
    }
}
