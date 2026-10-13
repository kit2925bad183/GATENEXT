package com.gatenest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("app", "GateNest Apartment Security");
        health.put("version", "1.2.0-pwa");
        health.put("timestamp", Instant.now().toString());

        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            if (result != null && result == 1) {
                health.put("database", "CONNECTED");
                return ResponseEntity.ok(health);
            } else {
                health.put("status", "DOWN");
                health.put("database", "UNHEALTHY");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
            }
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("database", "DISCONNECTED");
            health.put("databaseError", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
        }
    }
}
