package com.kozitskiy.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
public record OrderUpdateRequest(
        @NotBlank(message = "Status is required")
        @Pattern(regexp = "PENDING|PROCESSING|COMPLETED|CANCELLED|CONFIRMED",
                message = "Status must be one of: PENDING, PROCESSING, COMPLETED, CANCELLED")
        String status
) {
}
