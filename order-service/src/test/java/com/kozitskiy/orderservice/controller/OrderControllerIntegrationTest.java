package com.kozitskiy.orderservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kozitskiy.orderservice.BaseIntegrationTest;
import com.kozitskiy.orderservice.dto.OrderCreateRequest;
import com.kozitskiy.orderservice.dto.OrderItemRequest;
import com.kozitskiy.orderservice.dto.OrderUpdateRequest;
import com.kozitskiy.orderservice.entity.Item;
import com.kozitskiy.orderservice.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class OrderControllerIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        Item item = new Item();
        item.setName("Phone");
        item.setPrice(BigDecimal.valueOf(500));
        itemRepository.save(item);
    }

    @Test
    void shouldCreateOrderAndReturn201() throws Exception {
        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setItemId(1L);
        itemReq.setQuantity(1);

        OrderCreateRequest request = new OrderCreateRequest();
        request.setUserId(200L);
        request.setUserEmail("customer@test.com");
        request.setItems(List.of(itemReq));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldReturn404WhenOrderNotFound() throws Exception {
        mockMvc.perform(get("/orders/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnEmptyListWhenIdsNotFound() throws Exception {
        mockMvc.perform(get("/orders")
                        .param("ids", "999", "888"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldUpdateOrderStatusViaApi() throws Exception{
        Item item = new Item();
        item.setPrice(BigDecimal.valueOf(1000));
        item.setName("Laptop");
        Item savedItem = itemRepository.save(item);

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setItemId(savedItem.getId());
        itemRequest.setQuantity(3);

        OrderCreateRequest createRequest = new OrderCreateRequest();
        createRequest.setUserId(100L);
        createRequest.setUserEmail("test@mail.com");
        createRequest.setItems(List.of(itemRequest));

        MvcResult createResult = mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createOrder = objectMapper.readTree(createResult.getResponse().getContentAsString());
        Long orderId = createOrder.get("id").asLong();

        OrderUpdateRequest updateRequest = new OrderUpdateRequest();
        updateRequest.setStatus("CONFIRMED");

        mockMvc.perform(patch("/orders/" + orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.intValue()))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void shouldDeleteOrderByIdViaApi() throws Exception {
        Item item = new Item();
        item.setPrice(BigDecimal.valueOf(1000));
        item.setName("Laptop");
        Item savedItem = itemRepository.save(item);

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setItemId(savedItem.getId());
        itemRequest.setQuantity(3);

        OrderCreateRequest createRequest = new OrderCreateRequest();
        createRequest.setUserId(100L);
        createRequest.setUserEmail("test@mail.com");
        createRequest.setItems(List.of(itemRequest));

        MvcResult createResult = mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createdOrder = objectMapper.readTree(createResult.getResponse().getContentAsString());
        Long oderId = createdOrder.get("id").asLong();

        mockMvc.perform(delete("/orders/" + oderId))
                .andExpect(status().isNoContent());
    }
}
