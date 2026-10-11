package ratelimiter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ratelimiter.model.TokenBucketState;

@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final RedisTokenBucketService redisTokenBucketService;

    public TokenBucketState consumeTokens(String clientId, long tokens) {
        return redisTokenBucketService.consume(clientId, tokens);
    }

    public TokenBucketState getStatus(String clientId) {
        return redisTokenBucketService.getStatus(clientId);
    }
}
