package ratelimiter.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ratelimiter.config.RateLimiterProperties;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestRedisController {
    private final StringRedisTemplate redisTemplate;
    private final RateLimiterProperties properties;
    @GetMapping
    public String testRedis(){
        redisTemplate.opsForValue().set("rateLimiterTest", "working");

        return redisTemplate.opsForValue().get("rateLimiterTest");
    }

    @GetMapping("/config")
    public String config(){
        return "Capacity:" + properties.getCapacity()
                + " , RefillRate:" + properties.getRefillRate()
                + " , Duration:" + properties.getTimeout();
    }

}
