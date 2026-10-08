package org.example.nova_order_service.service;

import org.example.nova_order_service.event.Event;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class amqpPublisherService {
    private final RabbitTemplate rabbitTemplate;

    public amqpPublisherService(RabbitTemplate template){
        this.rabbitTemplate = template;
    }

    public void Publish(Event event){
        rabbitTemplate.convertAndSend(
                "nova.events",
                event.eventType(),
                event
        );
    }
}
