package ratelimiter.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ratelimiter.model.TokenBucketState;
import ratelimiter.service.RateLimiterService;

@RestController
@RequestMapping("/api/v1/rate-limit")
@RequiredArgsConstructor
@Validated
public class RateLimiterController {

    private final RateLimiterService rateLimiterService;

    @PostMapping("/consume")
    public TokenBucketState consume(
            @RequestParam @NotBlank String clientId,
            @RequestParam(defaultValue = "1") @Min(1) long tokens
    ) {
        return rateLimiterService.consumeTokens(clientId, tokens);
    }

    @GetMapping("/status")
    public TokenBucketState status(@RequestParam @NotBlank String clientId) {
        return rateLimiterService.getStatus(clientId);
    }
}
