package com.pizza.order_service.model;

import com.pizza.order_service.enums.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @NotNull(message = "userId can't be null")
    private String userId;

    @NotNull(message = "amount is required")
    private Double amount;

    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress; // Added for historical tracking

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(unique = true, nullable = false)
    private String idempotencyKey;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    //to maintain bidirectional consistency
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
}