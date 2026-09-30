package com.social.modern.service;

import com.social.modern.domain.User;
import com.social.modern.dto.ProfileResponseDto;
import com.social.modern.exception.PrivateProfileException;
import com.social.modern.exception.UserNotFoundException;
import com.social.modern.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class ProfileService {

    private final UserRepository userRepository;

    public ProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ProfileResponseDto fetchUserProfile(Integer userId, String requesterId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Not found"));

        if (Boolean.TRUE.equals(user.getIsPrivate()) && !userId.toString().equals(requesterId)) {
            throw new PrivateProfileException("Profile is private");
        }

        return new ProfileResponseDto(user.getId(), user.getUsername(), user.getFollowerCount(), new ArrayList<>());
    }
}
