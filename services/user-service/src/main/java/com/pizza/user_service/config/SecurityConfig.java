package com.pizza.user_service.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Disable CSRF as we are using JWT/Stateless communication
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Set session management to stateless (No cookies/sessions)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 3. Authorize requests
                .authorizeHttpRequests(auth -> auth
                        // Allow all requests to the user-service routes
                        // Security is enforced by the API Gateway before forwarding
                        .requestMatchers("/api/users/**").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}