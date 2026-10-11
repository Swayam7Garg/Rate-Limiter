package ratelimiter.model;

public record TokenBucketState(
        String clientId,
        boolean allowed,
        long remainingTokens,
        long capacity,
        long refillRatePerSecond,
        long timestampMillis
) {
}
