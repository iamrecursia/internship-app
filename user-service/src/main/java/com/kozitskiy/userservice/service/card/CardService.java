package com.kozitskiy.userservice.service.card;

import com.kozitskiy.userservice.dto.request.CreateCardDto;
import com.kozitskiy.userservice.dto.response.CardResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CardService {
    CardResponseDto createCard(CreateCardDto cardDto);
    CardResponseDto updateCard(long id, CreateCardDto dto);
    CardResponseDto getCardById(long id);
    Page<CardResponseDto> getAllCards(Pageable pageable);
    void deleteCardById(long id);
    Page<CardResponseDto> getCardsByUserId(long userId, Pageable pageable);
}
