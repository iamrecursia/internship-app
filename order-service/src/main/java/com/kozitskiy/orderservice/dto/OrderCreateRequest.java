package com.kozitskiy.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @Email(message = "Invalid email format")
    private String userEmail;

    @NotNull(message = "Order items are required")
    @Size(min = 1, message = "Order must contain at least one item")
    private List<@Valid OrderItemRequest> items;
}
