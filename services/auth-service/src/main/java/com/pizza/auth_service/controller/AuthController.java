package com.pizza.auth_service.controller;

import com.pizza.auth_service.dto.AuthResponse;
import com.pizza.auth_service.dto.LoginRequest;
import com.pizza.auth_service.dto.RegisterRequest;
import com.pizza.auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 🔹 Register
    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    // 🔹 Login
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}