package com.pizza.payment_service.service;

import com.pizza.payment_service.config.PaymentConfig;
import com.pizza.payment_service.dto.PaymentEvent;
import com.pizza.payment_service.dto.PaymentVerificationRequest;
import com.pizza.payment_service.model.Transaction;
import com.pizza.payment_service.repository.TransactionRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PaymentConfig paymentConfig;

    /**
     * Step 1: Create Order
     * Called when user clicks "Checkout".
     */
    public Transaction createOrder(Double amount, UUID userId) throws RazorpayException {
        // 1. Initialize Razorpay Client
        RazorpayClient client = new RazorpayClient(paymentConfig.getId(), paymentConfig.getSecret());

        // 2. Prepare Razorpay Order Request (Amount in paise: 1 INR = 100 paise)
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", (int) (amount * 100));
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "txn_" + System.currentTimeMillis());

        // 3. Create order in Razorpay systems
        Order razorpayOrder = client.orders.create(orderRequest);
        String razorpayOrderId = razorpayOrder.get("id");

        // 4. Save initial transaction in local DB
        Transaction transaction = Transaction.builder()
                .razorpayOrderId(razorpayOrderId)
                .userId(userId)
                .amount(amount)
                .currency("INR")
                .status("CREATED")
                .build();

        log.info("Razorpay Order Created: {} for User: {}", razorpayOrderId, userId);
        return transactionRepository.save(transaction);
    }

    /**
     * Step 2: Verify and Notify
     * Called after user completes payment in the UI.
     */
    @Transactional
    public void verifyAndNotify(PaymentVerificationRequest request) {
        // 1. Verify Signature using the secret from config
        boolean isValid = verifySignature(request);

        if (!isValid) {
            log.error("Invalid signature for Razorpay Order ID: {}", request.getRazorpayOrderId());
            throw new RuntimeException("Payment verification failed: Invalid Signature");
        }

        // 2. Update local Transaction record
        Transaction transaction = transactionRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new RuntimeException("Transaction record not found for Order: " + request.getRazorpayOrderId()));

        transaction.setRazorpayPaymentId(request.getRazorpayPaymentId());
        transaction.setRazorpaySignature(request.getRazorpaySignature());
        transaction.setStatus("SUCCESSFUL");
        transactionRepository.save(transaction);

        // 3. Notify Order-Service via Kafka
        PaymentEvent event = PaymentEvent.builder()
                .orderId(request.getRazorpayOrderId())
                .transactionId(transaction.getId().toString())
                .status("SUCCESSFUL")
                .message("Payment verified successfully")
                .build();

        kafkaTemplate.send("payment-topic", event);
        log.info("Payment success event published to Kafka for Order: {}", request.getRazorpayOrderId());
    }

    private boolean verifySignature(PaymentVerificationRequest request) {
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", request.getRazorpayOrderId());
            options.put("razorpay_payment_id", request.getRazorpayPaymentId());
            options.put("razorpay_signature", request.getRazorpaySignature());

            // Utils.verifyPaymentSignature returns void; throws exception if invalid
            Utils.verifyPaymentSignature(options, paymentConfig.getSecret());
            return true;
        } catch (RazorpayException e) {
            log.error("Razorpay verification error: {}", e.getMessage());
            return false;
        }
    }
}