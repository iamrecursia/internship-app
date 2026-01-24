package com.kozitskiy.paymentservice.dto;

import com.kozitskiy.paymentservice.entity.enums.PaymentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
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
