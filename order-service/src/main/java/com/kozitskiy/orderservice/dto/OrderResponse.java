package com.kozitskiy.orderservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;


@Builder(toBuilder = true)
public record OrderResponse(
        Long id,
        Long userId,
        String userEmail, //added
        String status,
        LocalDateTime creationDate,
        List<OrderItemResponse> items,
        UserDto user //added
) {
}
