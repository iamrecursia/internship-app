package com.kozitskiy.orderservice.service;

import com.kozitskiy.orderservice.BaseIntegrationTest;
import com.kozitskiy.orderservice.dto.OrderCreateRequest;
import com.kozitskiy.orderservice.dto.OrderItemRequest;
import com.kozitskiy.orderservice.dto.OrderResponse;
import com.kozitskiy.orderservice.dto.OrderUpdateRequest;
import com.kozitskiy.orderservice.entity.Item;
import com.kozitskiy.orderservice.entity.Order;
import com.kozitskiy.orderservice.entity.OrderStatus;
import com.kozitskiy.orderservice.repository.ItemRepository;
import com.kozitskiy.orderservice.repository.OrderRepository;
import com.kozitskiy.orderservice.service.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class OrderServiceIntegrationTest extends BaseIntegrationTest {


    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ItemRepository itemRepository;

    private Long existingItemId;

    @BeforeEach
    void setUp() {
        // Создаём и сохраняем item, получаем его реальный ID
        Item item = new Item();
        item.setName("Test Laptop");
        item.setPrice(BigDecimal.valueOf(999.99));
        Item savedItem = itemRepository.saveAndFlush(item);
        existingItemId = savedItem.getId(); // ← безопасно!
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @Test
    void shouldCreateOrderWithValidItems() {
        // Given
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setItemId(existingItemId); // ← используем реальный ID
        itemRequest.setQuantity(3);

        OrderCreateRequest request = new OrderCreateRequest();
        request.setUserId(123L);
        request.setUserEmail("customer@example.com");
        request.setItems(List.of(itemRequest));

        // When
        OrderResponse response = orderService.createOrder(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getUserId()).isEqualTo(123L);
        assertThat(response.getUserEmail()).isEqualTo("customer@example.com");
        assertThat(response.getItems()).hasSize(1);
    }

    @Test
    void shouldThrowExceptionWhenItemNotFound() {
        // Given
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setItemId(999L); // гарантированно несуществующий ID
        itemRequest.setQuantity(1);

        OrderCreateRequest request = new OrderCreateRequest();
        request.setUserId(100L);
        request.setUserEmail("user@test.com");
        request.setItems(List.of(itemRequest));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Item with id: 999 wasn't found"); // ← точное совпадение
    }

    @Test
    void shouldUpdateOrderStatus(){
        Item item = new Item();
        item.setName("Laptop");
        item.setPrice(BigDecimal.valueOf(1200));
        Item savedItem = itemRepository.saveAndFlush(item);

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setItemId(savedItem.getId());
        itemRequest.setQuantity(1);

        OrderCreateRequest createRequest = new OrderCreateRequest();
        createRequest.setUserId(100L);
        createRequest.setUserEmail("test@email.com");
        createRequest.setItems(List.of(itemRequest));

        OrderResponse createdOrder = orderService.createOrder(createRequest);

        OrderUpdateRequest updateRequest = new OrderUpdateRequest();
        updateRequest.setStatus("CONFIRMED");

        OrderResponse updatedOrder = orderService.updateOrder(createdOrder.getId(), updateRequest);

        assertThat(updatedOrder).isNotNull();
        assertThat(updatedOrder.getId()).isEqualTo(createdOrder.getId());
        assertThat(updatedOrder.getStatus()).isEqualTo("CONFIRMED");

        Order orderFromDB = orderRepository.findById(createdOrder.getId()).orElseThrow();
        assertThat(orderFromDB.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void shouldDeleteOrderById(){
        Item item = new Item();
        item.setName("Crap");
        item.setPrice(BigDecimal.valueOf(777));
        Item savedItem = itemRepository.save(item);


        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setQuantity(1);
        itemRequest.setItemId(savedItem.getId());

        OrderCreateRequest createRequest = new OrderCreateRequest();
        createRequest.setUserId(1L);
        createRequest.setItems(List.of(itemRequest));
        createRequest.setUserEmail("test@email.com");

        OrderResponse createdOrder = orderService.createOrder(createRequest);
        Long orderId = createdOrder.getId();

        //Check if order is in DB
        assertThat(orderRepository.existsById(orderId)).isTrue();

        //Delete the order
        orderService.deleteOrder(orderId);

        //Check if order has been deleted
        assertThat(orderRepository.existsById(orderId)).isFalse();
    }

    @Test
    void shouldThrowWhenDeletingNonExistentOrder() {
        assertThatThrownBy(() -> orderService.deleteOrder(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Order not found: 999");
    }
}