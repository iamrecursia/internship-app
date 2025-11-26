package com.kozitskiy.userservice.service.card;

import com.kozitskiy.userservice.dto.request.CreateCardDto;
import com.kozitskiy.userservice.dto.response.CardResponseDto;
import com.kozitskiy.userservice.entity.Card;
import com.kozitskiy.userservice.entity.User;
import com.kozitskiy.userservice.exception.CardNotFoundException;
import com.kozitskiy.userservice.repository.CardRepository;
import com.kozitskiy.userservice.repository.UserRepository;
import com.kozitskiy.userservice.service.user.UserService;
import com.kozitskiy.userservice.util.CardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService{
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;
    private final UserService userService;

    private static final String CARD_BY_ID_CACHE = "CardService::getById";;

    @Override
    @Transactional
    public CardResponseDto createCard(CreateCardDto cardDto) {
        User user = userRepository.findById(cardDto.getUserId())
                .orElseThrow(() -> new CardNotFoundException("User not found with id: " + cardDto.getUserId()));

        Card card = cardMapper.toEntity(cardDto);
        card.setUser(user);
        Card saved = cardRepository.save(card);

        userService.evictUserWithCardsCache(user.getId());

        return cardMapper.toDto(saved);
    }

    @Override
    public CardResponseDto updateCard(long id, CreateCardDto dto) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + id));

        long userId = card.getUser().getId();
        cardMapper.updateFromDto(dto, card);
        Card updated = cardRepository.save(card);

        userService.evictUserWithCardsCache(userId);

        return cardMapper.toDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
//    @Cacheable(value = CARD_BY_ID_CACHE, key = "#id", sync = true)
    public CardResponseDto getCardById(long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + id));
        return cardMapper.toDto(card);
    }

    @Override
    public Page<CardResponseDto> getAllCards(Pageable pageable) {
        Page<Card> cards = cardRepository.findAll(pageable);
        return cards.map(cardMapper::toDto);
    }

    @Override
    @Transactional
    public void deleteCardById(long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + id));

        long userId = card.getUser().getId();
        cardRepository.deleteById(id);

        userService.evictUserWithCardsCache(userId);
    }

    @Override
    public Page<CardResponseDto> getCardsByUserId(long userId, Pageable pageable) {
        Page<Card> cards = cardRepository.findCardsByUserId(userId, pageable);
        return cards.map(cardMapper::toDto);
    }
}
