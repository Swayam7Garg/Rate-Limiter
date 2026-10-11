package ratelimiter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ratelimiter.model.TokenBucketState;
import ratelimiter.service.RateLimiterService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TokenBucketRateLimiterFilter extends OncePerRequestFilter {

    private static final String PROTECTED_PREFIX = "/api/v1/protected";
    private static final String CLIENT_ID_HEADER = "X-Client-Id";

    private final RateLimiterService rateLimiterService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(PROTECTED_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String clientId = request.getHeader(CLIENT_ID_HEADER);
        if (clientId == null || clientId.isBlank()) {
            clientId = request.getRemoteAddr();
        }

        TokenBucketState decision = rateLimiterService.consumeTokens(clientId, 1);

        response.setHeader("X-RateLimit-Limit", String.valueOf(decision.capacity()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(decision.remainingTokens()));
        response.setHeader("X-RateLimit-Client", decision.clientId());

        if (!decision.allowed()) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"Rate limit exceeded\",\"clientId\":\"" + decision.clientId() + "\",\"remainingTokens\":" + decision.remainingTokens() + "}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
