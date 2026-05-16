package com.pizza.order_service.repository;

import com.pizza.order_service.enums.OrderStatus;
import com.pizza.order_service.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.orderId = :orderId")
    Optional<Order> findByIdWithItems(String orderId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Order o SET o.status = :newStatus WHERE o.orderId = :orderId " +
            "AND o.status NOT IN (com.pizza.order_service.enums.OrderStatus.CONFIRMED, " +
            "com.pizza.order_service.enums.OrderStatus.FAILED)")
    int updateStatusAtomically(String orderId, OrderStatus newStatus);
}