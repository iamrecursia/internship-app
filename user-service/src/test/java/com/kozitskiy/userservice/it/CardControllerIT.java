package com.kozitskiy.userservice.it;

import com.kozitskiy.userservice.dto.CardRequest;
import com.kozitskiy.userservice.dto.CardResponse;
import com.kozitskiy.userservice.dto.UserRequest;
import com.kozitskiy.userservice.dto.UserResponse;
import com.kozitskiy.userservice.repository.CardRepository;
import com.kozitskiy.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CardControllerIT extends BaseIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardRepository cardRepository;

    private Long savedUserId;

    @BeforeEach
    void setUp() {
        cardRepository.deleteAll();
        userRepository.deleteAll();

        UserRequest userRequest = new UserRequest("Ivan", "Ivanov", LocalDate.of(1990, 5, 10), "ivan@example.com");
        ResponseEntity<UserResponse> userResponse = restTemplate.postForEntity("/api/v1/users", userRequest, UserResponse.class);
        savedUserId = userResponse.getBody().id();
    }

    @Test
    @DisplayName("Should successfully create a card for existing user")
    void createCard_ShouldSaveToDb() {
        CardRequest request = new CardRequest(savedUserId, "1111222233334444", "IVAN IVANOV", LocalDate.now().plusYears(3));

        ResponseEntity<CardResponse> response = restTemplate.postForEntity("/api/v1/cards", request, CardResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().number()).isEqualTo("1111222233334444");
        assertThat(cardRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should return card by id from database")
    void getCardById_ShouldReturnStoredCard() {
        CardRequest request = new CardRequest(savedUserId, "5555666677778888", "IVAN IVANOV", LocalDate.now().plusYears(1));
        Long cardId = restTemplate.postForEntity("/api/v1/cards", request, CardResponse.class).getBody().id();

        ResponseEntity<CardResponse> response = restTemplate.getForEntity("/api/v1/cards/" + cardId, CardResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().id()).isEqualTo(cardId);
    }

    @Test
    @DisplayName("Should update card details in database")
    void updateCard_ShouldUpdateDbRecord() {
        CardRequest initial = new CardRequest(savedUserId, "1234123412341234", "OLD HOLDER", LocalDate.now().plusYears(1));
        Long cardId = restTemplate.postForEntity("/api/v1/cards", initial, CardResponse.class).getBody().id();

        CardRequest updateRequest = new CardRequest(savedUserId, "1234123412341234", "NEW HOLDER", LocalDate.now().plusYears(2));

        ResponseEntity<CardResponse> response = restTemplate.exchange(
                "/api/v1/cards/" + cardId,
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                CardResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().holder()).isEqualTo("NEW HOLDER");
    }

    @Test
    @DisplayName("Should return page of cards by user id")
    void getCardsByUserId_ShouldReturnPage() {
        CardRequest card1 = new CardRequest(savedUserId, "1111000011110000", "IVAN 1", LocalDate.now().plusYears(1));
        restTemplate.postForEntity("/api/v1/cards", card1, CardResponse.class);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/v1/cards/" + savedUserId + "/by",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("content")).asList().isNotEmpty();
    }

    @Test
    @DisplayName("Should delete card and remove it from database")
    void deleteCard_ShouldRemoveFromDb() {
        CardRequest request = new CardRequest(savedUserId, "9999000099990000", "IVAN IVANOV", LocalDate.now().plusYears(1));
        Long cardId = restTemplate.postForEntity("/api/v1/cards", request, CardResponse.class).getBody().id();

        restTemplate.delete("/api/v1/cards/" + cardId);

        assertThat(cardRepository.findById(cardId)).isEmpty();
    }
}