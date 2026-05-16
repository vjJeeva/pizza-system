package com.pizza.order_service.config;


import com.pizza.order_service.dto.OrderResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, OrderResponse> redisTemplate(RedisConnectionFactory connectionFactory){
        RedisTemplate<String, OrderResponse> template= new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());

        Jackson2JsonRedisSerializer<OrderResponse> serializer =new Jackson2JsonRedisSerializer<>(OrderResponse.class);
        template.setValueSerializer(serializer);

        return template;
    }
}
