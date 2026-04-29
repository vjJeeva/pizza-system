package com.pizza.auth_service.service;


import com.pizza.auth_service.dto.AuthResponse;
import com.pizza.auth_service.dto.LoginRequest;
import com.pizza.auth_service.dto.RegisterRequest;
import com.pizza.auth_service.entity.AuthUser;
import com.pizza.auth_service.enums.Role;
import com.pizza.auth_service.enums.UserStatus;
import com.pizza.auth_service.mapper.AuthMapper;
import com.pizza.auth_service.repository.AuthUserRepository;
import com.pizza.auth_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthUserRepository repository;
    private final AuthMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // to Register

    public AuthResponse register(RegisterRequest request){

        //to Check if email already exists
        repository.findByEmail(request.getEmail())
                .ifPresent(user -> {
                    throw new RuntimeException("Email already exists");
                });
        AuthUser user=mapper.toEntity(request);

        //enrich entity (business logic)
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setStatus(UserStatus.ACTIVE);

        repository.save(user);
        // after saving entity generating token

        String token = jwtService.generateToken(user.getId(), user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId().toString())
                .build();

    }

    //Login
    public AuthResponse login(LoginRequest request){
        AuthUser user=repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        //verify password
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        //check status
        if(user.getStatus() != UserStatus.ACTIVE){
            throw new RuntimeException("User is not active");
        }

        String token= jwtService.generateToken(user.getId(), user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId().toString())
                .build();
    }
}


