package com.social.legacy.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class SettingsControllerV1 {

    private static final Logger logger = LoggerFactory.getLogger(SettingsControllerV1.class);

    @PostMapping("/settings")
    public ResponseEntity<?> updateSettings(
            @RequestParam("user_id") Integer userId,
            @RequestParam("mute_notifications") String muteParam) {
        logger.info("V1 API: Received settings update request for user {}", userId);
        if ("true".equals(muteParam) || "false".equals(muteParam)) {
            // ... pseudo-logic to save setting for userId ...
            return ResponseEntity.ok(Map.of("status", "success"));
        }
        logger.warn("V1 API: Invalid parameter {}", muteParam);
        return ResponseEntity.badRequest().build();
    }
}
