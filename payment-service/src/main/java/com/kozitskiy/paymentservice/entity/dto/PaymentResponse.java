package com.kozitskiy.paymentservice.entity.dto;

import com.kozitskiy.paymentservice.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        Long id,
        Long orderId,
        Long userId,
        PaymentStatus status,
        BigDecimal amount,
        String currency,
        Instant createdAt
) {
}
