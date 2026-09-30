package com.social.legacy.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class LegacyFeedDao {

    private final JdbcTemplate jdbcTemplate;

    public LegacyFeedDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> getFeed(Integer userId) {
        return jdbcTemplate.query(
            "SELECT id, content, created_at FROM posts WHERE user_id = ? AND is_deleted = false ORDER BY created_at DESC LIMIT 2",
            (rs, rowNum) -> Map.of(
                "id", rs.getInt("id"),
                "content", rs.getString("content"),
                "created_at", rs.getLong("created_at")
            ),
            userId
        );
    }
}
