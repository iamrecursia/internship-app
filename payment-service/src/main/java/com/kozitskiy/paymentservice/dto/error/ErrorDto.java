package com.kozitskiy.paymentservice.dto.error;

import lombok.Builder;

import java.time.Instant;

@Builder
public record ErrorDto(String message, String errorCode, Instant timestamp) {
}
