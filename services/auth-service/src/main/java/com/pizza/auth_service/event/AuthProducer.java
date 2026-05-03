package com.pizza.auth_service.event;

import com.pizza.auth_service.dto.UserRegistrationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthProducer {

    private final KafkaTemplate<String, UserRegistrationEvent> kafkaTemplate;

    public void sendRegistrationEvent(UserRegistrationEvent event) {
        log.info("Sending registration event for: {}", event.getEmail());
        kafkaTemplate.send("user-registration-topic", event);
    }
}
