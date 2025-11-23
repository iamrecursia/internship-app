package com.kozitskiy.orderservice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResponse {
    private Long itemId;
    private String itemName;
    private BigDecimal itemPrice;
    private Integer quantity;
}
