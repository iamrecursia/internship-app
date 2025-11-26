package com.kozitskiy.userservice.service.user;

import com.kozitskiy.userservice.dto.request.CreateUserDto;
import com.kozitskiy.userservice.dto.response.CardResponseDto;
import com.kozitskiy.userservice.dto.response.UserResponseDto;
import com.kozitskiy.userservice.dto.response.UserWithCardResponseDto;
import com.kozitskiy.userservice.entity.Card;
import com.kozitskiy.userservice.entity.User;
import com.kozitskiy.userservice.exception.UserNotFoundException;
import com.kozitskiy.userservice.repository.CardRepository;
import com.kozitskiy.userservice.repository.UserRepository;
import com.kozitskiy.userservice.util.CardMapper;
import com.kozitskiy.userservice.util.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final UserMapper userMapper;
    private final CardMapper cardMapper;

    private static final String USER_WITH_CARDS_CACHE = "UserService::userWithCards";
    private static final String USER_BY_ID_CACHE = "UserService::getById";
    private static final String USER_BY_EMAIL_CACHE = "UserService::getByEmail";

    @Override
    @Transactional
//    @Caching(put = {
//            @CachePut(value = USER_BY_ID_CACHE, key = "#result.id"),
//            @CachePut(value = USER_BY_EMAIL_CACHE, key = "#result.email"),
//    })
    public UserResponseDto createUser(CreateUserDto userDto) {
        User user = userMapper.toEntity(userDto);
        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
//    @Cacheable(value = USER_BY_ID_CACHE, key = "#id", sync = true)
    public UserResponseDto getUserById(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id = " + id + " not found"));
        return userMapper.toDto(user);
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        return userMapper.toDtoList(userRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
//    @Cacheable(value = USER_BY_EMAIL_CACHE, key = "#email", sync = true)
    public UserResponseDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
//    @Caching(put = {
//            @CachePut(value = USER_BY_ID_CACHE, key = "#id"),
//    }, evict = {
//            @CacheEvict(value = USER_WITH_CARDS_CACHE, key = "#id")
//    }
//    )
    public UserResponseDto updateUserById(long id, CreateUserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + id));
        userMapper.updateFromDto(dto, user);
        User updated = userRepository.save(user);
        return userMapper.toDto(updated);
    }

    @Override
    @Transactional
//    @Caching(evict = {
//            @CacheEvict(value = USER_BY_ID_CACHE, key = "#id"),
//            @CacheEvict(value = USER_WITH_CARDS_CACHE, key = "#id"),
//            @CacheEvict(value = USER_BY_EMAIL_CACHE, allEntries = true)
//    })
    public void deleteUserById(long id) {
        User user = userRepository.findById(id)
                        .orElseThrow(() -> new UserNotFoundException("User with id: "+ id+ " not found"));
        long userId = user.getId();
        userRepository.deleteById(userId);
    }

    @Override
    @Transactional(readOnly = true)
//    @Cacheable(value = USER_WITH_CARDS_CACHE, key = "#userId")
    public UserWithCardResponseDto getUserWithCards(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        List<Card> cards = cardRepository.findCardsByUserId(userId, Pageable.unpaged()).getContent();

        UserResponseDto userDto = userMapper.toDto(user);
        List<CardResponseDto> cardDtos = cardMapper.toDtoList(cards);

        return UserWithCardResponseDto.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .surname(userDto.getSurname())
                .email(userDto.getEmail())
                .birthDate(userDto.getBirthDate())
                .cards(cardDtos)
                .build();
    }

    @Override
//    @CacheEvict(value = USER_WITH_CARDS_CACHE, key = "#userId")
    public void evictUserWithCardsCache(long userId) {

    }
}
