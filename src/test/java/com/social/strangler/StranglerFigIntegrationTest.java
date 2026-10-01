package com.social.strangler;

import com.social.strangler.model.TargetEngine;
import com.social.strangler.service.StranglerRouteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StranglerFigIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StranglerRouteService routeService;

    @BeforeEach
    void setUp() {
        // Reset to initial baseline state: Profile & Settings -> Modern V2, Feed -> Legacy V1
        routeService.resetToDefault();
    }

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void testInitialStranglerState_PartialMigrationVerified() {
        // 1. Profile is Strangled (Routed to Modern V2)
        ResponseEntity<String> profileResp = restTemplate.getForEntity(
                getBaseUrl() + "/gateway/api/profile?user_id=1&requester_id=1", String.class);
        assertThat(profileResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(profileResp.getHeaders().getFirst("X-Strangler-Target")).isEqualTo("Modern-V2-Service");
        assertThat(profileResp.getHeaders().getFirst("X-Strangler-Status")).isEqualTo("Strangled");
        assertThat(profileResp.getBody()).contains("\"follower_count\":500");

        // 2. Settings is Strangled (Routed to Modern V2)
        ResponseEntity<String> settingsResp = restTemplate.postForEntity(
                getBaseUrl() + "/gateway/api/settings?user_id=1&mute_notifications=true", null, String.class);
        assertThat(settingsResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(settingsResp.getHeaders().getFirst("X-Strangler-Target")).isEqualTo("Modern-V2-Service");
        assertThat(settingsResp.getHeaders().getFirst("X-Strangler-Status")).isEqualTo("Strangled");

        // 3. Feed is unmigrated (Routed to Legacy V1 DAO)
        ResponseEntity<String> feedResp = restTemplate.getForEntity(
                getBaseUrl() + "/gateway/api/feed?user_id=1", String.class);
        assertThat(feedResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(feedResp.getHeaders().getFirst("X-Strangler-Target")).isEqualTo("Legacy-V1-DAO");
        assertThat(feedResp.getHeaders().getFirst("X-Strangler-Status")).isEqualTo("Legacy-Unmigrated");
    }

    @Test
    void testLiveStranglingMigration_FeedMigratedToModernV2() {
        // Feed initially on V1
        ResponseEntity<String> initialFeed = restTemplate.getForEntity(
                getBaseUrl() + "/gateway/api/feed?user_id=1", String.class);
        assertThat(initialFeed.getHeaders().getFirst("X-Strangler-Target")).isEqualTo("Legacy-V1-DAO");

        // Dynamically Strangle /api/feed via Gateway Admin Control Plane
        ResponseEntity<Map> adminResp = restTemplate.postForEntity(
                getBaseUrl() + "/gateway/admin/route?path=/api/feed&target=V2", null, Map.class);
        assertThat(adminResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(adminResp.getBody().get("status")).isEqualTo("SUCCESS");
        assertThat(adminResp.getBody().get("new_target")).isEqualTo("Modern-V2-Service");

        // Subsequent call to /gateway/api/feed instantly serves Modern V2 with zero restart
        ResponseEntity<String> migratedFeed = restTemplate.getForEntity(
                getBaseUrl() + "/gateway/api/feed?user_id=1", String.class);
        assertThat(migratedFeed.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(migratedFeed.getHeaders().getFirst("X-Strangler-Target")).isEqualTo("Modern-V2-Service");
        assertThat(migratedFeed.getHeaders().getFirst("X-Strangler-Status")).isEqualTo("Strangled");
    }

    @Test
    void testEmergencyRollback_PerRouteReversion() {
        // Initially Profile is Modern V2
        ResponseEntity<String> initialProfile = restTemplate.getForEntity(
                getBaseUrl() + "/gateway/api/profile?user_id=1&requester_id=1", String.class);
        assertThat(initialProfile.getHeaders().getFirst("X-Strangler-Target")).isEqualTo("Modern-V2-Service");

        // Execute Emergency Rollback on /api/profile via Admin API
        ResponseEntity<Map> rollbackResp = restTemplate.postForEntity(
                getBaseUrl() + "/gateway/admin/rollback?path=/api/profile", null, Map.class);
        assertThat(rollbackResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rollbackResp.getBody().get("status")).isEqualTo("ROLLBACK_EXECUTED");

        // Subsequent call immediately falls back to Legacy V1 DAO
        ResponseEntity<String> postRollbackProfile = restTemplate.getForEntity(
                getBaseUrl() + "/gateway/api/profile?user_id=1&requester_id=1", String.class);
        assertThat(postRollbackProfile.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(postRollbackProfile.getHeaders().getFirst("X-Strangler-Target")).isEqualTo("Legacy-V1-DAO");
        assertThat(postRollbackProfile.getHeaders().getFirst("X-Strangler-Status")).isEqualTo("Legacy-Unmigrated");
    }

    @Test
    void testGlobalEmergencyRollback_RevertsAllRoutesToLegacy() {
        // Trigger global emergency rollback
        ResponseEntity<Map> rollbackResp = restTemplate.postForEntity(
                getBaseUrl() + "/gateway/admin/rollback", null, Map.class);
        assertThat(rollbackResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rollbackResp.getBody().get("target_path")).isEqualTo("ALL_ROUTES");

        // Verify all endpoints are now routed to Legacy V1
        ResponseEntity<String> profileResp = restTemplate.getForEntity(
                getBaseUrl() + "/gateway/api/profile?user_id=1&requester_id=1", String.class);
        assertThat(profileResp.getHeaders().getFirst("X-Strangler-Target")).isEqualTo("Legacy-V1-DAO");

        ResponseEntity<String> settingsResp = restTemplate.postForEntity(
                getBaseUrl() + "/gateway/api/settings?user_id=1&mute_notifications=true", null, String.class);
        assertThat(settingsResp.getHeaders().getFirst("X-Strangler-Target")).isEqualTo("Legacy-V1-DAO");

        ResponseEntity<String> feedResp = restTemplate.getForEntity(
                getBaseUrl() + "/gateway/api/feed?user_id=1", String.class);
        assertThat(feedResp.getHeaders().getFirst("X-Strangler-Target")).isEqualTo("Legacy-V1-DAO");
    }

    @Test
    void testAdminRoutesQueryEndpoint() {
        ResponseEntity<Map> routesResp = restTemplate.getForEntity(
                getBaseUrl() + "/gateway/admin/routes", Map.class);
        assertThat(routesResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(routesResp.getBody().get("gateway_status")).isEqualTo("ACTIVE");
        assertThat(routesResp.getBody().get("routes")).isNotNull();
    }
}
