package com.social.strangler.gateway;

import com.social.legacy.controller.ProfileControllerV1;
import com.social.legacy.controller.SettingsControllerV1;
import com.social.legacy.controller.FeedControllerV1;
import com.social.modern.controller.ProfileControllerV2;
import com.social.modern.controller.SettingsControllerV2;
import com.social.modern.controller.FeedControllerV2;
import com.social.strangler.model.TargetEngine;
import com.social.strangler.service.StranglerRouteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * The Strangler Fig API Gateway entry point.
 * Intercepts external requests to /gateway/api/* and dynamically routes
 * URL-by-URL to either Modern V2 Service or Legacy V1 DAO based on the route table.
 */
@RestController
@RequestMapping("/gateway/api")
public class StranglerGatewayController {

    private static final Logger logger = LoggerFactory.getLogger(StranglerGatewayController.class);

    private final StranglerRouteService routeService;
    private final ProfileControllerV1 profileV1;
    private final ProfileControllerV2 profileV2;
    private final SettingsControllerV1 settingsV1;
    private final SettingsControllerV2 settingsV2;
    private final FeedControllerV1 feedV1;
    private final FeedControllerV2 feedV2;

    public StranglerGatewayController(StranglerRouteService routeService,
                                      ProfileControllerV1 profileV1,
                                      ProfileControllerV2 profileV2,
                                      SettingsControllerV1 settingsV1,
                                      SettingsControllerV2 settingsV2,
                                      FeedControllerV1 feedV1,
                                      FeedControllerV2 feedV2) {
        this.routeService = routeService;
        this.profileV1 = profileV1;
        this.profileV2 = profileV2;
        this.settingsV1 = settingsV1;
        this.settingsV2 = settingsV2;
        this.feedV1 = feedV1;
        this.feedV2 = feedV2;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestParam("user_id") Integer userId,
                                        @RequestParam("requester_id") String requesterId) {
        TargetEngine target = routeService.getTarget("/api/profile");
        logger.info("[STRANGLER-ROUTER] /gateway/api/profile -> Routing to {}", target.getEngineName());

        HttpHeaders headers = createHeaders(target);
        if (target == TargetEngine.MODERN_V2) {
            Object result = profileV2.getProfile(userId, requesterId);
            return ResponseEntity.ok().headers(headers).body(result);
        } else {
            ResponseEntity<?> legacyResp = profileV1.getProfile(userId, requesterId);
            return ResponseEntity.status(legacyResp.getStatusCode()).headers(headers).body(legacyResp.getBody());
        }
    }

    @PostMapping("/settings")
    public ResponseEntity<?> updateSettings(@RequestParam("user_id") Integer userId,
                                            @RequestParam("mute_notifications") String muteParam) {
        TargetEngine target = routeService.getTarget("/api/settings");
        logger.info("[STRANGLER-ROUTER] /gateway/api/settings -> Routing to {}", target.getEngineName());

        HttpHeaders headers = createHeaders(target);
        if (target == TargetEngine.MODERN_V2) {
            Object result = settingsV2.updateSettings(userId, muteParam);
            return ResponseEntity.ok().headers(headers).body(result);
        } else {
            ResponseEntity<?> legacyResp = settingsV1.updateSettings(userId, muteParam);
            return ResponseEntity.status(legacyResp.getStatusCode()).headers(headers).body(legacyResp.getBody());
        }
    }

    @GetMapping("/feed")
    public ResponseEntity<?> getFeed(@RequestParam("user_id") Integer userId) {
        TargetEngine target = routeService.getTarget("/api/feed");
        logger.info("[STRANGLER-ROUTER] /gateway/api/feed -> Routing to {}", target.getEngineName());

        HttpHeaders headers = createHeaders(target);
        if (target == TargetEngine.MODERN_V2) {
            Object result = feedV2.getFeed(userId);
            return ResponseEntity.ok().headers(headers).body(result);
        } else {
            Object result = feedV1.getFeed(userId);
            return ResponseEntity.ok().headers(headers).body(result);
        }
    }

    private HttpHeaders createHeaders(TargetEngine target) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Strangler-Target", target.getEngineName());
        headers.set("X-Strangler-Status", target.getStatus());
        return headers;
    }
}
