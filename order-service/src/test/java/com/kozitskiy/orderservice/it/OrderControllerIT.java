package com.kozitskiy.orderservice.it;


import com.kozitskiy.orderservice.client.UserClient;
import com.kozitskiy.orderservice.dto.*;
import com.kozitskiy.orderservice.entity.Item;
import com.kozitskiy.orderservice.kafka.producer.OrderEventProducer;
import com.kozitskiy.orderservice.repository.ItemRepository;
import com.kozitskiy.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderControllerIT extends BaseIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ItemRepository itemRepository;

    @MockitoBean
    private OrderEventProducer orderEventProducer;

    @MockitoBean
    private UserClient userClient;

    private Long savedItemId;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        itemRepository.deleteAll();

        Item item = new Item();
        item.setName("Test Product");
        item.setPrice(BigDecimal.valueOf(100.0));
        Item savedItem = itemRepository.save(item);
        this.savedItemId = savedItem.getId();
    }

    @Test
    @DisplayName("Should create order and save it to database")
    void createOrder_ShouldSaveToDb() {
        OrderItemRequest itemRequest = new OrderItemRequest(savedItemId, 2);
        OrderCreateRequest request = new OrderCreateRequest(1L, "test@mail.com", List.of(itemRequest));

        ResponseEntity<OrderResponse> response = restTemplate.postForEntity(
                "/api/v1/orders", request, OrderResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().userEmail()).isEqualTo("test@mail.com");

        Long orderId = response.getBody().id();
        assertThat(orderId).isNotNull();
        assertThat(orderRepository.existsById(orderId)).isTrue();
    }

    @Test
    @DisplayName("Should return 400 Bad Request when validation fails")
    void createOrder_InvalidRequest_ShouldReturn400() {
        OrderCreateRequest invalidRequest = new OrderCreateRequest(1L, "invalid-email", List.of());

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/orders", invalidRequest, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Should fetch saved order by ID")
    void getOrderById_ShouldReturnOrder() {
        OrderItemRequest itemRequest = new OrderItemRequest(savedItemId, 1);
        OrderCreateRequest createRequest = new OrderCreateRequest(2L, "user@mail.com", List.of(itemRequest));

        ResponseEntity<OrderResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/orders", createRequest, OrderResponse.class);

        Long savedId = createResponse.getBody().id();

        ResponseEntity<OrderResponse> response = restTemplate.getForEntity(
                "/api/v1/orders/" + savedId, OrderResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().id()).isEqualTo(savedId);
    }

    @Test
    @DisplayName("Should return 404 when order does not exist")
    void getOrderById_NotFound() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/orders/999", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should delete order from database")
    void deleteOrder_ShouldRemoveFromDb() {
        OrderItemRequest itemRequest = new OrderItemRequest(savedItemId, 1);
        OrderCreateRequest createRequest = new OrderCreateRequest(1L, "del@mail.com", List.of(itemRequest));

        ResponseEntity<OrderResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/orders", createRequest, OrderResponse.class);

        Long id = createResponse.getBody().id();

        restTemplate.delete("/api/v1/orders/" + id);

        assertThat(orderRepository.existsById(id)).isFalse();
    }
}