package com.pizza.order_service.model;


import com.pizza.order_service.enums.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    private String orderId;

    @NotNull(message = "userId can't be null ")
    private String userId;

    @NotBlank
    private Double amount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;
}
