package com.social.modern.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class ProfileResponseDto {

    private Integer id;
    private String username;

    @JsonProperty("follower_count")
    private Integer followerCount;

    private List<String> badges = new ArrayList<>();

    public ProfileResponseDto() {
    }

    public ProfileResponseDto(Integer id, String username, Integer followerCount) {
        this.id = id;
        this.username = username;
        this.followerCount = followerCount;
        this.badges = new ArrayList<>();
    }

    public ProfileResponseDto(Integer id, String username, Integer followerCount, List<String> badges) {
        this.id = id;
        this.username = username;
        this.followerCount = followerCount;
        this.badges = badges != null ? badges : new ArrayList<>();
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

    public List<String> getBadges() {
        return badges;
    }

    public void setBadges(List<String> badges) {
        this.badges = badges;
    }
}
