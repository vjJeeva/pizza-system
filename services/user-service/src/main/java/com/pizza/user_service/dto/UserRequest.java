package com.pizza.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRequest {
    private UUID id; // Passed from Auth-Service during registration
    @NotBlank(message = "Name cannot be empty")
    private String name;
    @Email(message = "Invalid email")
    private String email;
    private String phoneNumber;
    private String address;
}