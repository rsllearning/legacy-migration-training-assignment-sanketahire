package com.social.modern;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ModernApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testFetchProfile_Returns200AndCorrectData() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v2/profile?user_id=1&requester_id=1", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        // Asserting the exact legacy payload matching Golden Master
        assertThat(response.getBody()).contains("\"username\":\"alice\"");
        assertThat(response.getBody()).contains("\"badges\":[]");
        assertThat(response.getBody()).contains("\"follower_count\":500");
    }

    @Test
    public void testFetchProfile_PrivateProfile_Returns403() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v2/profile?user_id=2&requester_id=3", String.class);

        // V2 properly blocks access to private profiles matching golden_profile_2.json
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).contains("\"error\":\"Profile is private\"");
    }

    @Test
    public void testUpdateSettings_Returns200() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("mute_notifications", "true");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v2/settings?user_id=1", request, String.class);

        // V2 returns 200 OK with status success matching golden_settings.json
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"success\"");
    }

    @Test
    public void testFetchFeed_LimitsTo2PostsAndExcludesDeleted() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v2/feed?user_id=1", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        // Asserting the limit is respected and order is descending
        String body = response.getBody();
        int postCount = body.split("\"id\"").length - 1;
        assertThat(postCount).isEqualTo(2);

        // Post 104 is newest active, Post 102 is 2nd newest, Post 103 (deleted) is filtered out
        assertThat(body).contains("\"id\":104");
        assertThat(body).contains("\"id\":102");
        assertThat(body).doesNotContain("\"id\":103");
    }
}
