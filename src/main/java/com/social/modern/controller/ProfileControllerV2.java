package com.social.modern.controller;

import com.social.modern.dto.ProfileResponseDto;
import com.social.modern.service.ProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2")
public class ProfileControllerV2 {

    private final ProfileService profileService;

    public ProfileControllerV2(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/profile")
    public ProfileResponseDto getProfile(@RequestParam("user_id") Integer userId,
                                         @RequestParam("requester_id") String requesterId) {
        return profileService.fetchUserProfile(userId, requesterId);
    }
}
