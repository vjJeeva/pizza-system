package com.pizza.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderPreparedEvent {
    private String orderId;
    private String customerName;
    private String deliveryAddress;
    private String contactNumber;
}