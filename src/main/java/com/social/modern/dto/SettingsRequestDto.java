package com.social.modern.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SettingsRequestDto {

    @JsonProperty("user_id")
    private Integer userId;

    @JsonProperty("mute_notifications")
    private String muteNotifications;

    public SettingsRequestDto() {
    }

    public SettingsRequestDto(Integer userId, String muteNotifications) {
        this.userId = userId;
        this.muteNotifications = muteNotifications;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getMuteNotifications() {
        return muteNotifications;
    }

    public void setMuteNotifications(String muteNotifications) {
        this.muteNotifications = muteNotifications;
    }
}
