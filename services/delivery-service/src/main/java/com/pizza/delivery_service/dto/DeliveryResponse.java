package com.pizza.delivery_service.dto;

import com.pizza.delivery_service.enums.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryResponse {
    private String deliveryId;
    private String orderId;
    private String customerName;
    private String deliveryAddress;
    private String contactNumber;
    private DeliveryStatus status;
    private String driverId;
    private String driverName;
    private String vehicleNumber;
    private LocalDateTime updatedAt;
}