package com.kozitskiy.orderservice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ErrorResponse(
        String message,
        int status,
        LocalDateTime timestamp
) {
}
