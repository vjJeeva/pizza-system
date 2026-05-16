package com.pizza.delivery_service.controller;

import com.pizza.delivery_service.dto.DeliveryResponse;
import com.pizza.delivery_service.model.Delivery;
import com.pizza.delivery_service.model.Driver;
import com.pizza.delivery_service.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping("/drivers")
    public ResponseEntity<Driver> registerDriver(@RequestBody Driver driver) {
        Driver savedDriver = deliveryService.registerDriver(driver);
        return new ResponseEntity<>(savedDriver, HttpStatus.CREATED);
    }

    @PutMapping("/{deliveryId}/assign")
    public ResponseEntity<DeliveryResponse> assignDriver(
            @PathVariable String deliveryId,
            @RequestParam String driverId) {

        Delivery delivery = deliveryService.assignDriverToDelivery(deliveryId, driverId);
        return ResponseEntity.ok(mapToResponse(delivery));
    }

    @PutMapping("/{deliveryId}/complete")
    public ResponseEntity<DeliveryResponse> completeDelivery(@PathVariable String deliveryId) {
        Delivery delivery = deliveryService.completeDelivery(deliveryId);
        return ResponseEntity.ok(mapToResponse(delivery));
    }


    private DeliveryResponse mapToResponse(Delivery delivery) {
        DeliveryResponse.DeliveryResponseBuilder builder = DeliveryResponse.builder()
                .deliveryId(delivery.getDeliveryId())
                .orderId(delivery.getOrderId())
                .customerName(delivery.getCustomerName())
                .deliveryAddress(delivery.getDeliveryAddress())
                .contactNumber(delivery.getContactNumber())
                .status(delivery.getStatus())
                .updatedAt(delivery.getUpdatedAt());

        if (delivery.getDriver() != null) {
            builder.driverId(delivery.getDriver().getDriverId())
                    .driverName(delivery.getDriver().getName())
                    .vehicleNumber(delivery.getDriver().getVehicleNumber());
        }

        return builder.build();
    }
}