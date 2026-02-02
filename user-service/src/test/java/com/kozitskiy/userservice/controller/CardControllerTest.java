package com.kozitskiy.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kozitskiy.userservice.dto.CardRequest;
import com.kozitskiy.userservice.dto.CardResponse;
import com.kozitskiy.userservice.service.card.CardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CardService cardService;

    private final String BASE_URL = "/api/v1/cards";

    @Test
    @DisplayName("Should return 201 Created when creating a valid card")
    void createCard_ShouldReturnCreated() throws Exception {
        CardRequest request = new CardRequest(1L, "1234567812345678", "JOHN DOE", LocalDate.now().plusYears(1));
        CardResponse response = new CardResponse(10L, 1L, "1234567812345678", "JOHN DOE", LocalDate.now().plusYears(1));

        when(cardService.createCard(any(CardRequest.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.number").value("1234567812345678"));
    }

    @Test
    @DisplayName("Should return 200 OK when updating a card")
    void updateCard_ShouldReturnOk() throws Exception {
        long cardId = 10L;
        CardRequest request = new CardRequest(1L, "8765432187654321", "JANE DOE", LocalDate.now().plusYears(2));
        CardResponse response = new CardResponse(cardId, 1L, "8765432187654321", "JANE DOE", LocalDate.now().plusYears(2));

        when(cardService.updateCard(eq(cardId), any(CardRequest.class))).thenReturn(response);

        mockMvc.perform(put(BASE_URL + "/{id}", cardId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("8765432187654321"));
    }

    @Test
    @DisplayName("Should return card by ID")
    void getCardById_ShouldReturnCard() throws Exception {
        long cardId = 10L;
        CardResponse response = new CardResponse(cardId, 1L, "11112222", "HOLDER", LocalDate.now());

        when(cardService.getCardById(cardId)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/{id}", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId))
                .andExpect(jsonPath("$.holder").value("HOLDER"));
    }

    @Test
    @DisplayName("Should return page of all cards")
    void getAllCards_ShouldReturnPage() throws Exception {
        CardResponse response = new CardResponse(1L, 1L, "111", "H", LocalDate.now());
        Page<CardResponse> page = new PageImpl<>(List.of(response));

        when(cardService.getAllCards(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    @DisplayName("Should return page of cards by user ID")
    void getCardsByUserId_ShouldReturnPage() throws Exception {
        long userId = 1L;
        CardResponse response = new CardResponse(10L, userId, "111", "H", LocalDate.now());
        Page<CardResponse> page = new PageImpl<>(List.of(response));

        when(cardService.getCardsByUserId(eq(userId), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get(BASE_URL + "/{userId}/by", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].userId").value(userId));
    }

    @Test
    @DisplayName("Should return 204 No Content on deletion")
    void deleteCard_ShouldReturnNoContent() throws Exception {
        long cardId = 10L;
        doNothing().when(cardService).deleteCardById(cardId);

        mockMvc.perform(delete(BASE_URL + "/{id}", cardId))
                .andExpect(status().isNoContent());
    }
}