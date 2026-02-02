package com.kozitskiy.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kozitskiy.orderservice.dto.*;
import com.kozitskiy.orderservice.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @Test
    @DisplayName("Should return 201 Created when order is successfully created")
    void createOrder_ShouldReturnCreated() throws Exception {
        OrderItemRequest itemRequest = new OrderItemRequest(101L, 2);
        OrderCreateRequest request = new OrderCreateRequest(1L, "test@mail.com", List.of(itemRequest));

        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("Ivan")
                .surname("Ivanov")
                .email("test@mail.com")
                .birthDate(LocalDate.of(1990, 5, 15))
                .build();

        OrderItemResponse itemResponse = OrderItemResponse.builder()
                .itemId(1L)
                .itemName("Laptop")
                .itemPrice(BigDecimal.valueOf(150.0))
                .quantity(2)
                .build();

        OrderResponse response = OrderResponse.builder()
                .id(1L)
                .userId(1L)
                .userEmail("test@mail.com")
                .status("CREATED")
                .creationDate(LocalDateTime.now())
                .items(List.of(itemResponse))
                .user(userDto)
                .build();

        when(orderService.createOrder(any(OrderCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.user.name").value("Ivan"))
                .andExpect(jsonPath("$.items[0].itemName").value("Laptop"))
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{id} - Should return 200 OK")
    void getOrderById_ShouldReturnOrder() throws Exception {
        Long orderId = 1L;
        OrderResponse response = OrderResponse.builder().id(orderId).userEmail("test@mail.com").build();

        when(orderService.getOrderById(orderId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.userEmail").value("test@mail.com"));
    }

    @Test
    @DisplayName("GET /api/v1/orders?ids=... - Should return list of orders")
    void getOrdersByIds_ShouldReturnList() throws Exception {
        List<Long> ids = List.of(1L, 2L);
        OrderResponse res1 = OrderResponse.builder().id(1L).build();
        OrderResponse res2 = OrderResponse.builder().id(2L).build();

        when(orderService.getOrdersByIds(ids)).thenReturn(List.of(res1, res2));

        mockMvc.perform(get("/api/v1/orders")
                        .param("ids", "1,2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("PATCH /api/v1/orders/{id} - Should update status")
    void updateOrder_ShouldReturnUpdatedOrder() throws Exception {
        Long orderId = 1L;
        OrderUpdateRequest updateRequest = new OrderUpdateRequest("COMPLETED");
        OrderResponse response = OrderResponse.builder().id(orderId).status("COMPLETED").build();

        when(orderService.updateOrder(eq(orderId), any(OrderUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/orders/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("DELETE /api/v1/orders/{id} - Should return 204 No Content")
    void deleteOrder_ShouldReturnNoContent() throws Exception {
        Long orderId = 1L;
        doNothing().when(orderService).deleteOrder(orderId);

        mockMvc.perform(delete("/api/v1/orders/{id}", orderId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /api/v1/orders - Should return 400 if validation fails")
    void createOrder_InvalidEmail_ShouldReturnBadRequest() throws Exception {
        // Невалидный email
        OrderCreateRequest invalidRequest = new OrderCreateRequest(1L, "invalid-email", List.of());

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}