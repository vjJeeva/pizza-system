package com.pizza.auth_service.entity;


import com.pizza.auth_service.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "auth_users")
public class AuthUser {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(unique = true, nullable = false)
    private  String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
