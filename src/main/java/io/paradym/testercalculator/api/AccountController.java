package io.paradym.testercalculator.api;

import io.paradym.testercalculator.api.dto.MeResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AccountController {

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal UserDetails user) {
        return new MeResponse(user.getUsername());
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
