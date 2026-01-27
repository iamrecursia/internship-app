package com.kozitskiy.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record OrderCreatedEvent(
        Long orderId,
        Long userId,
        BigDecimal amount,
        String currency
) {
}
