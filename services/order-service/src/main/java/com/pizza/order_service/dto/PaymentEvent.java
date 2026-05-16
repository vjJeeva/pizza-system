package com.pizza.order_service.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {
    private String orderId; // The Pizza Order ID
    private String transactionId; // Your internal Transaction ID
    private String status; // e.g., "SUCCESSFUL" or "FAILED"
    private String message;
}