package com.kozitskiy.userservice.service;

import com.kozitskiy.userservice.dto.CardRequest;
import com.kozitskiy.userservice.dto.CardResponse;
import com.kozitskiy.userservice.entity.Card;
import com.kozitskiy.userservice.entity.User;
import com.kozitskiy.userservice.exception.CardNotFoundException;
import com.kozitskiy.userservice.mapper.CardMapper;
import com.kozitskiy.userservice.repository.CardRepository;
import com.kozitskiy.userservice.repository.UserRepository;
import com.kozitskiy.userservice.service.card.CardServiceImpl;
import com.kozitskiy.userservice.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CardMapper cardMapper;
    @Mock
    private UserService userService;

    @InjectMocks
    private CardServiceImpl cardService;

    @Test
    @DisplayName("Should successfully create a card")
    void createCard_ShouldReturnCardResponse() {
        long userId = 1L;
        CardRequest request = new CardRequest(userId, "12345678", "JOHN DOE", LocalDate.now().plusYears(1));
        User user = new User();
        user.setId(userId);
        Card card = new Card();
        Card savedCard = new Card();
        CardResponse expectedResponse = new CardResponse(10L, userId, "12345678", "JOHN DOE", LocalDate.now().plusYears(1));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardMapper.toEntity(request)).thenReturn(card);
        when(cardRepository.save(card)).thenReturn(savedCard);
        when(cardMapper.toDto(savedCard)).thenReturn(expectedResponse);

        CardResponse actualResponse = cardService.createCard(request);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(userRepository).findById(userId);
        verify(cardRepository).save(card);
        verify(userService).evictUserWithCardsCache(userId);
    }

    @Test
    @DisplayName("Should throw CardNotFoundException when user for card creation not found")
    void createCard_WhenUserDoesNotExist_ShouldThrowException() {
        long userId = 1L;
        CardRequest request = new CardRequest(userId, "12345678", "JOHN DOE", LocalDate.now());

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.createCard(request))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessageContaining("User not found with id: " + userId);

        verify(cardRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully update an existing card")
    void updateCard_WhenCardExists_ShouldReturnCardResponse() {
        long cardId = 10L;
        long userId = 1L;
        CardRequest request = new CardRequest(userId, "87654321", "JANE DOE", LocalDate.now().plusYears(2));
        User user = new User();
        user.setId(userId);
        Card card = new Card();
        card.setUser(user);
        Card updatedCard = new Card();
        CardResponse expectedResponse = new CardResponse(cardId, userId, "87654321", "JANE DOE", LocalDate.now().plusYears(2));

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(updatedCard);
        when(cardMapper.toDto(updatedCard)).thenReturn(expectedResponse);

        CardResponse actualResponse = cardService.updateCard(cardId, request);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(cardMapper).updateFromDto(request, card);
        verify(userService).evictUserWithCardsCache(userId);
    }

    @Test
    @DisplayName("Should return card when found by ID")
    void getCardById_WhenCardExists_ShouldReturnCardResponse() {
        long cardId = 10L;
        Card card = new Card();
        CardResponse expectedResponse = new CardResponse(cardId, 1L, "111", "HOLDER", LocalDate.now());

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardMapper.toDto(card)).thenReturn(expectedResponse);

        CardResponse actualResponse = cardService.getCardById(cardId);

        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("Should return page of all cards")
    void getAllCards_ShouldReturnPageOfCardResponses() {
        Pageable pageable = PageRequest.of(0, 10);
        Card card = new Card();
        Page<Card> cardPage = new PageImpl<>(List.of(card));
        CardResponse expectedResponse = new CardResponse(1L, 1L, "111", "H", LocalDate.now());

        when(cardRepository.findAll(pageable)).thenReturn(cardPage);
        when(cardMapper.toDto(card)).thenReturn(expectedResponse);

        Page<CardResponse> actualResponse = cardService.getAllCards(pageable);

        assertThat(actualResponse.getContent()).hasSize(1);
        assertThat(actualResponse.getContent().get(0)).isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("Should delete card when card exists")
    void deleteCardById_WhenCardExists_ShouldExecuteDeletion() {
        long cardId = 10L;
        long userId = 1L;
        User user = new User();
        user.setId(userId);
        Card card = new Card();
        card.setUser(user);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        cardService.deleteCardById(cardId);

        verify(cardRepository).deleteById(cardId);
        verify(userService).evictUserWithCardsCache(userId);
    }

    @Test
    @DisplayName("Should return page of cards by user ID")
    void getCardsByUserId_ShouldReturnPageOfCardResponses() {
        long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Card card = new Card();
        Page<Card> cardPage = new PageImpl<>(List.of(card));
        CardResponse expectedResponse = new CardResponse(10L, userId, "111", "H", LocalDate.now());

        // FIX: Using matchers for both arguments to avoid InvalidUseOfMatchersException
        when(cardRepository.findCardsByUserId(eq(userId), eq(pageable))).thenReturn(cardPage);
        when(cardMapper.toDto(any(Card.class))).thenReturn(expectedResponse);

        Page<CardResponse> actualResponse = cardService.getCardsByUserId(userId, pageable);

        assertThat(actualResponse.getContent()).hasSize(1);
        verify(cardRepository).findCardsByUserId(eq(userId), eq(pageable));
    }
}