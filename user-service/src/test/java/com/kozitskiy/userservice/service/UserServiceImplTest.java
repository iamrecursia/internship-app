package com.kozitskiy.userservice.service;


import com.kozitskiy.userservice.dto.CardResponse;
import com.kozitskiy.userservice.dto.UserRequest;
import com.kozitskiy.userservice.dto.UserResponse;
import com.kozitskiy.userservice.dto.UserWithCardResponse;
import com.kozitskiy.userservice.entity.Card;
import com.kozitskiy.userservice.entity.User;
import com.kozitskiy.userservice.exception.UserNotFoundException;
import com.kozitskiy.userservice.mapper.CardMapper;
import com.kozitskiy.userservice.mapper.UserMapper;
import com.kozitskiy.userservice.repository.CardRepository;
import com.kozitskiy.userservice.repository.UserRepository;
import com.kozitskiy.userservice.service.user.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
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
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Should successfully create a user")
    void createUser_ShouldReturnUserResponse() {

        UserRequest request = new UserRequest("John", "Doe", LocalDate.of(1990, 1, 1), "john@example.com");
        User user = new User();
        User savedUser = new User();
        UserResponse expectedResponse = new UserResponse(1L, "John", "Doe", "john@example.com", LocalDate.of(1990, 1, 1));

        when(userMapper.toEntity(request)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toDto(savedUser)).thenReturn(expectedResponse);

        UserResponse actualResponse = userService.createUser(request);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Should return user when found by ID")
    void getUserById_WhenUserExists_ShouldReturnResponse() {
        long userId = 1L;
        User user = new User();
        UserResponse expectedResponse = new UserResponse(userId, "John", "Doe", "john@example.com", LocalDate.of(1990, 1, 1));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(expectedResponse);

        UserResponse actualResponse = userService.getUserById(userId);

        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when getting non-existent user by ID")
    void getUserById_WhenUserDoesNotExist_ShouldThrowException() {
        long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found: " + userId);
    }

    @Test
    @DisplayName("Should return list of all users")
    void getAllUsers_ShouldReturnList() {
        List<User> users = List.of(new User(), new User());
        List<UserResponse> expectedResponses = List.of(
                new UserResponse(1L, "A", "B", "a@b.com", LocalDate.now()),
                new UserResponse(2L, "C", "D", "c@d.com", LocalDate.now())
        );

        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toDtoList(users)).thenReturn(expectedResponses);

        List<UserResponse> actualResponses = userService.getAllUsers();

        assertThat(actualResponses).hasSize(2).isEqualTo(expectedResponses);
    }

    @Test
    @DisplayName("Should successfully update an existing user")
    void updateUserById_WhenUserExists_ShouldReturnUpdatedResponse() {

        long userId = 1L;
        UserRequest request = new UserRequest("Jane", "Smith", LocalDate.of(1992, 2, 2), "jane@example.com");
        User user = new User();
        UserResponse expectedResponse = new UserResponse(userId, "Jane", "Smith", "jane@example.com", LocalDate.of(1992, 2, 2));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(expectedResponse);

        UserResponse actualResponse = userService.updateUserById(userId, request);

        assertThat(actualResponse.name()).isEqualTo("Jane");
        verify(userMapper).updateFromDto(request, user);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should delete user when user exists")
    void deleteUserById_WhenUserExists_ShouldExecuteDeletion() {
        long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);

        userService.deleteUserById(userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    @DisplayName("Should return user details with associated cards")
    void getUserWithCards_WhenUserExists_ShouldReturnCombinedResponse() {
        long userId = 1L;
        User user = new User();
        UserResponse userDto = new UserResponse(userId, "John", "Doe", "john@example.com", LocalDate.of(1990, 1, 1));

        List<Card> cards = List.of(new Card());
        List<CardResponse> cardDtos = List.of(
                new CardResponse(10L, userId, "1111-2222", "JOHN DOE", LocalDate.of(2028, 12, 31))
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardRepository.findCardsByUserId(eq(userId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(cards));
        when(userMapper.toDto(user)).thenReturn(userDto);
        when(cardMapper.toDtoList(cards)).thenReturn(cardDtos);

        UserWithCardResponse result = userService.getUserWithCards(userId);

        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.email()).isEqualTo("john@example.com");
        assertThat(result.cards()).hasSize(1);
        assertThat(result.cards().get(0).number()).isEqualTo("1111-2222");
    }
}