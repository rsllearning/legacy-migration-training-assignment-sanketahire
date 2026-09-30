package com.social.modern.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FeedPostDto {

    private Integer id;
    private String content;

    @JsonProperty("created_at")
    private Long createdAt;

    public FeedPostDto() {
    }

    public FeedPostDto(Integer id, String content, Long createdAt) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
}
