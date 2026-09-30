package com.social.modern.service;

import com.social.modern.dto.SettingsResponseDto;
import com.social.modern.exception.InvalidSettingException;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {

    public SettingsResponseDto updateSettings(Integer userId, String muteParam) {
        if ("true".equals(muteParam) || "false".equals(muteParam)) {
            // Business logic to update settings
            return new SettingsResponseDto("success");
        }
        throw new InvalidSettingException("Invalid parameter");
    }
}
