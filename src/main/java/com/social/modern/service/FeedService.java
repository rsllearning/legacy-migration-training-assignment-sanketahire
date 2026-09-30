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
        List<Post> posts = postRepository.findFeedPostsByUserId(userId);
        return posts.stream()
                .map(post -> new FeedPostDto(post.getId(), post.getContent(), post.getCreatedAt()))
                .collect(Collectors.toList());
    }
}
