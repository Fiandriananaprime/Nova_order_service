package org.example.nova_order_service.event;

import java.time.Instant;

public interface Event {
    String eventType();
    Instant occuredAt();
}
