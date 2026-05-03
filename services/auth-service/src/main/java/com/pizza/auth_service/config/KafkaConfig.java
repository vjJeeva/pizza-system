package com.pizza.auth_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic registrationTopic() {
        return TopicBuilder.name("user-registration-topic")
                .partitions(3) // Good for scalability later
                .replicas(1)
                .build();
    }
}