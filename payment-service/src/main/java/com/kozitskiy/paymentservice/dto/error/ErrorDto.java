package com.kozitskiy.paymentservice.dto.error;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;


@Builder
public record ErrorDto(String message, String errorCode, Instant timestamp) {
}
