package com.pizza.user_service.event;

import com.pizza.user_service.dto.UserRegistrationEvent;
import com.pizza.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationConsumer {

    private final UserService userService;

    @KafkaListener(topics = "user-registration-topic", groupId = "user-group")
    public void consumeRegistrationEvent(UserRegistrationEvent event){
        log.info("Received registration event for user ID: {}",event.getId());

        //this method will create an empty profile in user service db and this method is already defined in user-service
        userService.createInitialProfile(event.getId(), event.getEmail());
    }
}
