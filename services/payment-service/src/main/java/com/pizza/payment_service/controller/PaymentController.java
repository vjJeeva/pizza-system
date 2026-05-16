package com.pizza.payment_service.controller;

import com.pizza.payment_service.dto.PaymentVerificationRequest;
import com.pizza.payment_service.service.PaymentService;
import com.pizza.payment_service.model.Transaction;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    public ResponseEntity<Transaction> create(@RequestParam Double amount,
                                              @RequestHeader("X-User-Id") UUID userId) throws RazorpayException {
        return ResponseEntity.ok(paymentService.createOrder(amount, userId));
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verify(@RequestBody PaymentVerificationRequest request) {
        paymentService.verifyAndNotify(request);
        return ResponseEntity.ok("Payment Verified and Order Updated");
    }
}