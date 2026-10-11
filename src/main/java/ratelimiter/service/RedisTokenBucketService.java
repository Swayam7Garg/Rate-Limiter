package ratelimiter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ratelimiter.config.RateLimiterProperties;
import ratelimiter.model.TokenBucketState;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisTokenBucketService {

    private static final String TOKENS_KEY_PREFIX = "token_bucket:tokens:";
    private static final String LAST_REFILL_TIME_KEY_PREFIX = "token_bucket:last_refill_time:";

    private static final String CONSUME_LUA = """
            local tokenKey = KEYS[1]
            local lastRefillKey = KEYS[2]

            local capacity = tonumber(ARGV[1])
            local refillRate = tonumber(ARGV[2])
            local now = tonumber(ARGV[3])
            local requested = tonumber(ARGV[4])
            local ttl = tonumber(ARGV[5])

            local tokens = tonumber(redis.call('GET', tokenKey))
            local lastRefill = tonumber(redis.call('GET', lastRefillKey))

            if tokens == nil then
              tokens = capacity
            end
            if lastRefill == nil then
              lastRefill = now
            end

            local elapsed = math.max(0, now - lastRefill)
            local refill = math.floor((elapsed / 1000) * refillRate)

            if refill > 0 then
              tokens = math.min(capacity, tokens + refill)
              lastRefill = now
            end

            local allowed = 0
            if tokens >= requested then
              tokens = tokens - requested
              allowed = 1
            end

            redis.call('SET', tokenKey, tokens, 'EX', ttl)
            redis.call('SET', lastRefillKey, lastRefill, 'EX', ttl)

            return {allowed, tokens, lastRefill}
            """;

    private static final String STATUS_LUA = """
            local tokenKey = KEYS[1]
            local lastRefillKey = KEYS[2]

            local capacity = tonumber(ARGV[1])
            local refillRate = tonumber(ARGV[2])
            local now = tonumber(ARGV[3])
            local ttl = tonumber(ARGV[4])

            local tokens = tonumber(redis.call('GET', tokenKey))
            local lastRefill = tonumber(redis.call('GET', lastRefillKey))

            if tokens == nil then
              tokens = capacity
            end
            if lastRefill == nil then
              lastRefill = now
            end

            local elapsed = math.max(0, now - lastRefill)
            local refill = math.floor((elapsed / 1000) * refillRate)

            if refill > 0 then
              tokens = math.min(capacity, tokens + refill)
              lastRefill = now
            end

            redis.call('SET', tokenKey, tokens, 'EX', ttl)
            redis.call('SET', lastRefillKey, lastRefill, 'EX', ttl)

            return {tokens, lastRefill}
            """;

    private final JedisPool jedisPool;
    private final RateLimiterProperties properties;

    public TokenBucketState consume(String clientId, long requestedTokens) {
        String tokenKey = TOKENS_KEY_PREFIX + clientId;
        String lastRefillTimeKey = LAST_REFILL_TIME_KEY_PREFIX + clientId;
        long now = System.currentTimeMillis();

        try (Jedis jedis = jedisPool.getResource()) {
            Object raw = jedis.eval(
                    CONSUME_LUA,
                    List.of(tokenKey, lastRefillTimeKey),
                    List.of(
                            String.valueOf(properties.getCapacity()),
                            String.valueOf(properties.getRefillRate()),
                            String.valueOf(now),
                            String.valueOf(requestedTokens),
                            String.valueOf(properties.getBucketTtlSeconds())
                    )
            );

            List<?> response = (List<?>) raw;
            boolean allowed = toLong(response.get(0)) == 1L;
            long remainingTokens = toLong(response.get(1));

            return new TokenBucketState(
                    clientId,
                    allowed,
                    remainingTokens,
                    properties.getCapacity(),
                    properties.getRefillRate(),
                    now
            );
        }
    }

    public TokenBucketState getStatus(String clientId) {
        String tokenKey = TOKENS_KEY_PREFIX + clientId;
        String lastRefillTimeKey = LAST_REFILL_TIME_KEY_PREFIX + clientId;
        long now = System.currentTimeMillis();

        try (Jedis jedis = jedisPool.getResource()) {
            Object raw = jedis.eval(
                    STATUS_LUA,
                    List.of(tokenKey, lastRefillTimeKey),
                    List.of(
                            String.valueOf(properties.getCapacity()),
                            String.valueOf(properties.getRefillRate()),
                            String.valueOf(now),
                            String.valueOf(properties.getBucketTtlSeconds())
                    )
            );

            List<?> response = (List<?>) raw;
            long remainingTokens = toLong(response.get(0));

            return new TokenBucketState(
                    clientId,
                    true,
                    remainingTokens,
                    properties.getCapacity(),
                    properties.getRefillRate(),
                    now
            );
        }
    }

    private long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }
}
