package com.pizza.delivery_service.service;

import com.pizza.delivery_service.dto.OrderPreparedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryEventListener {

    private final DeliveryService deliveryService;

    @KafkaListener(
            topics = "order-prepared-topic",
            groupId = "delivery-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderPreparedEvent(OrderPreparedEvent event) {
        log.info("Received OrderPreparedEvent from Kafka for Order ID: {}", event.getOrderId());
        try {
            deliveryService.initializeDelivery(event);
        } catch (Exception e) {
            log.error("Failed to process delivery initialization for order {}: {}", event.getOrderId(), e.getMessage());
        }
    }
}