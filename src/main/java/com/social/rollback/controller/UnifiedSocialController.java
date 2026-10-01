package com.social.rollback.controller;

import com.social.legacy.controller.SettingsControllerV1;
import com.social.legacy.dao.LegacyFeedDao;
import com.social.legacy.dao.LegacyProfileDao;
import com.social.modern.dto.FeedPostDto;
import com.social.modern.dto.ProfileResponseDto;
import com.social.modern.dto.SettingsResponseDto;
import com.social.modern.service.FeedService;
import com.social.modern.service.ProfileService;
import com.social.modern.service.SettingsService;
import com.social.rollback.FeatureFlagService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Unified Routing Seam acting as the Branch-by-Abstraction facade.
 * Dynamically routes requests between Modern V2 Services and Legacy V1 DAOs
 * based on the runtime state of FeatureFlagService.
 */
@RestController
@RequestMapping("/api")
public class UnifiedSocialController {

    private final FeatureFlagService featureFlagService;
    private final ProfileService profileServiceV2;
    private final SettingsService settingsServiceV2;
    private final FeedService feedServiceV2;
    private final LegacyProfileDao legacyProfileDaoV1;
    private final SettingsControllerV1 settingsControllerV1;
    private final LegacyFeedDao legacyFeedDaoV1;

    public UnifiedSocialController(FeatureFlagService featureFlagService,
                                   ProfileService profileServiceV2,
                                   SettingsService settingsServiceV2,
                                   FeedService feedServiceV2,
                                   LegacyProfileDao legacyProfileDaoV1,
                                   SettingsControllerV1 settingsControllerV1,
                                   LegacyFeedDao legacyFeedDaoV1) {
        this.featureFlagService = featureFlagService;
        this.profileServiceV2 = profileServiceV2;
        this.settingsServiceV2 = settingsServiceV2;
        this.feedServiceV2 = feedServiceV2;
        this.legacyProfileDaoV1 = legacyProfileDaoV1;
        this.settingsControllerV1 = settingsControllerV1;
        this.legacyFeedDaoV1 = legacyFeedDaoV1;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestParam("user_id") Integer userId,
                                         @RequestParam("requester_id") String requesterId) {
        if (featureFlagService.isV2EnabledForEndpoint("profile")) {
            ProfileResponseDto v2Response = profileServiceV2.fetchUserProfile(userId, requesterId);
            return ResponseEntity.ok()
                    .header("X-Served-By", "V2-Modern-Service")
                    .body(v2Response);
        } else {
            ResponseEntity<?> v1Response = legacyProfileDaoV1.fetchUserProfile(userId, requesterId);
            return ResponseEntity.status(v1Response.getStatusCode())
                    .header("X-Served-By", "V1-Legacy-DAO")
                    .body(v1Response.getBody());
        }
    }

    @PostMapping("/settings")
    public ResponseEntity<?> updateSettings(@RequestParam("user_id") Integer userId,
                                            @RequestParam("mute_notifications") String muteParam) {
        if (featureFlagService.isV2EnabledForEndpoint("settings")) {
            SettingsResponseDto v2Response = settingsServiceV2.updateSettings(userId, muteParam);
            return ResponseEntity.ok()
                    .header("X-Served-By", "V2-Modern-Service")
                    .body(v2Response);
        } else {
            ResponseEntity<?> v1Response = settingsControllerV1.updateSettings(userId, muteParam);
            return ResponseEntity.status(v1Response.getStatusCode())
                    .header("X-Served-By", "V1-Legacy-DAO")
                    .body(v1Response.getBody());
        }
    }

    @GetMapping("/feed")
    public ResponseEntity<?> getFeed(@RequestParam("user_id") Integer userId) {
        if (featureFlagService.isV2EnabledForEndpoint("feed")) {
            List<FeedPostDto> v2Feed = feedServiceV2.getFeed(userId);
            return ResponseEntity.ok()
                    .header("X-Served-By", "V2-Modern-Service")
                    .body(v2Feed);
        } else {
            List<Map<String, Object>> v1Feed = legacyFeedDaoV1.getFeed(userId);
            return ResponseEntity.ok()
                    .header("X-Served-By", "V1-Legacy-DAO")
                    .body(v1Feed);
        }
    }
}
