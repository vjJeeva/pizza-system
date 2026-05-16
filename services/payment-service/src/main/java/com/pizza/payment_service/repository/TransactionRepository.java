package com.pizza.payment_service.repository;

import com.pizza.payment_service.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {


    Optional<Transaction> findByRazorpayOrderId(String razorpayOrderId);


    List<Transaction> findByUserId(UUID userId);

    Optional<Transaction> findByRazorpayPaymentId(String razorpayPaymentId);
}