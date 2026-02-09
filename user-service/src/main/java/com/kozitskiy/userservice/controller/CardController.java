package com.kozitskiy.userservice.controller;

import com.kozitskiy.userservice.dto.CardRequest;
import com.kozitskiy.userservice.dto.CardResponse;
import com.kozitskiy.userservice.service.card.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {
    private final CardService cardService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CardRequest dto) {
        CardResponse card = cardService.createCard(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CardResponse> updateCard(@PathVariable long id, @Valid @RequestBody CardRequest dto) {
        CardResponse card = cardService.updateCard(id, dto);
        return ResponseEntity.ok(card);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardResponse> getCardById(@PathVariable long id) {
        CardResponse card = cardService.getCardById(id);
        return ResponseEntity.ok(card);
    }

    @GetMapping
    public ResponseEntity<Page<CardResponse>> getAllCards(Pageable pageable) {
        Page<CardResponse> card = cardService.getAllCards(pageable);
        return ResponseEntity.ok(card);
    }

    @GetMapping("/{userId}/by")
    public ResponseEntity<Page<CardResponse>> getCardsByUserId(@PathVariable Long userId, Pageable pageable){
        Page<CardResponse> cards = cardService.getCardsByUserId(userId, pageable);
        return ResponseEntity.ok(cards);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable long id) {
        cardService.deleteCardById(id);
        return ResponseEntity.noContent().build();
    }
}
