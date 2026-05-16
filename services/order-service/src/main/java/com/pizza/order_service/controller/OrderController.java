package com.pizza.order_service.controller;

import com.pizza.order_service.dto.OrderRequest;
import com.pizza.order_service.dto.OrderResponse;
import com.pizza.order_service.enums.OrderStatus;
import com.pizza.order_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Creates a new order.
     * Generates a fallback UUID if the X-Idempotency-Key header is missing.
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest request,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {

            // Logic for missing key: ensure Service layer always receives a key.
            String finalKey = (idempotencyKey != null && !idempotencyKey.isBlank())
                    ? idempotencyKey
                    : "AUTO-" + UUID.randomUUID().toString();

            OrderResponse response = orderService.createOrder(request, finalKey);

            // Return the key in the header to confirm the tracking ID to the client.
            return ResponseEntity.status(HttpStatus.CREATED)
                    .header("X-Idempotency-Key", finalKey)
                    .body(response);

    }

    /**
     * Updates order status with Atomic State Guarding.
     */
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable String orderId,
            @RequestParam OrderStatus status) {

        OrderResponse response = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(response);
    }

    /**
     * Local Exception Handler for invalid state transitions.
     * Prevents 500 errors by returning a 400 Bad Request when status logic is violated.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleInvalidState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}