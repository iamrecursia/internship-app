package com.kozitskiy.orderservice.service;

import com.kozitskiy.dto.OrderCreatedEvent;
import com.kozitskiy.orderservice.client.UserClient;
import com.kozitskiy.orderservice.dto.*;
import com.kozitskiy.orderservice.entity.Item;
import com.kozitskiy.orderservice.entity.Order;
import com.kozitskiy.orderservice.entity.OrderItem;
import com.kozitskiy.orderservice.entity.OrderStatus;
import com.kozitskiy.orderservice.exception.InvalidOrderStatusException;
import com.kozitskiy.orderservice.exception.ItemNotFoundException;
import com.kozitskiy.orderservice.exception.OrderNotFoundException;
import com.kozitskiy.orderservice.kafka.producer.OrderEventProducer;
import com.kozitskiy.orderservice.mapper.OrderMapper;
import com.kozitskiy.orderservice.repository.ItemRepository;
import com.kozitskiy.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService{

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderMapper orderMapper;
    private final UserClient userClient; // Feign-client
    private final OrderEventProducer orderEventProducer;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        // Create Order
        Order order = Order.builder()
                .userId(request.getUserId())
                .status(OrderStatus.PENDING)
                .creationDate(LocalDateTime.now())
                .orderItems(new ArrayList<>())
                .userEmail(request.getUserEmail()) // userEmail
                .build();

        for (OrderItemRequest itemRequest : request.getItems()){
            Item item = itemRepository.findById(itemRequest.getItemId())
                    .orElseThrow(() -> new ItemNotFoundException("Item with id: " + itemRequest.getItemId() + " wasn't found"));

            // Creating orderItem line
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .item(item)
                    .quantity(itemRequest.getQuantity())
                    .build();

            order.getOrderItems().add(orderItem);
        }

        BigDecimal totalAmount = order.getOrderItems().stream()
                .map(oi -> oi.getItem().getPrice().multiply(BigDecimal.valueOf(oi.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order savedOrder = orderRepository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(
                order.getId(),
                order.getUserId(),
                totalAmount,
                "USD"
        );

        orderEventProducer.sendOrderCreated(event);

        log.info("Order created and sent to Kafka. ID: {}", savedOrder.getId());

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
        if (ids == null || ids.isEmpty()){
            return List.of();
        }

        List<Order> orders = orderRepository.findAllById(ids);
        return orders.stream()
                .map(this::enrichOrderWithUser)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatuses(List<String> statuses) {
        if(statuses == null || statuses.isEmpty()){
            return List.of();
        }

        List<OrderStatus> statusEnums = new ArrayList<>();
        for (String statusStr : statuses) {
            if (statusStr == null || statusStr.isBlank()) continue;
            try {
                OrderStatus status = OrderStatus.valueOf(statusStr.toUpperCase());
                statusEnums.add(status);
            } catch (IllegalArgumentException e) {
                throw new InvalidOrderStatusException("Invalid order status: " + statusStr);
            }
        }
        if (statusEnums.isEmpty()) {
            return List.of();
        }

        List<Order> orders = orderRepository.findByStatusIn(statusEnums);

        return orders.stream()
                .map(this::enrichOrderWithUser)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponse updateOrder(Long id, OrderUpdateRequest request) {
        if (request.getStatus() == null || request.getStatus().isBlank()){
            throw new InvalidOrderStatusException("Status cannot be null or empty");
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));


        try {
            OrderStatus newStatus = OrderStatus.valueOf(request.getStatus().toUpperCase());
            order.setStatus(newStatus);
        }catch (IllegalArgumentException e) {
            throw new InvalidOrderStatusException("Invalid status: " + request.getStatus());
        }

        Order saved = orderRepository.save(order);

        return enrichOrderWithUser(saved);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)){
            throw new OrderNotFoundException("Order not found: " + id);
        }
        orderRepository.deleteById(id);
    }

    private OrderResponse enrichOrderWithUser(Order order){
        System.out.println("enrichOrderWith called for email: " + order.getUserEmail());
        OrderResponse response = orderMapper.toDto(order);
        try {
            UserDto user = userClient.getUserByEmail(order.getUserEmail());
            response.setUser(user);
        }catch (Exception e){
            System.out.println("WARN: Failed to fetch user by email: " + order.getUserEmail() + ", error: " + e.getMessage());
            response.setUser(null);
        }
        return response;
    }
}
