package com.pizza.order_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    @NotBlank(message = "userId is required")
    private String userId;

    @NotNull(message = "Items list cannot be empty")
    private List<OrderItemRequest> items;

    @NotNull(message = "Total amount is required")
    private Double totalAmount;

    private String addressId; // For User-Service lookup

    private String manualAddress; // For map pins/custom entry
}