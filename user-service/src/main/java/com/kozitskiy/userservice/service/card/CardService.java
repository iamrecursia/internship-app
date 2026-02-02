package com.kozitskiy.userservice.service.card;

import com.kozitskiy.userservice.dto.CardRequest;
import com.kozitskiy.userservice.dto.CardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CardService {
    CardResponse createCard(CardRequest cardDto);

    CardResponse updateCard(long id, CardRequest dto);

    CardResponse getCardById(long id);

    Page<CardResponse> getAllCards(Pageable pageable);

    void deleteCardById(long id);

    Page<CardResponse> getCardsByUserId(long userId, Pageable pageable);
}
