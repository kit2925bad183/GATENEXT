package com.gatenest.controller;

import com.gatenest.dto.ApiResponse;
import com.gatenest.service.DemoDataSeederService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/demo")
public class AdminDemoController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminDemoController.class);
    private final DemoDataSeederService demoDataSeederService;
    private final JdbcTemplate jdbcTemplate;

    @Value("${gatenest.bootstrap.admin-token:demo-gatenest-admin-presentation-2026}")
    private String bootstrapToken;

    public AdminDemoController(DemoDataSeederService demoDataSeederService, JdbcTemplate jdbcTemplate) {
        this.demoDataSeederService = demoDataSeederService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDemoStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("isDemoEnvironment", demoDataSeederService.isDemoEnvironment());

        try {
            Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
            Integer visitCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM visits", Integer.class);
            Integer flatCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM flats", Integer.class);
            String communityName = jdbcTemplate.queryForObject("SELECT community_name FROM community_settings LIMIT 1", String.class);

            status.put("communityName", communityName);
            status.put("userCount", userCount);
            status.put("visitCount", visitCount);
            status.put("flatCount", flatCount);
            status.put("databaseConfigured", true);
        } catch (Exception e) {
            status.put("databaseConfigured", false);
            status.put("error", e.getMessage());
        }

        return ResponseEntity.ok(ApiResponse.ok("Demo environment status", status));
    }

    @PostMapping("/seed")
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedDemo(
            @RequestParam(defaultValue = "false") boolean forceReset,
            @RequestHeader(value = "X-Admin-Bootstrap-Token", required = false) String token
    ) {
        try {
            Map<String, Object> stats = demoDataSeederService.seedDemoDataset(forceReset);
            return ResponseEntity.ok(ApiResponse.ok("Demo dataset seeded successfully", stats));
        } catch (SecurityException e) {
            log.warn("Security exception in seedDemo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to seed demo dataset", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to seed demo dataset: " + e.getMessage()));
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resetDemo(
            @RequestHeader(value = "X-Admin-Bootstrap-Token", required = false) String token
    ) {
        try {
            Map<String, Object> stats = demoDataSeederService.seedDemoDataset(true);
            return ResponseEntity.ok(ApiResponse.ok("Demo dataset cleanly reset and re-seeded successfully", stats));
        } catch (SecurityException e) {
            log.warn("Security exception in resetDemo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to reset demo dataset", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to reset demo dataset: " + e.getMessage()));
        }
    }
}
