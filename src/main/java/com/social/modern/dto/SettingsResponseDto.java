package com.social.modern.dto;

public class SettingsResponseDto {

    private String status;

    public SettingsResponseDto() {
    }

    public SettingsResponseDto(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
