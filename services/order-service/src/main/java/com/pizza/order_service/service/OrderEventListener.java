package com.pizza.order_service.service;

import com.pizza.order_service.dto.PaymentEvent;
import com.pizza.order_service.enums.OrderStatus;
import com.pizza.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final OrderRepository orderRepository;

    @KafkaListener(topics = "payment-topic", groupId = "order-group")
    public void handlePaymentEvent(PaymentEvent event) {
        log.info("Received Payment Event for Order ID: {}", event.getOrderId());

        if ("SUCCESSFUL".equals(event.getStatus())) {

            orderRepository.findById(event.getOrderId()).ifPresent(order -> {
                order.setStatus(OrderStatus.PAID);
                orderRepository.save(order);
                log.info("Order {} status updated to PAID", event.getOrderId());
            });
        }
    }
}