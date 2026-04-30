package com.pizza.user_service.service;

import com.pizza.user_service.dto.UserProfileUpdateDto;
import com.pizza.user_service.dto.UserRequest;
import com.pizza.user_service.entity.User;
import com.pizza.user_service.mapper.UserMapper;
import com.pizza.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Used by the Kafka Listener later to create a placeholder.
     * Only ID and Email are known at this stage.
     */
    @Transactional
    public void createInitialProfile(UUID id, String email) {
        if (userRepository.existsById(id)) {
            log.info("User profile already exists for ID: {}", id);
            return;
        }

        User user = User.builder()
                .id(id)
                .email(email)
                .name("New User") // Placeholder until profile is completed
                .build();

        userRepository.save(user);
        log.info("Initial profile created for user: {}", email);
    }

    /**
     * Used by the Controller when the user submits the "Complete Profile" form.
     */
    @Transactional
    public UserRequest updateUserProfile(UUID userId, UserProfileUpdateDto updateDto) {
        // We find the user strictly by the ID from the GATEWAY (Header)
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // We only update what is allowed
        existingUser.setName(updateDto.getName());
        existingUser.setPhoneNumber(updateDto.getPhoneNumber());
        existingUser.setAddress(updateDto.getAddress());

        User savedUser = userRepository.save(existingUser);

        // We still return the full UserRequest DTO so the frontend
        // gets the updated profile back (including the email and ID)
        return userMapper.toDto(savedUser);
    }

    /**
     * Fetches the current profile details.
     */
    @Transactional(readOnly = true)
    public UserRequest getUserProfile(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User profile not found"));
        return userMapper.toDto(user);
    }
}