package com.pizza.user_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    private UUID id;

    @Column(nullable = true) // Allow null initially for Kafka sync
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String phoneNumber;
    private String address;
}