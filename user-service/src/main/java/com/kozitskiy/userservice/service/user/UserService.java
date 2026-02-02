package com.kozitskiy.userservice.service.user;

import com.kozitskiy.userservice.dto.UserRequest;
import com.kozitskiy.userservice.dto.UserResponse;
import com.kozitskiy.userservice.dto.UserWithCardResponse;

import java.util.List;

public interface UserService {
    UserResponse createUser(UserRequest userDao);

    UserResponse getUserById(long id);

    List<UserResponse> getAllUsers();

    UserResponse getUserByEmail(String email);

    UserResponse updateUserById(long id, UserRequest dto);

    void deleteUserById(long id);

    UserWithCardResponse getUserWithCards(long userId);

    void evictUserWithCardsCache(long userId);
}
