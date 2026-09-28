package ratelimiter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ratelimiter.config.RateLimiterProperties;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;


//store token bucket state in redis
//manage tokens per client
//handle token refill based on time

@Service
@RequiredArgsConstructor
public class RedisTokenBucketService {
    private final JedisPool jedisPool;

    private final RateLimiterProperties properties;

    private final String TOKENS_KEY_PREFIX = "token_bucket:tokens";
    private static final String LAST_REFILL_TIME_KEY_PREFIX = "token_bucket:last_refill_time";


    public boolean isAllowed(String clientId) {
        String tokenKey = TOKENS_KEY_PREFIX + clientId;

        try(Jedis jedis = jedisPool.getResource()){

            refillTokens(clientId , jedis);

            String tokenStr = jedis.get(tokenKey);

            long currentTokens = tokenStr != null ? Long.parseLong(tokenStr) : 

        }

    }

}