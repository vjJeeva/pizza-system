package com.pizza.order_service.service;

import com.pizza.order_service.dto.OrderPreparedEvent;
import com.pizza.order_service.dto.OrderRequest;
import com.pizza.order_service.dto.OrderResponse;
import com.pizza.order_service.enums.OrderStatus;
import com.pizza.order_service.mapper.OrderMapper;
import com.pizza.order_service.model.Order;
import com.pizza.order_service.model.OrderItem;
import com.pizza.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final RedisTemplate<String, OrderResponse> redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String IDEMPOTENCY_CACHE_PREFIX = "idemp_order:";
    private static final String PREPARED_TOPIC = "order-prepared-topic";

    @Transactional
    public OrderResponse createOrder(OrderRequest request, String idempotencyKey) {
        String cacheKey = IDEMPOTENCY_CACHE_PREFIX + idempotencyKey;

        // L1 Cache Look-up (Redis)
        OrderResponse cachedResponse = redisTemplate.opsForValue().get(cacheKey);
        if (cachedResponse != null) {
            log.info("Idempotent hit in Redis for key: {}", idempotencyKey);
            return cachedResponse;
        }

        // L2 Consistency Check (Database)
        return orderRepository.findByIdempotencyKey(idempotencyKey)
                .map(order -> {
                    OrderResponse response = orderMapper.toResponse(order);
                    redisTemplate.opsForValue().set(cacheKey, response, Duration.ofHours(24));
                    return response;
                })
                .orElseGet(() -> processNewOrder(request, idempotencyKey, cacheKey));
    }

    private OrderResponse processNewOrder(OrderRequest request, String idempotencyKey, String cacheKey) {
        if(request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        String finalAddress = (request.getManualAddress() != null && !request.getManualAddress().isBlank())
                ? request.getManualAddress()
                : request.getAddressId();

        if (finalAddress == null || finalAddress.isBlank()) {
            throw new IllegalArgumentException("Delivery address is required");
        }

        Order order = Order.builder()
                .orderId("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .userId(request.getUserId())
                .amount(request.getTotalAmount())
                .deliveryAddress(finalAddress)
                .status(OrderStatus.CREATED)
                .idempotencyKey(idempotencyKey)
                .build();

        request.getItems().forEach(itemDto -> {
            OrderItem item = OrderItem.builder()
                    .pizzaName(itemDto.getPizzaName())
                    .quantity(itemDto.getQuantity())
                    .priceAtPurchase(itemDto.getPriceAtPurchase())
                    .order(order)
                    .build();
            order.addItem(item);
        });

        Order savedOrder = orderRepository.save(order);
        OrderResponse response = orderMapper.toResponse(savedOrder);

        redisTemplate.opsForValue().set(cacheKey, response, Duration.ofHours(24));

        return response;
    }

    @Transactional
    public OrderResponse updateOrderStatus(String orderId, OrderStatus newStatus) {
        // Atomic State Guarding
        int updatedRows = orderRepository.updateStatusAtomically(orderId, newStatus);

        if (updatedRows == 0) {
            log.warn("Failed to update order {} to status {}. Possibly invalid state transition.", orderId, newStatus);
            throw new IllegalStateException("Order " + orderId + " cannot be transitioned to " + newStatus);
        }

        Order updatedOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Cache Eviction using the configured RedisTemplate
        redisTemplate.delete(IDEMPOTENCY_CACHE_PREFIX + updatedOrder.getIdempotencyKey());

        // Dispatch payload to Kafka if state shifts to PREPARED
        if (newStatus == OrderStatus.PREPARED) {
            triggerDeliveryDispatch(updatedOrder);
        }

        return orderMapper.toResponse(updatedOrder);
    }

    private void triggerDeliveryDispatch(Order order) {
        OrderPreparedEvent event = OrderPreparedEvent.builder()
                .orderId(order.getOrderId())
                .customerName("Customer-" + order.getUserId().substring(0, 4))
                .deliveryAddress(order.getDeliveryAddress())
                .contactNumber("Stored-Contact")
                .build();
        
        kafkaTemplate.send(PREPARED_TOPIC, order.getOrderId(), event);
        log.info("Kafka event dispatched: Order {} moved to kitchen fulfillment queue.", order.getOrderId());
    }
}