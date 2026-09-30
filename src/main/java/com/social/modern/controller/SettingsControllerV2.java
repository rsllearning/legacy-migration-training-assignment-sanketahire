package com.social.modern.controller;

import com.social.modern.dto.SettingsResponseDto;
import com.social.modern.service.SettingsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2")
public class SettingsControllerV2 {

    private final SettingsService settingsService;

    public SettingsControllerV2(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @PostMapping("/settings")
    public SettingsResponseDto updateSettings(
            @RequestParam("user_id") Integer userId,
            @RequestParam("mute_notifications") String muteParam) {
        return settingsService.updateSettings(userId, muteParam);
    }
}
