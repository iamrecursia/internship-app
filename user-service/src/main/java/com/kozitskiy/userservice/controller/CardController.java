package com.kozitskiy.userservice.controller;

import com.kozitskiy.userservice.dto.request.CreateCardDto;
import com.kozitskiy.userservice.dto.response.CardResponseDto;
import com.kozitskiy.userservice.service.card.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CardResponseDto> createCard(@Valid @RequestBody CreateCardDto dto) {
        CardResponseDto card = cardService.createCard(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CardResponseDto> updateCard(@PathVariable long id, @Valid @RequestBody CreateCardDto dto) {
        CardResponseDto card = cardService.updateCard(id, dto);
        return ResponseEntity.ok(card);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardResponseDto> getCardById(@PathVariable long id) {
        CardResponseDto card = cardService.getCardById(id);
        return ResponseEntity.ok(card);
    }

    @GetMapping
    public ResponseEntity<Page<CardResponseDto>> getAllCards(Pageable pageable) {
        Page<CardResponseDto> card = cardService.getAllCards(pageable);
        return ResponseEntity.ok(card);
    }

    @GetMapping("/{userId}/by")
    public ResponseEntity<Page<CardResponseDto>> getCardsByUserId(@PathVariable Long userId, Pageable pageable){
        Page<CardResponseDto> cards = cardService.getCardsByUserId(userId, pageable);
        return ResponseEntity.ok(cards);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable long id) {
        cardService.deleteCardById(id);
        return ResponseEntity.noContent().build();
    }
}

/*

{
  "name": "John",
  "surname": "Biden",
  "birthDate": "1990-05-15T00:00:00",
  "email": "john.doe@example.com"
}


 */