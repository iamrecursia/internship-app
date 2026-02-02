package com.kozitskiy.orderservice.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Builder
public record OrderItemResponse(
        Long itemId,
        String itemName,
        BigDecimal itemPrice,
        Integer quantity
) {
}
