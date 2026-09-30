package com.social.modern.service;

import com.social.modern.domain.Post;
import com.social.modern.dto.FeedPostDto;
import com.social.modern.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedService {

    private final PostRepository postRepository;

    public FeedService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<FeedPostDto> getFeed(Integer userId) {
        // Initial AI generation: uses standard findByUserId without soft-delete filter,
        // timestamp ordering, or limit 2
        List<Post> posts = postRepository.findByUserId(userId);
        return posts.stream()
                .map(post -> new FeedPostDto(post.getId(), post.getContent(), post.getCreatedAt()))
                .collect(Collectors.toList());
    }
}
