package com.social.modern.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProfileResponseDto {

    private Integer id;
    private String username;

    @JsonProperty("follower_count")
    private Integer followerCount;

    public ProfileResponseDto() {
    }

    public ProfileResponseDto(Integer id, String username, Integer followerCount) {
        this.id = id;
        this.username = username;
        this.followerCount = followerCount;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(Integer followerCount) {
        this.followerCount = followerCount;
    }
}
