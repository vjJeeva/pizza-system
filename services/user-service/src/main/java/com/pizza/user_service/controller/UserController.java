package com.pizza.user_service.controller;

import com.pizza.user_service.dto.UserProfileUpdateDto;
import com.pizza.user_service.dto.UserRequest;
import com.pizza.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Endpoint: PUT /api/users/profile
     * Used to fill in Name, Phone, and Address.
     */
    @PutMapping("/profile")
    public ResponseEntity<UserRequest> updateProfile(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody UserProfileUpdateDto updateDto) { // Changed UserRequest to UserProfileUpdateDto

        // The service still returns UserRequest (the full profile) for the frontend to use
        return ResponseEntity.ok(userService.updateUserProfile(userId, updateDto));
    }

    /**
     * Endpoint: GET /api/users/me
     * Returns the profile of the currently logged-in user.
     */
    @GetMapping("/me")
    public ResponseEntity<UserRequest> getMyProfile(@RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }
}