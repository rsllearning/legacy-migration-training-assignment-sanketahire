package com.social.legacy.controller;

import com.social.legacy.dao.LegacyProfileDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ProfileControllerV1 {

    private static final Logger logger = LoggerFactory.getLogger(ProfileControllerV1.class);
    private final LegacyProfileDao profileDao;

    public ProfileControllerV1(LegacyProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestParam("user_id") Integer userId, @RequestParam("requester_id") String requesterId) {
        logger.info("V1 API: Fetching profile for user {} requested by {}", userId, requesterId);
        return profileDao.fetchUserProfile(userId, requesterId);
    }
}
