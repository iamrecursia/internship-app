package com.kozitskiy.dto;

import lombok.Builder;

@Builder
public record PaymentProcessedEvent(
        Long orderId,
        Long paymentId,
        String status
) {

}
