package com.pizza.auth_service.dto;

import lombok.*;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegistrationEvent {
    private UUID id;
    private String email;
}