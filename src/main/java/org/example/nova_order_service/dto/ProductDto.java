package org.example.nova_order_service.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductDto(
        UUID productId,
        String name,
        String image,
        BigDecimal price,
        Integer qty,
        UUID sellerId,
        String sellerName,
        Integer stock,
        BigDecimal originalPrice,
        BigDecimal discount,
        BigDecimal subtotal
) {
}
