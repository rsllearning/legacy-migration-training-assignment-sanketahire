package com.social.legacy.dao;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Repository
public class LegacyProfileDao {

    private final JdbcTemplate jdbcTemplate;

    public LegacyProfileDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Returning ResponseEntity from a DAO is terrible practice, exactly what we expect from "messy" V1 code
    public ResponseEntity<?> fetchUserProfile(Integer userId, String requesterId) {
        return jdbcTemplate.query("SELECT * FROM users WHERE id = ?", rs -> {
            if (rs.next()) {
                boolean isPrivate = rs.getBoolean("is_private");
                
                if (isPrivate && !userId.toString().equals(requesterId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Profile is private"));
                }
                
                Map<String, Object> response = new HashMap<>();
                response.put("id", userId);
                response.put("username", rs.getString("username"));
                response.put("follower_count", rs.getInt("follower_count"));
                response.put("badges", new ArrayList<>()); 
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Not found"));
        }, userId);
    }
}
