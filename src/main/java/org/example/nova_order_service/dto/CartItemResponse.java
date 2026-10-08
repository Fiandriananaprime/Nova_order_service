package org.example.nova_order_service.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartItemResponse(List<ProductDto> items, BigDecimal subTotal, Integer itemsCount) {
}
