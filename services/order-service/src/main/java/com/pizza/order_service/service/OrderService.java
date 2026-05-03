package com.pizza.order_service.service;

import com.pizza.order_service.dto.OrderRequest;
import com.pizza.order_service.dto.OrderResponse;
import com.pizza.order_service.enums.OrderStatus;
import com.pizza.order_service.mapper.OrderMapper;
import com.pizza.order_service.model.Order;
import com.pizza.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final OrderMapper orderMapper;

    //1. logic to initialize a secure, unique order
    public OrderResponse createOrder(OrderRequest request){
        //Business Validation
        if(request.getAmount()== null || request.getAmount() <= 0){
            throw new IllegalArgumentException("Invalid Order amount");
        }
        if(request.getUserId()== null || request.getUserId().isBlank()){
            throw new IllegalArgumentException(("User ID is required"));
        }
        //CONVERT DTO TO ENTITY
        Order order=orderMapper.toEntity(request);

        //SET INTERNAL FIELDS MANUALLY
        order.setOrderId("ORD-" + UUID.randomUUID().toString().substring(0,8).toUpperCase());
        order.setStatus(OrderStatus.CREATED);

        //SAVE TO PIZZA_ORDER_DB

        Order savedOrder=orderRepository.save(order);

        //CONVERT ENTITY BACK TO RESPONSE DTO

        return orderMapper.toResponse(savedOrder);

    }
}
