package com.pizza.delivery_service.service;

import com.pizza.delivery_service.dto.OrderPreparedEvent;
import com.pizza.delivery_service.enums.DeliveryStatus;
import com.pizza.delivery_service.model.Delivery;
import com.pizza.delivery_service.model.Driver;
import com.pizza.delivery_service.repository.DeliveryRepository;
import com.pizza.delivery_service.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final DriverRepository driverRepository;

    @Transactional
    public void initializeDelivery(OrderPreparedEvent event) {
        deliveryRepository.findByOrderId(event.getOrderId()).ifPresentOrElse(
                existing -> log.warn("Delivery job already initialized for Order ID: {}", event.getOrderId()),
                () -> {
                    Delivery delivery = Delivery.builder()
                            .deliveryId("DEL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                            .orderId(event.getOrderId())
                            .customerName(event.getCustomerName())
                            .deliveryAddress(event.getDeliveryAddress())
                            .contactNumber(event.getContactNumber())
                            .status(DeliveryStatus.ASSIGNING_DRIVER)
                            .build();

                    deliveryRepository.save(delivery);
                    log.info("Initialized Delivery tracking record {} for Order {}", delivery.getDeliveryId(), event.getOrderId());
                }
        );
    }

    @Transactional
    public Driver registerDriver(Driver driver) {
        if (driver.getDriverId() == null || driver.getDriverId().isBlank()) {
            driver.setDriverId("DRV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        return driverRepository.save(driver);
    }

    @Transactional
    public Delivery assignDriverToDelivery(String deliveryId, String driverId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery record not found: " + deliveryId));

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found: " + driverId));

        if (!driver.isAvailable()) {
            throw new IllegalStateException("Driver " + driverId + " is currently busy on another route.");
        }

        driver.setAvailable(false);
        driverRepository.save(driver);

        delivery.setDriver(driver);
        delivery.setStatus(DeliveryStatus.OUT_FOR_DELIVERY);

        log.info("Driver {} assigned to delivery {}. Package is OUT_FOR_DELIVERY.", driverId, deliveryId);
        return deliveryRepository.save(delivery);
    }

    @Transactional
    public Delivery completeDelivery(String deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery record not found: " + deliveryId));

        delivery.setStatus(DeliveryStatus.DELIVERED);

        if (delivery.getDriver() != null) {
            Driver driver = delivery.getDriver();
            driver.setAvailable(true);
            driverRepository.save(driver);
        }

        log.info("Delivery {} successfully completed.", deliveryId);
        return deliveryRepository.save(delivery);
    }
}