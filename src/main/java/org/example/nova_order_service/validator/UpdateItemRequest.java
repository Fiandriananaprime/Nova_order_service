package org.example.nova_order_service.validator;

import jakarta.validation.constraints.Min;

public record UpdateItemRequest (
    @Min(1)
    int qty
){}
