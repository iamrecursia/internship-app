package com.kozitskiy.orderservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class OrderResponse {
    private Long id;
    private Long userId;
    private String userEmail; //added
    private String status;
    private LocalDateTime creationDate;
    private List<OrderItemResponse> items;
    private UserDto user; //added
}
