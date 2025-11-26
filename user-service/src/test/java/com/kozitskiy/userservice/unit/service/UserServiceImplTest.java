package com.kozitskiy.userservice.unit.service;

import com.kozitskiy.userservice.dto.request.CreateUserDto;
import com.kozitskiy.userservice.dto.response.CardResponseDto;
import com.kozitskiy.userservice.dto.response.UserResponseDto;
import com.kozitskiy.userservice.dto.response.UserWithCardResponseDto;
import com.kozitskiy.userservice.entity.Card;
import com.kozitskiy.userservice.entity.User;
import com.kozitskiy.userservice.exception.UserNotFoundException;
import com.kozitskiy.userservice.repository.CardRepository;
import com.kozitskiy.userservice.repository.UserRepository;
import com.kozitskiy.userservice.service.user.UserServiceImpl;
import com.kozitskiy.userservice.util.CardMapper;
import com.kozitskiy.userservice.util.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

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
    void getUserById_shouldReturnUser_whenUserExists(){
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        UserResponseDto dto = new UserResponseDto();
        dto.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(dto);

        UserResponseDto result = userService.getUserById(userId);

        assertThat(result.getId()).isEqualTo(userId);
        verify(userRepository).findById(userId);
        verify(userMapper).toDto(user);
    }

    @Test
    void getUserById_shouldThrowException_whenUserNotFound(){
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(1L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getAllUsers_shouldReturnListOfUserResponseDto(){
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        List<User> users = List.of(user1, user2);

        UserResponseDto dto1 = new UserResponseDto();
        dto1.setId(1L);
        UserResponseDto dto2 = new UserResponseDto();
        dto2.setId(2L);
        List<UserResponseDto> expectedDtos = List.of(dto1, dto2);


        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toDtoList(users)).thenReturn(expectedDtos);

        List<UserResponseDto> result = userService.getAllUsers();

        assertThat(result).hasSize(2).isEqualTo(expectedDtos);
        verify(userRepository).findAll();
        verify(userMapper).toDtoList(users);
    }

    @Test
    void deleteUserById_shouldFindAndDeleteUserAndEvictCache() {
        long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.deleteUserById(userId);

        verify(userRepository).findById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUserById_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUserById(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User with id: 999 not found");

        verify(userRepository).findById(userId);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void getUserWithCards_shouldReturnUserWithCardResponseDto_whenUserExists() {
        long userId = 1L;

        User user = new User();
        user.setId(userId);
        user.setName("John");
        user.setSurname("Doe");
        user.setEmail("john@example.com");

        Card card1 = new Card();
        card1.setId(10L);
        card1.setHolder("John Doe");
        List<Card> cards = List.of(card1);

        UserResponseDto userDto = new UserResponseDto();
        userDto.setId(userId);
        userDto.setName("John");
        userDto.setSurname("Doe");
        userDto.setEmail("john@example.com");

        CardResponseDto cardDto = new CardResponseDto();
        cardDto.setId(10L);
        cardDto.setHolder("John Doe");
        List<CardResponseDto> cardDtos = List.of(cardDto);

        UserWithCardResponseDto expectedResponse = UserWithCardResponseDto.builder()
                .id(userId)
                .name("John")
                .surname("Doe")
                .email("john@example.com")
                .cards(cardDtos)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cardRepository.findCardsByUserId(userId, Pageable.unpaged()))
                .thenReturn(new PageImpl<>(cards));
        when(userMapper.toDto(user)).thenReturn(userDto);
        when(cardMapper.toDtoList(cards)).thenReturn(cardDtos);

        UserWithCardResponseDto result = userService.getUserWithCards(userId);

        assertThat(result).usingRecursiveComparison().isEqualTo(expectedResponse);
        verify(userRepository).findById(userId);
        verify(cardRepository).findCardsByUserId(userId, Pageable.unpaged());
        verify(userMapper).toDto(user);
        verify(cardMapper).toDtoList(cards);
    }

    @Test
    void getUserWithCards_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserWithCards(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: " + userId);

        verify(userRepository).findById(userId);
        verify(cardRepository, never()).findCardsByUserId(anyLong(), any());
        verify(userMapper, never()).toDto(any());
        verify(cardMapper, never()).toDtoList(any());
    }

    @Test
    void updateUserById_shouldUpdateUserAndReturnDto_whenUserExists() {
        long userId = 1L;
        CreateUserDto dto = new CreateUserDto();
        dto.setName("Updated Name");
        dto.setEmail("updated@example.com");

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Old Name");

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setName("Updated Name");
        updatedUser.setEmail("updated@example.com");

        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(userId);
        responseDto.setName("Updated Name");
        responseDto.setEmail("updated@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(responseDto);

        UserResponseDto result = userService.updateUserById(userId, dto);

        assertThat(result).usingRecursiveComparison().isEqualTo(responseDto);
        verify(userRepository).findById(userId);
        verify(userMapper).updateFromDto(dto, existingUser); // обновление in-place
        verify(userRepository).save(existingUser);
        verify(userMapper).toDto(updatedUser);
    }

    @Test
    void updateUserById_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        long userId = 999L;
        CreateUserDto dto = new CreateUserDto();
        dto.setName("Test");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserById(userId, dto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id " + userId);

        verify(userRepository).findById(userId);
        verify(userMapper, never()).updateFromDto(any(), any());
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void createUser_shouldCreateUserAndReturnDto() {
        CreateUserDto dto = new CreateUserDto();
        dto.setName("Alice");
        dto.setEmail("alice@example.com");

        User userEntity = new User();
        userEntity.setName("Alice");
        userEntity.setEmail("alice@example.com");

        User savedUser = new User();
        savedUser.setId(100L);
        savedUser.setName("Alice");
        savedUser.setEmail("alice@example.com");

        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(100L);
        responseDto.setName("Alice");
        responseDto.setEmail("alice@example.com");

        when(userMapper.toEntity(dto)).thenReturn(userEntity);
        when(userRepository.save(userEntity)).thenReturn(savedUser);
        when(userMapper.toDto(savedUser)).thenReturn(responseDto);

        UserResponseDto result = userService.createUser(dto);

        assertThat(result).usingRecursiveComparison().isEqualTo(responseDto);
        verify(userMapper).toEntity(dto);
        verify(userRepository).save(userEntity);
        verify(userMapper).toDto(savedUser);
    }

    @Test
    void getUserByEmail_shouldReturnUserDto_whenUserExists() {
        String email = "alice@example.com";

        User user = new User();
        user.setId(100L);
        user.setName("Alice");
        user.setEmail(email);

        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(100L);
        responseDto.setName("Alice");
        responseDto.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(responseDto);

        UserResponseDto result = userService.getUserByEmail(email);

        assertThat(result).usingRecursiveComparison().isEqualTo(responseDto);
        verify(userRepository).findByEmail(email);
        verify(userMapper).toDto(user);
    }

    @Test
    void getUserByEmail_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        String email = "notfound@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByEmail(email))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with email: " + email);

        verify(userRepository).findByEmail(email);
        verify(userMapper, never()).toDto(any());
    }



}
