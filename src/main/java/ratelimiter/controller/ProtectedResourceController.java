package ratelimiter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/protected")
public class ProtectedResourceController {

    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of("message", "Request accepted by protected resource");
    }
}
