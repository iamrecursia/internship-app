package com.kozitskiy.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
public record OrderCreateRequest(
        @NotNull(message = "User ID is required")
        Long userId,

        @NotBlank
        @Email(message = "Invalid email format")
        String userEmail,

        @NotNull(message = "Order items are required")
        @Size(min = 1, message = "Order must contain at least one item")
        List<@Valid OrderItemRequest> items
) {
}
