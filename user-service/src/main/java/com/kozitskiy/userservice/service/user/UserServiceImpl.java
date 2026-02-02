package com.kozitskiy.userservice.service.user;

import com.kozitskiy.userservice.dto.UserRequest;
import com.kozitskiy.userservice.dto.CardResponse;
import com.kozitskiy.userservice.dto.UserResponse;
import com.kozitskiy.userservice.dto.UserWithCardResponse;
import com.kozitskiy.userservice.entity.Card;
import com.kozitskiy.userservice.entity.User;
import com.kozitskiy.userservice.exception.UserNotFoundException;
import com.kozitskiy.userservice.repository.CardRepository;
import com.kozitskiy.userservice.repository.UserRepository;
import com.kozitskiy.userservice.mapper.CardMapper;
import com.kozitskiy.userservice.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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

    private static final String USER_CACHE = "UserCache";
    private static final String USER_CARDS_CACHE = "UserCardsCache";

    @Override
    @Transactional
    @Caching(put = {
            @CachePut(value = USER_CACHE, key = "#result.id"),
            @CachePut(value = USER_CACHE, key = "#result.email")
    })
    public UserResponse createUser(UserRequest userDto) {
        User user = userMapper.toEntity(userDto);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = USER_CACHE, key = "#id", sync = true)
    public UserResponse getUserById(long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userMapper.toDtoList(userRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = USER_CACHE, key = "#email")
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    @Caching(put = {
            @CachePut(value = USER_CACHE, key = "#id"),
            @CachePut(value = USER_CACHE, key = "#result.email")
    },
    evict = {
            @CacheEvict(value = USER_CARDS_CACHE, key = "#id")
    })
    public UserResponse updateUserById(long id, UserRequest dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + id));
        userMapper.updateFromDto(dto, user);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = USER_CACHE, key = "#id"),
            @CacheEvict(value = USER_CARDS_CACHE, key = "#id"),
            @CacheEvict(value = USER_CACHE, allEntries = true)
    })
    public void deleteUserById(long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = USER_CARDS_CACHE, key = "#userId")
    public UserWithCardResponse getUserWithCards(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        List<Card> cards = cardRepository.findCardsByUserId(userId, Pageable.unpaged()).getContent();

        UserResponse userDto = userMapper.toDto(user);
        List<CardResponse> cardDtos = cardMapper.toDtoList(cards);

        return UserWithCardResponse.builder()
                .id(userDto.id())
                .name(userDto.name())
                .surname(userDto.surname())
                .email(userDto.email())
                .birthDate(userDto.birthDate())
                .cards(cardDtos)
                .build();
    }

    @Override
    public void evictUserWithCardsCache(long userId) {

    }
}
