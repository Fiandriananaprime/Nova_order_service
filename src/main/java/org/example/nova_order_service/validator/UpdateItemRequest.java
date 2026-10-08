package org.example.nova_order_service.validator;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateItemRequest (
    @Min(0)
    @NotNull
    int qty
){}
