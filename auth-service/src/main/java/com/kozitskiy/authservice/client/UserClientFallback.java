package com.kozitskiy.authservice.client;

import com.kozitskiy.authservice.dto.CreateUserRequest;
import com.kozitskiy.authservice.dto.UserResponseDto;
import com.kozitskiy.authservice.exception.UserServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class UserClientFallback implements UserClient {

    private static final Logger log = LoggerFactory.getLogger(UserClientFallback.class);
    @Override
    public ResponseEntity<UserResponseDto> createUser(CreateUserRequest request) {
        log.error("Fallback triggered: user-service is unavailable. Failed to create user with email: {}",
                request.email());

        throw new UserServiceUnavailableException("User service is currently unavailable");
    }
}
