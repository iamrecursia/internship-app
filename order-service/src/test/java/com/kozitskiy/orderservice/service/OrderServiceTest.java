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
import com.kozitskiy.orderservice.kafka.producer.OrderEventProducer;
import com.kozitskiy.orderservice.mapper.OrderItemMapper;
import com.kozitskiy.orderservice.mapper.OrderMapper;
import com.kozitskiy.orderservice.repository.ItemRepository;
import com.kozitskiy.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private OrderMapper orderMapper;
    @Mock private OrderItemMapper orderItemMapper;
    @Mock private UserClient userClient;
    @Mock private OrderEventProducer orderEventProducer;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    @DisplayName("Successfully create order and send event to Kafka")
    void createOrder_Success() {
        OrderCreateRequest request = createOrderRequest();
        Order order = new Order();
        order.setUserEmail("test@test.com");

        Item item = Item.builder()
                .id(1L)
                .price(BigDecimal.TEN)
                .build();

        OrderItem orderItem = new OrderItem();
        orderItem.setQuantity(2);
        orderItem.setItem(item);

        when(orderMapper.toEntity(any())).thenReturn(order);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(orderItemMapper.toEntity(any())).thenReturn(orderItem);


        when(orderRepository.save(any())).thenReturn(order);
        when(orderMapper.toEvent(any(), any())).thenReturn(mock(OrderCreatedEvent.class));
        when(orderMapper.toDto(any())).thenReturn(createOrderResponse());
        when(userClient.getUserByEmail(anyString())).thenReturn(UserDto.builder().build());


        OrderResponse result = orderService.createOrder(request);

        assertThat(result).isNotNull();
        verify(orderRepository).save(any(Order.class));
        verify(orderEventProducer).sendOrderCreated(any());
    }

    @Test
    @DisplayName("Should throw ItemNotFoundException when item does not exist")
    void createOrder_ItemNotFound_ThrowsException() {

        OrderCreateRequest request = createOrderRequest();
        when(orderMapper.toEntity(any())).thenReturn(new Order());
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ItemNotFoundException.class);
    }

    @Test
    @DisplayName("Should return OrderResponse even if UserClient fails (enrichOrderWithUser)")
    void getOrderById_UserClientFails_ReturnsPartialResponse() {

        Long orderId = 1L;
        Order order = new Order();
        order.setUserEmail("fail@test.com");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(any())).thenReturn(createOrderResponse());
        when(userClient.getUserByEmail(anyString())).thenThrow(new RuntimeException("Service Down"));

        OrderResponse result = orderService.getOrderById(orderId);

        assertThat(result).isNotNull();
        assertThat(result.user()).isNull(); // Убеждаемся, что ошибка клиента не сломала метод
        verify(orderRepository).findById(orderId);
    }

    @Test
    @DisplayName("Update order status successfully")
    void updateOrder_Success() {

        Long orderId = 1L;
        OrderUpdateRequest updateRequest = new OrderUpdateRequest("COMPLETED");
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);
        when(orderMapper.toDto(any())).thenReturn(createOrderResponse());

        OrderResponse result = orderService.updateOrder(orderId, updateRequest);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("Should throw exception for invalid status string")
    void updateOrder_InvalidStatus_ThrowsException() {

        Long orderId = 1L;
        OrderUpdateRequest updateRequest = new OrderUpdateRequest("INVALID_STATUS");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(new Order()));

        assertThatThrownBy(() -> orderService.updateOrder(orderId, updateRequest))
                .isInstanceOf(InvalidOrderStatusException.class);
    }

    private OrderCreateRequest createOrderRequest() {
        return OrderCreateRequest.builder()
                .userId(1L)
                .userEmail("test@test.com")
                .items(List.of(new OrderItemRequest(1L, 2)))
                .build();
    }

    private OrderResponse createOrderResponse() {
        return OrderResponse.builder()
                .id(1L)
                .status("PENDING")
                .build();
    }
}