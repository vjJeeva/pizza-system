package com.pizza.auth_service.service;

import com.pizza.auth_service.dto.AuthResponse;
import com.pizza.auth_service.dto.LoginRequest;
import com.pizza.auth_service.dto.RegisterRequest;
import com.pizza.auth_service.entity.AuthUser;
import com.pizza.auth_service.enums.Role;
import com.pizza.auth_service.enums.UserStatus;
import com.pizza.auth_service.event.AuthProducer;
import com.pizza.auth_service.mapper.AuthMapper;
import com.pizza.auth_service.repository.AuthUserRepository;
import com.pizza.auth_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pizza.auth_service.dto.UserRegistrationEvent;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthUserRepository repository;
    private final AuthMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthProducer authProducer;

    /**
     * Registers a new user.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        // 1. Check if email already exists
        repository.findByEmail(request.getEmail())
                .ifPresent(user -> {
                    throw new RuntimeException("Email already exists: " + request.getEmail());
                });

        // 2. Map DTO to Entity
        AuthUser user = mapper.toEntity(request);

        // 3. Enrich entity with business logic
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setStatus(UserStatus.ACTIVE);

        // 4. Save to DB and capture the returned object with the generated UUID
        AuthUser savedUser = repository.save(user);

        //-----------------------------------------------------------

        //NEW KAFKA LOGIC
        // fire the event so user-service can create a profile
        UserRegistrationEvent event= UserRegistrationEvent.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .build();

        authProducer.sendRegistrationEvent(event);

        //-----------------------------------------------------------

        // 5. Generate token using the persistent entity data
        String token = jwtService.generateToken(savedUser.getId(), savedUser.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(savedUser.getId().toString())
                .build();
    }


     // Authenticates a user and returns a JWT.

    public AuthResponse login(LoginRequest request) {

        AuthUser user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("Account is not active. Please contact support.");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId().toString())
                .build();
    }
}