package org.example.nova_order_service.event;

import java.time.Instant;
import java.util.UUID;

public record ItemAdded(UUID buyerId,UUID productId,int quantity) implements Event {

    @Override
    public String eventType(){
        return "order.item.added";
    }

    @Override
    public Instant occuredAt(){
        return Instant.now();
    }
}
