package com.kozitskiy.orderservice.service;

import com.kozitskiy.dto.OrderCreatedEvent;
import com.kozitskiy.orderservice.client.UserClient;
import com.kozitskiy.orderservice.dto.*;
import com.kozitskiy.orderservice.entity.Item;
import com.kozitskiy.orderservice.entity.Order;
import com.kozitskiy.orderservice.entity.OrderItem;
import com.kozitskiy.orderservice.entity.enums.OrderStatus;
import com.kozitskiy.orderservice.exception.InvalidOrderStatusException;
import com.kozitskiy.orderservice.exception.ItemNotFoundException;
import com.kozitskiy.orderservice.exception.OrderNotFoundException;
import com.kozitskiy.orderservice.exception.UserNotFoundException;
import com.kozitskiy.orderservice.kafka.producer.OrderEventProducer;
import com.kozitskiy.orderservice.mapper.OrderItemMapper;
import com.kozitskiy.orderservice.mapper.OrderMapper;
import com.kozitskiy.orderservice.repository.ItemRepository;
import com.kozitskiy.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserClient userClient;
    private final OrderEventProducer orderEventProducer;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {

        UserDto user = userClient.getUserByEmail(request.userEmail());

        if (user == null || user.id() == null) {
            throw new UserNotFoundException("User not found with email: " + request.userEmail());
        }

        Order order = orderMapper.toEntity(request);
        order.setUserId(user.id());
        order.setStatus(OrderStatus.PENDING);
        order.setCreationDate(LocalDateTime.now());

        request.items().forEach(itemReq -> {
            Item item = itemRepository.findById(itemReq.itemId())
                    .orElseThrow(() -> new ItemNotFoundException("Item not found " + itemReq.itemId()));

            OrderItem orderItem = orderItemMapper.toEntity(itemReq);
            orderItem.setItem(item);
            order.addOrderItem(orderItem);
        });

        Order savedOrder = orderRepository.save(order);

        OrderCreatedEvent event = orderMapper.toEvent(savedOrder, savedOrder.getTotalAmount());
        orderEventProducer.sendOrderCreated(event);

        log.info("Order created. ID: {}", savedOrder.getId());

        return enrichOrderWithUser(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));

        return enrichOrderWithUser(order);
    }


    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        List<Order> orders = orderRepository.findAllByIdIn(ids);
        return orders.stream()
                .map(this::enrichOrderWithUser)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatuses(List<String> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return List.of();
        }

        List<OrderStatus> statusEnums = statuses.stream()
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .map(s -> {
                    try {
                        return OrderStatus.valueOf(s.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new InvalidOrderStatusException("Invalid order status: " + s);
                    }
                })
                .toList();

        return orderRepository.findByStatusIn(statusEnums).stream()
                .map(this::enrichOrderWithUser)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse updateOrder(Long id, OrderUpdateRequest request) {
        if (request.status() == null || request.status().isBlank()) {
            throw new InvalidOrderStatusException("Status cannot be null or empty");
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));

        try {
            OrderStatus newStatus = OrderStatus.valueOf(request.status().toUpperCase());
            order.setStatus(newStatus);
        } catch (IllegalArgumentException e) {
            throw new InvalidOrderStatusException("Invalid status: " + request.status());
        }

        Order saved = orderRepository.save(order);

        return enrichOrderWithUser(saved);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new OrderNotFoundException("Order not found: " + id);
        }
        orderRepository.deleteById(id);
    }

    private OrderResponse enrichOrderWithUser(Order order) {
        OrderResponse response = orderMapper.toDto(order);
        try {
            UserDto user = userClient.getUserByEmail(order.getUserEmail());
            return response.toBuilder().user(user).build();
        } catch (Exception e) {
            log.warn("Failed to fetch user by email: {}, error: {}", order.getUserEmail(), e.getMessage());
            return response;
        }
    }
}
