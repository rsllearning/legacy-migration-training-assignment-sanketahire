package com.social.rollback;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RollbackIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private FeatureFlagService featureFlagService;

    @BeforeEach
    public void setup() {
        // Reset to default active V2 state before each test
        featureFlagService.setV2Enabled(true);
    }

    @Test
    public void testProfileEndpoint_InstantRollback_FromV2ToV1_WithZeroDowntime() {
        // 1. Initial State: Modern V2 is ACTIVE
        ResponseEntity<String> v2Response = restTemplate.getForEntity(
                "/api/profile?user_id=1&requester_id=1", String.class);

        assertThat(v2Response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(v2Response.getHeaders().getFirst("X-Served-By")).isEqualTo("V2-Modern-Service");
        assertThat(v2Response.getBody()).contains("\"username\":\"alice\"");
        assertThat(v2Response.getBody()).contains("\"badges\":[]");

        // 2. SIMULATE INCIDENT: Flip switch to execute instant rollback
        featureFlagService.setV2Enabled(false);

        // 3. Immediately send the next request: Served by Legacy V1 seamlessly!
        ResponseEntity<String> v1Response = restTemplate.getForEntity(
                "/api/profile?user_id=1&requester_id=1", String.class);

        assertThat(v1Response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(v1Response.getHeaders().getFirst("X-Served-By")).isEqualTo("V1-Legacy-DAO");
        assertThat(v1Response.getBody()).contains("\"username\":\"alice\"");
        assertThat(v1Response.getBody()).contains("\"badges\":[]");

        // 4. Verify privacy gate still holds under rolled-back V1
        ResponseEntity<String> privateResponse = restTemplate.getForEntity(
                "/api/profile?user_id=2&requester_id=3", String.class);
        assertThat(privateResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(privateResponse.getHeaders().getFirst("X-Served-By")).isEqualTo("V1-Legacy-DAO");
    }

    @Test
    public void testSettingsEndpoint_InstantRollback() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("mute_notifications", "true");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        // Step 1: V2 Active
        ResponseEntity<String> v2Resp = restTemplate.postForEntity(
                "/api/settings?user_id=1", request, String.class);
        assertThat(v2Resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(v2Resp.getHeaders().getFirst("X-Served-By")).isEqualTo("V2-Modern-Service");

        // Step 2: Rollback to V1
        featureFlagService.setV2Enabled(false);

        // Step 3: V1 Serves successfully
        ResponseEntity<String> v1Resp = restTemplate.postForEntity(
                "/api/settings?user_id=1", request, String.class);
        assertThat(v1Resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(v1Resp.getHeaders().getFirst("X-Served-By")).isEqualTo("V1-Legacy-DAO");
    }

    @Test
    public void testFeedEndpoint_InstantRollback() {
        // Step 1: V2 Active
        ResponseEntity<String> v2Resp = restTemplate.getForEntity("/api/feed?user_id=1", String.class);
        assertThat(v2Resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(v2Resp.getHeaders().getFirst("X-Served-By")).isEqualTo("V2-Modern-Service");

        // Step 2: Rollback to V1
        featureFlagService.setV2Enabled(false);

        // Step 3: V1 Serves successfully
        ResponseEntity<String> v1Resp = restTemplate.getForEntity("/api/feed?user_id=1", String.class);
        assertThat(v1Resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(v1Resp.getHeaders().getFirst("X-Served-By")).isEqualTo("V1-Legacy-DAO");
    }

    @Test
    public void testAdminRollbackEndpoint_RuntimeHttpInvocation() {
        // Call admin toggle API via HTTP
        ResponseEntity<String> toggleResp = restTemplate.postForEntity(
                "/api/admin/rollback?v2=false", null, String.class);

        assertThat(toggleResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(toggleResp.getBody()).contains("\"status\":\"SUCCESS\"");
        assertThat(toggleResp.getBody()).contains("\"active_engine\":\"V1-Legacy-DAO\"");

        // Confirm routing immediately switched to V1
        ResponseEntity<String> profileResp = restTemplate.getForEntity(
                "/api/profile?user_id=1&requester_id=1", String.class);
        assertThat(profileResp.getHeaders().getFirst("X-Served-By")).isEqualTo("V1-Legacy-DAO");
    }
}
