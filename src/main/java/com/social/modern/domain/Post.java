package com.social.modern.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("POSTS")
public class Post {

    @Id
    private Integer id;
    
    @Column("USER_ID")
    private Integer userId;
    
    private String content;
    
    @Column("CREATED_AT")
    private Long createdAt;
    
    @Column("IS_DELETED")
    private Boolean isDeleted;

    public Post() {
    }

    public Post(Integer id, Integer userId, String content, Long createdAt, Boolean isDeleted) {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
        this.isDeleted = isDeleted;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
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

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}
