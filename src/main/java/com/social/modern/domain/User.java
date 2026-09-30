package com.social.modern.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("USERS")
public class User {

    @Id
    private Integer id;
    private String username;
    
    @Column("FOLLOWER_COUNT")
    private Integer followerCount;
    
    @Column("IS_PRIVATE")
    private Boolean isPrivate;

    public User() {
    }

    public User(Integer id, String username, Integer followerCount, Boolean isPrivate) {
        this.id = id;
        this.username = username;
        this.followerCount = followerCount;
        this.isPrivate = isPrivate;
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

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }
}
