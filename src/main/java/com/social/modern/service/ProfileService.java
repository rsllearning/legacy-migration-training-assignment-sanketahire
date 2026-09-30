package com.social.modern.service;

import com.social.modern.domain.User;
import com.social.modern.dto.ProfileResponseDto;
import com.social.modern.exception.UserNotFoundException;
import com.social.modern.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final UserRepository userRepository;

    public ProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ProfileResponseDto fetchUserProfile(Integer userId, String requesterId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Not found"));

        // Initial AI generation: blindly returns DTO mapped from DB entity,
        // missing privacy check (exposing private profile) and missing synthetic badges
        return new ProfileResponseDto(user.getId(), user.getUsername(), user.getFollowerCount());
    }
}
