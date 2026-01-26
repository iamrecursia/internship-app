package com.kozitskiy.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record PaymentRequest(

        @NotNull(message = "User ID is required")
        @Positive(message = "User ID must be positive")
        Long userId,

        @NotNull(message = "order ID is required")
        @Positive(message = "order ID must be positive")
        Long orderId,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency must be 3 characters (ISO 4217)")
        String currency
) {
}
