package com.kozitskiy.userservice.unit.service;

import com.kozitskiy.userservice.dto.request.CreateCardDto;
import com.kozitskiy.userservice.dto.response.CardResponseDto;
import com.kozitskiy.userservice.entity.Card;
import com.kozitskiy.userservice.entity.User;
import com.kozitskiy.userservice.exception.CardNotFoundException;
import com.kozitskiy.userservice.repository.CardRepository;
import com.kozitskiy.userservice.repository.UserRepository;
import com.kozitskiy.userservice.service.card.CardServiceImpl;
import com.kozitskiy.userservice.service.user.UserService;
import com.kozitskiy.userservice.util.CardMapper;
import com.kozitskiy.userservice.util.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CardMapper cardMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private UserService userService;
    @InjectMocks
    private CardServiceImpl cardService;


//    private final User testUser = User.builder()
//            .name("test")
//            .surname("test")
//            .birthDate()
//            .email("test@mail.ru")
//            .build();
//
//    private final Card testCard = Card.builder()
//            .id(1L)
//            .user()
//            .holder("holder")
//            .build()

    @Test
    void getCardById_shouldReturnCard_whenCardExists(){
        Long cardId = 1L;
        Long userId = 1L;
        Card card = new Card();
        User user = new User();

        card.setId(cardId);
        user.setId(userId);

        CardResponseDto dto = new CardResponseDto();
        dto.setId(cardId);
        dto.setUserId(userId);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardMapper.toDto(card)).thenReturn(dto);

        CardResponseDto result = cardService.getCardById(cardId);

        assertThat(result.getId()).isEqualTo(cardId);
        verify(cardRepository).findById(cardId);
        verify(cardMapper).toDto(card);
    }

    @Test
    void getCardById_shouldThrowException_whenCardNotFound(){
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getCardById(1L))
                .isInstanceOf(CardNotFoundException.class);
    }

    @Test
    void getAllCards_shouldReturnMappedPageOfCards(){
        Pageable pageable = PageRequest.of(0,10);

        Card card1 = new Card();
        card1.setId(1L);
        Card card2 = new Card();
        card2.setId(2L);
        List<Card> cardList = List.of(card1, card2);
        Page<Card> cardPage = new PageImpl<>(cardList);

        CardResponseDto dto1 = new CardResponseDto();
        dto1.setId(1L);
        CardResponseDto dto2 = new CardResponseDto();
        dto2.setId(2L);

        when(cardRepository.findAll(pageable)).thenReturn(cardPage);

        when(cardMapper.toDto(card1)).thenReturn(dto1);
        when(cardMapper.toDto(card2)).thenReturn(dto2);

        Page<CardResponseDto> result = cardService.getAllCards(pageable);

        assertThat(result).hasSize(2);
        assertThat(result.getContent()).containsExactly(dto1, dto2);

        verify(cardRepository).findAll(pageable);
        verify(cardMapper).toDto(card1);
        verify(cardMapper).toDto(card2);
    }

    @Test
    void deleteCardById_shouldDeleteCardAndEvictUserCache(){
        long cardId = 1L;
        long userId = 1L;

        User user = new User();
        user.setId(userId);

        Card card = new Card();
        card.setId(cardId);
        card.setUser(user);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        cardService.deleteCardById(cardId);

        verify(cardRepository).findById(cardId);
        verify(cardRepository).deleteById(cardId);
        verify(userService).evictUserWithCardsCache(userId); // now works!
    }

    @Test
    void deleteCardById_shouldThrowCardNotFoundException_whenCardDoesNotExist(){
        long cardId = 999L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.deleteCardById(cardId))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessage("Card not found with id: " + cardId);

        verify(cardRepository).findById(cardId);
        verify(cardRepository, never())
                .deleteById(anyLong());
        verify(userService, never())
                .evictUserWithCardsCache(anyLong());
    }

    @Test
    void getCardsByUserId_shouldReturnMappedPageOfCards(){
        long userId = 1L;
        Pageable pageable = PageRequest.of(0,10);

        Card card1 = new Card();
        card1.setId(1L);
        Card card2 = new Card();
        card2.setId(2L);

        List<Card> cardList = List.of(card1, card2);
        Page<Card> cardPage = new PageImpl<>(cardList, pageable, cardList.size());

        CardResponseDto dto1 = new CardResponseDto();
        dto1.setId(1L);
        CardResponseDto dto2 = new CardResponseDto();
        dto2.setId(2L);

        when(cardRepository.findCardsByUserId(userId, pageable))
                .thenReturn(cardPage);
        when(cardMapper.toDto(card1)).thenReturn(dto1);
        when(cardMapper.toDto(card2)).thenReturn(dto2);

        Page<CardResponseDto> result = cardService.getCardsByUserId(userId, pageable);

        assertThat(result).hasSize(2);
        assertThat(result.getContent()).containsExactly(dto1, dto2);
        assertThat(result.getPageable()).isEqualTo(pageable);
        verify(cardRepository).findCardsByUserId(userId, pageable);
        verify(cardMapper).toDto(card1);
        verify(cardMapper).toDto(card2);
    }

    @Test
    void createCard_shouldCreateAndReturnCardResponseDto_whenUserExists(){
        long userId = 1L;
        CreateCardDto dto = new CreateCardDto();
        dto.setUserId(userId);
        dto.setHolder("John Doe");

        User user = new User();
        user.setId(userId);

        Card cardEntity = new Card();
        cardEntity.setHolder("John Doe");
        cardEntity.setUser(user);

        Card savedCard = new Card();
        savedCard.setId(100L);
        savedCard.setHolder("John Doe");
        savedCard.setUser(user);

        CardResponseDto responseDto = new CardResponseDto();
        responseDto.setId(100L);
        responseDto.setUserId(userId);
        responseDto.setHolder("John Doe");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardMapper.toEntity(dto)).thenReturn(cardEntity);
        when(cardRepository.save(cardEntity)).thenReturn(savedCard);
        when(cardMapper.toDto(savedCard)).thenReturn(responseDto);

        // when
        CardResponseDto result = cardService.createCard(dto);

        // then
        assertThat(result).isEqualTo(responseDto);
        verify(userRepository).findById(userId);
        verify(cardMapper).toEntity(dto);
        verify(cardRepository).save(cardEntity);
        verify(userService).evictUserWithCardsCache(userId);
        verify(cardMapper).toDto(savedCard);
    }

    @Test
    void createCard_shouldThrowCardNotFoundException_whenUserDoesNotExits(){
        CreateCardDto dto = new CreateCardDto();
        dto.setUserId(999L);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.createCard(dto))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessage("User not found with id: 999");

        verify(userRepository).findById(999L);
        verify(cardMapper, never()).toEntity(any());
        verify(cardRepository, never()).save(any());
        verify(userService, never()).evictUserWithCardsCache(anyLong());
    }

    @Test
    void updateCard_shouldUpdateAndReturnCardResponseDto_whenCardExists() {
        long cardId = 5L;
        long userId = 2L;

        CreateCardDto dto = new CreateCardDto();
        dto.setHolder("Updated Holder");

        User user = new User();
        user.setId(userId);

        Card existingCard = new Card();
        existingCard.setId(cardId);
        existingCard.setUser(user);
        existingCard.setHolder("Old Holder");

        Card updatedCard = new Card();
        updatedCard.setId(cardId);
        updatedCard.setUser(user);
        updatedCard.setHolder("Updated Holder");

        CardResponseDto responseDto = new CardResponseDto();
        responseDto.setId(cardId);
        responseDto.setUserId(userId);
        responseDto.setHolder("Updated Holder");

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(existingCard));
        when(cardRepository.save(existingCard)).thenReturn(updatedCard);
        when(cardMapper.toDto(updatedCard)).thenReturn(responseDto);

        CardResponseDto result = cardService.updateCard(cardId, dto);

        assertThat(result).isEqualTo(responseDto);
        verify(cardRepository).findById(cardId);
        verify(cardMapper).updateFromDto(dto, existingCard);
        verify(cardRepository).save(existingCard);
        verify(userService).evictUserWithCardsCache(userId);
        verify(cardMapper).toDto(updatedCard);
    }

    @Test
    void updateCard_shouldThrowCardNotFoundException_whenCardDoesNotExist(){
        long cardId = 999L;
        CreateCardDto dto = new CreateCardDto();
        dto.setHolder("Test");

        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.updateCard(cardId, dto))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessage("Card not found with id: 999");

        verify(cardRepository).findById(cardId);
        verify(cardMapper, never()).updateFromDto(any(), any());
        verify(cardRepository, never()).save(any());
        verify(userService, never()).evictUserWithCardsCache(anyLong());
    }



}
