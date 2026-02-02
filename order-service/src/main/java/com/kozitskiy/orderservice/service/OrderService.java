package com.kozitskiy.orderservice.service;

import com.kozitskiy.orderservice.dto.OrderCreateRequest;
import com.kozitskiy.orderservice.dto.OrderResponse;
import com.kozitskiy.orderservice.dto.OrderUpdateRequest;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(OrderCreateRequest request);

    OrderResponse getOrderById(Long id);

    List<OrderResponse> getOrdersByIds(List<Long> ids);

    List<OrderResponse> getOrdersByStatuses(List<String> statuses);

    OrderResponse updateOrder(Long id, OrderUpdateRequest request);

    void deleteOrder(Long id);
}
