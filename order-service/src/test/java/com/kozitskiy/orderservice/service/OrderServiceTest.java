package com.kozitskiy.orderservice.service;

import com.kozitskiy.orderservice.client.UserClient;
import com.kozitskiy.orderservice.dto.*;
import com.kozitskiy.orderservice.entity.Item;
import com.kozitskiy.orderservice.entity.Order;
import com.kozitskiy.orderservice.entity.OrderItem;
import com.kozitskiy.orderservice.entity.OrderStatus;
import com.kozitskiy.orderservice.exception.InvalidOrderStatusException;
import com.kozitskiy.orderservice.mapper.OrderMapper;
import com.kozitskiy.orderservice.repository.ItemRepository;
import com.kozitskiy.orderservice.repository.OrderRepository;
import com.kozitskiy.orderservice.service.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrder_shouldCreateOrderWithItems() {
        // Given
        Long itemId = 1L;

        // Создаем Item
        Item item = Item.builder()
                .id(itemId)
                .name("Test item")
                .price(new BigDecimal("10.00"))
                .build();

        // Создаем запрос
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setItemId(itemId);
        itemRequest.setQuantity(2);

        OrderCreateRequest request = new OrderCreateRequest();
        request.setUserId(123L);
        request.setUserEmail("test@example.com");
        request.setItems(List.of(itemRequest));

        // Создаем Order с OrderItems
        Order savedOrder = Order.builder()
                .id(1L)
                .userId(123L)
                .status(OrderStatus.PENDING)
                .creationDate(LocalDateTime.now())
                .userEmail("test@example.com")
                .orderItems(new ArrayList<>())
                .build();

        // Create OrderItem and inject in Order
        OrderItem orderItem = OrderItem.builder()
                .id(1L)
                .order(savedOrder)
                .item(item)
                .quantity(2)
                .build();
        savedOrder.getOrderItems().add(orderItem);

        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setEmail("test@example.com");
        userDto.setName("Test User");

        // Response
        OrderResponse expectedResponse = OrderResponse.builder()
                .id(1L)
                .userId(123L)
                .userEmail("test@example.com")
                .status(OrderStatus.PENDING.name())
                .items(List.of()) // OrderItemMapper создаст пустой список
                .user(userDto)
                .build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderMapper.toDto(savedOrder)).thenReturn(
                OrderResponse.builder()
                        .id(1L)
                        .userId(123L)
                        .userEmail("test@example.com")
                        .status(OrderStatus.PENDING.name())
                        .items(List.of())
                        .user(userDto)
                        .build()
        );
        when(userClient.getUserByEmail("test@example.com")).thenReturn(userDto);
        OrderResponse response = orderService.createOrder(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUser()).isNotNull(); // ← новая проверка
        assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");

        // Verify
        verify(itemRepository).findById(itemId);
        verify(orderRepository).save(any(Order.class));
        verify(orderMapper).toDto(savedOrder);
        verify(userClient).getUserByEmail("test@example.com");
    }

    @Test
    void getOrderById_shouldReturnOrder_whenOrderExists(){

        Long orderId = 1L;

        Order order = Order.builder()
                .id(orderId)
                .userId(123L)
                .status(OrderStatus.PENDING)
                .creationDate(LocalDateTime.now())
                .userEmail("test@email.com")
                .orderItems(new ArrayList<>())
                .build();

        Item item1 = Item.builder()
                .id(1L)
                .name("item1")
                .price(new BigDecimal("10.00"))
                .build();

        Item item2 = Item.builder()
                .id(2L)
                .name("item2")
                .price(new BigDecimal("20.00"))
                .build();

        OrderItem orderItem1 = OrderItem.builder()
                .id(1L)
                .item(item1)
                .quantity(2)
                .order(order)
                .build();

        OrderItem orderItem2 =OrderItem.builder()
                .id(2L)
                .item(item2)
                .order(order)
                .quantity(1)
                .build();

        order.getOrderItems().addAll(List.of(orderItem1, orderItem2));

        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setEmail("test@email.com");
        userDto.setName("Test User");

        OrderResponse expectedResponse = OrderResponse.builder()
                .id(orderId)
                .userId(123L)
                .status(OrderStatus.PENDING.name())
                .user(userDto)
                .userEmail("test@email.com")
                .items(List.of(

                ))
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(expectedResponse);

        OrderResponse response = orderService.getOrderById(orderId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(orderId);
        assertThat(response.getUserId()).isEqualTo(123L);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING.name());

        verify(orderRepository).findById(orderId);
        verify(orderMapper).toDto(order);
    }

    @Test
    void getOrderById_shouldThrowException_whenOrderNotFound(){

        Long nonExistentId = 999L;

        when(orderRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(nonExistentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Order not found: " + nonExistentId);

        verify(orderRepository).findById(nonExistentId);
        verify(orderMapper, never()).toDto(any(Order.class));
    }

    @Test
    void getOrdersByIds_shouldReturnMappedOrders_whenOrdersExist(){
        Long id1 = 1L;
        Long id2 = 2L;

        Order order1 = new Order();
        order1.setId(id1);
        order1.setUserId(100L);
        order1.setUserEmail("test@mail.com");
        order1.setStatus(OrderStatus.PENDING);

        Order order2 = new Order();
        order2.setId(id2);
        order2.setUserId(101L);
        order2.setUserEmail("test@mail.com");
        order2.setStatus(OrderStatus.CONFIRMED);

        OrderResponse dto1 = OrderResponse.builder()
                .id(id1)
                .userId(100L)
                .status("PENDING")
                .userEmail("test@mail.com")
                .build();

        OrderResponse dto2 = OrderResponse.builder()
                .id(id2)
                .userId(101L)
                .status("CONFIRMED")
                .userEmail("test@mail.com")
                .build();

        when(orderRepository.findAllById(Arrays.asList(id1, id2)))
                .thenReturn(Arrays.asList(order1, order2));

        when(orderMapper.toDto(order1)).thenReturn(dto1);
        when(orderMapper.toDto(order2)).thenReturn(dto2);

        List<OrderResponse> result = orderService.getOrdersByIds(Arrays.asList(id1, id2));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(OrderResponse::getId)
                .containsExactlyInAnyOrder(id1, id2);

        verify(orderRepository).findAllById(Arrays.asList(id1, id2));
        verify(orderMapper).toDto(order1);
        verify(orderMapper).toDto(order2);
    }

    @Test
    void getOrdersByIds_shouldReturnEmptyList_whenIdsEmpty(){
        List<OrderResponse> result = orderService.getOrdersByIds(List.of());
        assertThat(result).isEmpty();
        verifyNoInteractions(orderRepository, orderMapper);
    }

    @Test
    void getOrdersByIds_shouldReturnEmptyList_whenIdsNull(){
        List<OrderResponse> result = orderService.getOrdersByIds(null);
        assertThat(result).isEmpty();
        verifyNoInteractions(orderRepository, orderMapper);
    }

    @Test
    void getOrdersByStatuses_shouldReturnMappedOrders_whenValidStatuses(){
        List<String> statuses = Arrays.asList("pending", "confirmed");

        Order order1 = new Order();
        order1.setId(1L);
        order1.setStatus(OrderStatus.PENDING);

        Order order2 = new Order();
        order2.setId(2L);
        order2.setStatus(OrderStatus.CONFIRMED);

        OrderResponse dto1 = OrderResponse.builder()
                .id(1L)
                .status("PENDING")
                .build();

        OrderResponse dto2 = OrderResponse.builder()
                .id(2L)
                .status("CONFIRMED")
                .build();

        when(orderRepository.findByStatusIn(Arrays.asList(OrderStatus.PENDING, OrderStatus.CONFIRMED)))
                .thenReturn(Arrays.asList(order1, order2));

        when(orderMapper.toDto(order1)).thenReturn(dto1);
        when(orderMapper.toDto(order2)).thenReturn(dto2);

        List<OrderResponse> result = orderService.getOrdersByStatuses(statuses);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(OrderResponse::getStatus)
                .containsExactlyInAnyOrder("PENDING", "CONFIRMED");

        verify(orderRepository).findByStatusIn(Arrays.asList(OrderStatus.PENDING, OrderStatus.CONFIRMED));
        verify(orderMapper, times(2)).toDto(any(Order.class));
    }

    @Test
    void getOrdersByStatuses_shouldReturnEmptyList_whenStatusesEmpty() {
        List<OrderResponse> result = orderService.getOrdersByStatuses(List.of());
        assertThat(result).isEmpty();
        verifyNoInteractions(orderRepository, orderMapper);
    }

    @Test
    void getOrdersByStatuses_shouldReturnEmptyList_whenStatusesNull() {
        List<OrderResponse> result = orderService.getOrdersByStatuses(null);
        assertThat(result).isEmpty();
        verifyNoInteractions(orderRepository, orderMapper);
    }

    @Test
    void getOrdersByStatuses_shouldThrowException_whenInvalidStatus() {
        List<String> invalidStatuses = Arrays.asList("pending", "invalid_status");

        assertThatThrownBy(() -> orderService.getOrdersByStatuses(invalidStatuses))
                .isInstanceOf(InvalidOrderStatusException.class)
                .hasMessage("Invalid order status: invalid_status");

        verifyNoInteractions(orderRepository, orderMapper);
    }

    @Test
    void updateOrder_shouldUpdateStatusAndReturnDto(){
        Long orderId = 1L;
        OrderUpdateRequest request = new OrderUpdateRequest();
        request.setStatus("shipped");

        Order existingOrder = Order.builder()
                .id(orderId)
                .status(OrderStatus.PENDING)
                .userId(100L)
                .build();

        Order updatedOrder = Order.builder()
                .id(orderId)
                .status(OrderStatus.SHIPPED)
                .userId(100L)
                .build();

        OrderResponse expectedDto = OrderResponse.builder()
                .id(orderId)
                .status("SHIPPED")
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);
        when(orderMapper.toDto(updatedOrder)).thenReturn(expectedDto);

        OrderResponse result = orderService.updateOrder(orderId, request);

        assertThat(result.getStatus()).isEqualTo("SHIPPED");

        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(argThat(order ->
                order.getId().equals(orderId) &&
                        order.getStatus() == OrderStatus.SHIPPED
        ));
        verify(orderMapper).toDto(updatedOrder);
    }

    @Test
    void updateOrder_shouldThrowException_whenOrderNotFound() {
        Long orderId = 999L;
        OrderUpdateRequest request = new OrderUpdateRequest();
        request.setStatus("shipped");

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateOrder(orderId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Order not found: " + orderId);

        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void updateOrder_shouldThrowException_whenInvalidStatus() {
        Long orderId = 1L;
        OrderUpdateRequest request = new OrderUpdateRequest();
        request.setStatus("invalid");

        Order order = new Order();
        order.setId(orderId);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateOrder(orderId, request))
                .isInstanceOf(InvalidOrderStatusException.class)
                .hasMessage("Invalid status: invalid");

        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void deleteOrder_shouldDeleteOrder_whenExists() {
        Long orderId = 1L;

        when(orderRepository.existsById(orderId)).thenReturn(true);

        orderService.deleteOrder(orderId);

        verify(orderRepository).existsById(orderId);
        verify(orderRepository).deleteById(orderId);
    }

    @Test
    void deleteOrder_shouldThrowException_whenOrderNotFound() {
        Long orderId = 999L;

        when(orderRepository.existsById(orderId)).thenReturn(false);

        assertThatThrownBy(() -> orderService.deleteOrder(orderId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Order not found: " + orderId);

        verify(orderRepository).existsById(orderId);
        verify(orderRepository, never()).deleteById(any());
    }


}
