package org.example.nova_order_service.configuration;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class amqpConfig {
    public static  final String EXCHANGE = "nova.events";

    @Bean
    public TopicExchange exchange(){
        return new TopicExchange(EXCHANGE);
    }
}
