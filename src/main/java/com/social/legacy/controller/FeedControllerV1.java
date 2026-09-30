package com.social.legacy.controller;

import com.social.legacy.dao.LegacyFeedDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class FeedControllerV1 {

    private static final Logger logger = LoggerFactory.getLogger(FeedControllerV1.class);
    private final LegacyFeedDao feedDao;

    public FeedControllerV1(LegacyFeedDao feedDao) {
        this.feedDao = feedDao;
    }

    @GetMapping("/feed")
    public List<Map<String, Object>> getFeed(@RequestParam("user_id") Integer userId) {
        logger.info("V1 API: Loading feed for user {}", userId);
        return feedDao.getFeed(userId);
    }
}
