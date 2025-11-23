package com.kozitskiy.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kozitskiy.orderservice.dto.*;
import com.kozitskiy.orderservice.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    void createOrder_shouldReturnCreatedOrder() throws Exception {
        // Given: входной запрос
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setItemId(1L);
        itemRequest.setQuantity(2);

        OrderCreateRequest request = new OrderCreateRequest();
        request.setUserId(123L);
        request.setUserEmail("test@example.com");
        request.setItems(List.of(itemRequest));

        // Given: ответ от сервиса
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setEmail("test@example.com");
        userDto.setName("Test User");

        OrderResponse serviceResponse = OrderResponse.builder()
                .id(1L)
                .userId(123L)
                .userEmail("test@example.com")
                .status("PENDING")
                .creationDate(LocalDateTime.now())
                .items(List.of())
                .user(userDto)
                .build();

        when(orderService.createOrder(any(OrderCreateRequest.class)))
                .thenReturn(serviceResponse);

        // When & Then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(123))
                .andExpect(jsonPath("$.userEmail").value("test@example.com"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createOrder_shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {
        OrderCreateRequest invalidRequest = new OrderCreateRequest();
        invalidRequest.setUserEmail("test@example.com");
        invalidRequest.setItems(List.of());

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteOrder_shouldReturnNoContent() throws Exception {
        doNothing().when(orderService).deleteOrder(1L);

        mockMvc.perform(delete("/orders/1"))
                .andExpect(status().isNoContent());
    }


    @Test
    void getOrdersByStatuses_shouldReturnOrders() throws Exception {
        OrderResponse order = OrderResponse.builder()
                .id(1L)
                .userId(100L)
                .status("PENDING")
                .userEmail("test@example.com")
                .items(List.of())
                .user(new UserDto())
                .build();

        when(orderService.getOrdersByStatuses(Arrays.asList("pending", "confirmed")))
                .thenReturn(List.of(order));

        mockMvc.perform(get("/orders/by-status?statuses=pending&statuses=confirmed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void getOrderById_shouldReturnOrder() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setEmail("test@example.com");

        OrderResponse response = OrderResponse.builder()
                .id(10L)
                .userId(123L)
                .userEmail("test@example.com")
                .status("PENDING")
                .creationDate(LocalDateTime.now())
                .items(List.of())
                .user(userDto)
                .build();

        when(orderService.getOrderById(10L)).thenReturn(response);

        mockMvc.perform(get("/orders/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.user.email").value("test@example.com"));
    }



    @Test
    void updateOrder_shouldReturnBadRequest_whenStatusInvalid() throws Exception {
        OrderUpdateRequest invalidRequest = new OrderUpdateRequest();
        invalidRequest.setStatus(""); // пустой статус

        mockMvc.perform(patch("/orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

}