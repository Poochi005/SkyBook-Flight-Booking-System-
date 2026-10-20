package com.flightbooking.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Simple public endpoints for local checks and hosting-platform health probes. */
@RestController
public class HealthController {

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
                "application", "SkyBook Flight Booking System",
                "status", "running",
                "health", "/health",
                "api", "/api"
        );
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
