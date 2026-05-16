package com.pizza.auth_service.security;

import com.pizza.auth_service.entity.AuthUser;
import com.pizza.auth_service.repository.AuthUserRepository;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AuthUserRepository repository;

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // 🔹 No token → continue down the filter chain
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);
        String email;

        try {
            email = jwtService.extractEmail(token);
        } catch (Exception e) {
            log.error("Failed to extract email from JWT token: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // 🔹 If email is valid and context is not already authenticated
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            AuthUser user = repository.findByEmail(email).orElse(null);

            if (user != null && jwtService.isTokenValid(token, user.getEmail())) {

                // Keeping your original functionality intact: passing 'user' object as principal
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                Collections.emptyList()
                        );

                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("Successfully authenticated user: {}", email);
            }
        }

        filterChain.doFilter(request, response);
    }


    public static AuthUser getAuthenticatedUser() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return null;
        }

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof AuthUser) {
            return (AuthUser) principal;
        }

        return null;
    }
}