package org.example.nova_order_service.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(List<ProductDto> items, BigDecimal subtotal) {
}
