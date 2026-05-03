package com.pizza.order_service.dto;


import com.pizza.order_service.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderResponse {

    private String orderId;

    private String userId;

    private  Double amount;

    private OrderStatus status;
}
