package com.social.modern.controller;

import com.social.modern.dto.FeedPostDto;
import com.social.modern.service.FeedService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v2")
public class FeedControllerV2 {

    private final FeedService feedService;

    public FeedControllerV2(FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping("/feed")
    public List<FeedPostDto> getFeed(@RequestParam("user_id") Integer userId) {
        return feedService.getFeed(userId);
    }
}
