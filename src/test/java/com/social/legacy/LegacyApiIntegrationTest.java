package com.social.legacy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LegacyApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testFetchProfile_Returns200AndCorrectData() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/profile?user_id=1&requester_id=1", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        // Asserting the exact legacy payload
        assertThat(response.getBody()).contains("\"username\":\"alice\"");
        assertThat(response.getBody()).contains("\"badges\":[]");
        assertThat(response.getBody()).contains("\"follower_count\":500");
    }

    @Test
    public void testFetchProfile_PrivateProfile_Returns403() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/profile?user_id=2&requester_id=3", String.class);

        // Legacy properly blocks access to private profiles
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void testUpdateSettings_Returns200() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("mute_notifications", "true");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/settings?user_id=1", request, String.class);

        // Legacy returns 200 OK
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testFetchFeed_LimitsTo2Posts() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/feed?user_id=1", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        // Asserting the limit is respected (we only expect 2 posts in the JSON array)
        String body = response.getBody();
        int postCount = body.split("\"id\"").length - 1;
        assertThat(postCount).isEqualTo(2);
    }
}
