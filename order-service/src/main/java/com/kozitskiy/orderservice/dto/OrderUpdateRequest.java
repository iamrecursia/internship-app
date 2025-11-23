package com.kozitskiy.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderUpdateRequest {
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "PENDING|PROCESSING|COMPLETED|CANCELLED|CONFIRMED",
            message = "Status must be one of: PENDING, PROCESSING, COMPLETED, CANCELLED")
    private String status;
}
