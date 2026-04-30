package com.pizza.auth_service.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {

    private String token;
    private String userId;
}
