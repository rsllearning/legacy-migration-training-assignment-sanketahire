package com.social.rollback.controller;

import com.social.rollback.FeatureFlagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin Management API providing runtime control over feature flags
 * for instant, zero-downtime rollbacks and canary traffic control.
 */
@RestController
@RequestMapping("/api/admin/rollback")
public class AdminRollbackController {

    private final FeatureFlagService featureFlagService;

    public AdminRollbackController(FeatureFlagService featureFlagService) {
        this.featureFlagService = featureFlagService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> toggleV2Master(
            @RequestParam(name = "v2", defaultValue = "false") boolean v2Enabled) {
        featureFlagService.setV2Enabled(v2Enabled);

        String message = v2Enabled
                ? "Modern V2 Architecture ACTIVATED. Client traffic is served by Spring Data JDBC N-Tier services."
                : "EMERGENCY ROLLBACK EXECUTED. Client traffic is instantly falling back to Legacy V1 with ZERO downtime.";

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "master_v2_enabled", v2Enabled,
                "active_engine", v2Enabled ? "V2-Modern-Service" : "V1-Legacy-DAO",
                "message", message
        ));
    }

    @PostMapping("/endpoint")
    public ResponseEntity<Map<String, Object>> toggleEndpointV2(
            @RequestParam("name") String endpointName,
            @RequestParam("v2") boolean v2Enabled) {
        featureFlagService.setEndpointV2Enabled(endpointName, v2Enabled);
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "endpoint", endpointName,
                "v2_enabled", v2Enabled,
                "message", "Endpoint '" + endpointName + "' routing updated."
        ));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getRollbackStatus() {
        return ResponseEntity.ok(featureFlagService.getStatus());
    }
}
